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
//        System.gc();
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
    public static int timeArray[][] = new int[100][13];

    static {
        int y = 2020;
        int y2 = 2020;
        for (int M = 1; M <= 12; ++M) {
            int y3 = y;
            int m2 = M;
            m2 -= 2;
            y3 -= m2 <= 0 ? 1 : 0;
            m2 += m2 <= 0 ? 12 : 0;
            int day = y3 / 4 - y3 / 100 + y3 / 400 + 367 * m2 / 12 + y3 * 365 - 719499;
            timeArray[2020 - y2][M] = day * 24 * 60 - 480;
        }
    }
    HashMap<ByteString, DAGPrepare.AnswerStructure> Q2Answer;
    @Override
    public Collection<String> getLongestPath(String caller, String responder, String time, String type) {
//        try {
//            ch=(char[])stringField.get(time);
//        } catch (IllegalAccessException e) {
//
//        }

        int y = 55348 - time.charAt(0) * 1000 - time.charAt(1) * 100 - time.charAt(2) * 10 - time.charAt(3);
        int M = time.charAt(5) * 10 + time.charAt(6) - 528;
        int t = timeArray[y][M] + time.charAt(8) * 14400 + time.charAt(9) * 1440 - 792528 + time.charAt(11) * 600 + time.charAt(12) * 60 + time.charAt(14) * 10 + time.charAt(15) - firstMinute;
        t = (t < 0 || t > maxMinute) ? maxMinute + 1 : t;

        bs.fromString(caller, responder);

        return Q2Answer.get(bs).ansArray[type.charAt(0)-'P'][t];
//                    an=type.charAt(0) == 'S'?ans.SRArray[t]:ans.P99Array[t];
//        return an;

    }
}