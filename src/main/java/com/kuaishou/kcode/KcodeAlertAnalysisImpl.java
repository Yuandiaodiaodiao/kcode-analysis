package com.kuaishou.kcode;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author KCODE
 * Created on 2020-07-04
 */
public class KcodeAlertAnalysisImpl implements KcodeAlertAnalysis {
    private DataPrepareManager manager;
    public KcodeAlertAnalysisImpl(){
        manager=new DataPrepareManager();
    }
    @Override
    public Collection<String> alarmMonitor(String path, Collection<String> alertRules) {

        TimeRange t1=new TimeRange();
        manager.start(path,alertRules);
        manager.stop();

        ArrayList<String>ans=manager.getAnswer1();
        manager.prepareQ2();
        t1.point();
        t1.output("read 耗时");
        System.gc();
        String s=AnalyzeData.printMemoryInfo();
//        Utils.getAnswer1Type(ans);


//        if(DistributeBufferThread.baseMinuteTime>0){
//            throw new ArrayIndexOutOfBoundsException("RAM"+s+"耗时"+t1.firstTime()+"R="+alertRules.size()+"K="+manager.getServicePairNum()+"A="+ans.size()    );
//        }
        return ans;
    }
    ByteString bs=new ByteString(128);
    ArrayList<String>NOANSWER=new ArrayList<>();
    @Override
    public Collection<String> getLongestPath(String caller, String responder, String time, String type) {
        bs.fromString(caller,responder);
        DAGPrepare.AnswerStructure ans=manager.Q2Answer.get(bs);
        if(ans!=null){
            System.out.println(caller+responder+" 有"+ans.ansNum+"种");
            for(ByteString s1:ans.s1){
                for(ByteString s2:ans.s2){
                    System.out.println(s1.toString()+s2.toString());
                }
            }
        }
        return NOANSWER;
    }
}