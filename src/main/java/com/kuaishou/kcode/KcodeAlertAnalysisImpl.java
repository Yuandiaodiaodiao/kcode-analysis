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
        firstMinute=manager.
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
    int firstMinute;
    @Override
    public Collection<String> getLongestPath(String caller, String responder, String time, String type) {
        char[]ch=Utils.getStringByteArray(time);
        int y = ch[0] * 1000 + ch[1] * 100 + ch[2] * 10 + ch[3] - '0' * 1111;
        int M = ch[5] * 10 + ch[6] - '0' * 11;
        int d = ch[8] * 10 + ch[9] - '0' * 11;
        int H = ch[11] * 600 + ch[12]*60 - 31680;
        int m = ch[14] * 10 + ch[15] - '0' * 11;
        M -= 2;
        y -= M <= 0 ? 1 : 0;
        M += M <= 0 ? 12 : 0;
        int day = y / 4 - y / 100 + y / 400 + 367 * M / 12 + d + y * 365 - 719499;
        int t = day * 1440 + H -480 + m - firstMinute;
        if(t<0){
            return NOANSWER;
        }
        bs.fromString(caller,responder);
        DAGPrepare.AnswerStructure ans=manager.Q2Answer.get(bs);
        if(ans!=null){
            if(type.charAt(0)=='S'){
                //SR
                return ans.SRArray[t];
            }else{
                //P99
                return ans.P99Array[t];
            }
//            System.out.println(caller+responder+" 有"+ans.ansNum+"种");
//            for(ByteString s1:ans.s1){
//                for(ByteString s2:ans.s2){
//                    System.out.println(s1.toString()+s2.toString());
//                }
//            }
        }
        return NOANSWER;
    }
}