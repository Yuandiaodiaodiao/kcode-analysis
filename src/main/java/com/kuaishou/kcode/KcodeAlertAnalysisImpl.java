package com.kuaishou.kcode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * @author KCODE
 * Created on 2020-07-04
 */
public class KcodeAlertAnalysisImpl implements KcodeAlertAnalysis {
    private DataPrepareManager manager;

    public KcodeAlertAnalysisImpl() {
        manager = new DataPrepareManager();
    }

    @Override
    public Collection<String> alarmMonitor(String path, Collection<String> alertRules) {

        TimeRange t1 = new TimeRange();
        manager.start(path, alertRules);
        manager.stop();

        ArrayList<String> ans = manager.getAnswer1();
        manager.prepareQ2();
        firstMinute = manager.mergeThread.firstMinute;
        maxMinute = manager.mergeThread.maxMinute;
        Q2Answer=manager.Q2Answer;
        t1.point();
        t1.output("read 耗时");
        System.gc();
//        String s=AnalyzeData.printMemoryInfo();
//        Utils.getAnswer1Type(ans);


//        if (DistributeBufferThread.baseMinuteTime > 0) {
//            throw new ArrayIndexOutOfBoundsException("RAM" + s + "耗时" + t1.firstTime() + "R=" + alertRules.size() + "K=" + manager.getServicePairNum() + "A=" + ans.size());
//        }
        return ans;
    }
    static Field stringField;

    static {
        try {
            stringField = String.class.getDeclaredField("value");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        stringField.setAccessible(true);
    }
    static char[]sss;
    ByteString bs = new ByteString(128);
    ArrayList<String> NOANSWER = new ArrayList<>();
    int firstMinute;
    int maxMinute;
    char[] ch;
    HashMap<ByteString, DAGPrepare.AnswerStructure> Q2Answer;
    @Override
    public Collection<String> getLongestPath(String caller, String responder, String time, String type) {
        TimeRange tt=new TimeRange();
//        try {
//            ch=(char[])stringField.get(time);
//        } catch (IllegalAccessException e) {
//
//        }
        tt.point();
        int y = time.charAt(0) * 1000 + time.charAt(1) * 100 + time.charAt(2) * 10 + time.charAt(3) - 53328;
        int M = time.charAt(5) * 10 + time.charAt(6) - 530;
        int d = time.charAt(8) * 10 + time.charAt(9) - 528;
        int H = time.charAt(11) * 600 + time.charAt(12) * 60 - 31680;
        int m = time.charAt(14) * 10 + time.charAt(15) - 528;
        tt.point();
        y -= M <= 0 ? 1 : 0;
        M += M <= 0 ? 12 : 0;
        int day = y / 4 - y / 100 + y / 400 + 367 * M / 12 + d + y * 365 - 719499;
        int t = day * 1440 + H - 480 + m - firstMinute;
        tt.point();
        t = (t < 0 || t > maxMinute) ? maxMinute + 1 : t;
        tt.point();
        bs.fromString(caller, responder);
        tt.point();
        DAGPrepare.AnswerStructure ans = Q2Answer.get(bs);
        tt.point();
        Collection<String> an=type.charAt(0) == 'S'?ans.SRArray[t]:ans.P99Array[t];
        tt.point();
        tt.outputns();
        System.out.println("比较"+bs.cmpTimes);
        return an;

    }
}