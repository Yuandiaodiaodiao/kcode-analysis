package com.kuaishou.kcode;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;

public class DataPrepareManager {
    static int DIRECT_CHUNK_SIZE = 500 * 1024 * 1024;
    DiskReadThread diskRead;
    DistributeBufferThread distributeBuffer;
    HashMapMergeThread mergeThread;
    static RawBufferSolveThread[] rawBufferSolveThreadArray=new RawBufferSolveThread[16];
    static int THREAD_NUMBER=4;
    DataPrepareManager() {
        diskRead = new DiskReadThread();
        distributeBuffer = new DistributeBufferThread();
        mergeThread=new HashMapMergeThread();
        ArrayBlockingQueue<ByteBuffer> canuse = new ArrayBlockingQueue<ByteBuffer>(8);
        ArrayBlockingQueue<ByteBuffer> canread = new ArrayBlockingQueue<ByteBuffer>(8);
        ArrayBlockingQueue<BufferWithLatch> unsolvedBuffer = new ArrayBlockingQueue<>(16);
        ArrayBlockingQueue<ByteBuffer> solvedBuffer = new ArrayBlockingQueue<ByteBuffer>(16);
        ArrayBlockingQueue<BufferWithLatch> coutdownQueue = new ArrayBlockingQueue<>(128);
        distributeBuffer.LinkCountDownBuffer(coutdownQueue);
        mergeThread.LinkCountDownBuffer(coutdownQueue);
        diskRead.LinkBlockingQueue(canuse, canread);
        distributeBuffer.LinkDirectBufferBlockingQueue(canuse, canread);
        distributeBuffer.LinkHeapBufferBlockingQueue(unsolvedBuffer, solvedBuffer);
        for(int i=0;i<THREAD_NUMBER;++i){
            rawBufferSolveThreadArray[i]=new RawBufferSolveThread();
            rawBufferSolveThreadArray[i].LinkHeapBufferBlockingQueue(unsolvedBuffer, solvedBuffer);
        }
    }

    public void start(String path, Collection<String> alertRules) {

        int readTimes = diskRead.initChannel(path);
        distributeBuffer.setReadTimes(readTimes);
        diskRead.start();
        distributeBuffer.start();
        for(int i=0;i<THREAD_NUMBER;++i){
            rawBufferSolveThreadArray[i].start();
        }
        mergeThread.start();
    }

    public void stop() {
        try {
            diskRead.join();
            distributeBuffer.join();
            mergeThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
