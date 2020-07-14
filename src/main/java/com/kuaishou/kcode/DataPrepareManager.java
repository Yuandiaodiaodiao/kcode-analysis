package com.kuaishou.kcode;

import java.nio.ByteBuffer;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

public class DataPrepareManager {
    static int DIRECT_CHUNK_SIZE = 500 * 1024 * 1024;
    DiskReadThread diskRead;
    DistributeBufferThread distributeBuffer;
    HashMapMergeThread mergeThread;
    static RawBufferSolveThread[] rawBufferSolveThreadArray = new RawBufferSolveThread[16];
    static int THREAD_NUMBER = 4;
    static ArrayBlockingQueue<ByteBuffer> canuse = new ArrayBlockingQueue<ByteBuffer>(64);
    static ArrayBlockingQueue<ByteBuffer> canread = new ArrayBlockingQueue<ByteBuffer>(64);
    static ArrayBlockingQueue<BufferWithLatch> unsolvedBuffer = new ArrayBlockingQueue<>(64);
    static ArrayBlockingQueue<ByteBuffer> solvedBuffer = new ArrayBlockingQueue<ByteBuffer>(64);
    static ArrayBlockingQueue<BufferWithLatch> coutdownQueue = new ArrayBlockingQueue<>(256);
    static int MAXTIME=256;
    DataPrepareManager() {
        unsolvedBuffer.clear();
        coutdownQueue.clear();
        diskRead = new DiskReadThread();
        distributeBuffer = new DistributeBufferThread();
        mergeThread = new HashMapMergeThread();

        distributeBuffer.LinkCountDownBuffer(coutdownQueue);
        mergeThread.LinkCountDownBuffer(coutdownQueue);
        diskRead.LinkBlockingQueue(canuse, canread);
        distributeBuffer.LinkDirectBufferBlockingQueue(canuse, canread);
        distributeBuffer.LinkHeapBufferBlockingQueue(unsolvedBuffer, solvedBuffer);
        for (int i = 0; i < THREAD_NUMBER; ++i) {
            rawBufferSolveThreadArray[i] = new RawBufferSolveThread();
            rawBufferSolveThreadArray[i].LinkHeapBufferBlockingQueue(unsolvedBuffer, solvedBuffer);
        }
    }

    public void start(String path, Collection<String> alertRules) {

        int readTimes = diskRead.initChannel(path);
        diskRead.start();
        distributeBuffer.start();
        for (int i = 0; i < THREAD_NUMBER; ++i) {
            rawBufferSolveThreadArray[i].start();
        }
        //这个位置预处理alertRules
        ArrayList<AlertRulesPrepare.Rule> ruleArray = AlertRulesPrepare.prepare(alertRules);
        AlertRulesPrepare.RuleMaps rm = AlertRulesPrepare.prepare3HashMap(ruleArray);
        //rules处理好之后在merge每分钟之后进行报警处理
        mergeThread.setRuleMaps(rm);
//        mergeThread.start();
    }

    public void stop() {
        try {
            diskRead.join();
            distributeBuffer.join();
            for (int i = 0; i < THREAD_NUMBER; ++i) {
                rawBufferSolveThreadArray[i].join();
//                System.out.println("thread"+i+"join");
            }
            unsolvedBuffer.clear();
            solvedBuffer.clear();
            DistributeBufferThread.lastBufferNumbers=THREAD_NUMBER+1;
//            System.gc();
            TimeRange tz=new TimeRange();
            mergeThread.handleMerge(distributeBuffer.lastMinuteTime+2+6);
            for(int i=0;i<THREAD_NUMBER;++i){
                rawBufferSolveThreadArray[i].timeNameIpStore=null;
            }
            tz.point();
            tz.output("handleMerge");
//            mergeThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    HashMap<ByteString, DAGPrepare.AnswerStructure> Q2Answer;
    public void prepareQ2(){
        DAGPrepare dag=new DAGPrepare(mergeThread.firstMinute);
        int firstM = mergeThread.firstMinute;
        int maxM = mergeThread.maxMinute;
        try{
            dag.serviceMapAll=mergeThread.serviceMapAll;
            dag.convertServiceName2VertexId();
            dag.buildDAG();
            dag.topsort();
            dag.solveMaxPathLength();
            dag.solveVertexPath();
            int firstMinute = mergeThread.firstMinute;
            int maxMinute = mergeThread.maxMinute;
            dag.generateAnswer(firstMinute,maxMinute);
            Q2Answer=dag.Q2Answer;

        }catch (OutOfMemoryError e){
//            System.out.println(e.getCause());
        throw new ArrayIndexOutOfBoundsException("OOM"+(maxM-firstM));
        }

    }
    public ArrayList<String>getAnswer1(){
        return mergeThread.warningList;
    }
    public int getServicePairNum() {
        int i = 0;

        HashMap<ByteString, HashMapMergeThread.RuleIpPayload> m = mergeThread.serviceMapAll;
        if (m != null) {
            i = Math.max(i, m.keySet().size());
        }

        return i;
    }
}
