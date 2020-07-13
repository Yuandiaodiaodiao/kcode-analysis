package com.kuaishou.kcode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * @author KCODE
 * Created on 2020-07-04
 */
class FastHashString{
    int length;
    int middle;
    String s1,s2;
    static long[]powArray=new long[256];
    static{
        powArray[0]=1;
        for(int a=1;a<=200;++a){
            powArray[a]=powArray[a-1]*31;
        }
    }
    FastHashString(){}
    FastHashString(ByteString bs){
        StringBuilder sb=new StringBuilder();
        for (int a = bs.offset; a < bs.middle; ++a) {
            sb.append((char)bs.value[a]);
        }
        s1=sb.toString();
        sb.setLength(0);
        for (int a = bs.middle; a < bs.length; ++a) {
            sb.append((char)bs.value[a]);
        }
        s2=sb.toString();
        fromString(s1,s2);
    }
    long hashcodelong;
    void fromString(String s1, String s2) {
        this.s1=s1;
        this.s2=s2;
        middle = s1.length();
        length=middle+s2.length();
        hashcodelong=s1.hashCode()*powArray[length-middle]+s2.hashCode();
    }

    public int hashCode() {
        return (int) (hashcodelong%1000000007);
    }

    public boolean equals(Object obj) {
        FastHashString fs = (FastHashString) obj;
        return this.hashcodelong==fs.hashcodelong;
//        return this.length==fs.length && this.middle == fs.middle &&fs.s1.equals(s1) &&fs.s2.equals(s2);
    }

}

public class KcodeAlertAnalysisImpl implements KcodeAlertAnalysis {
    private DataPrepareManager manager;

    public KcodeAlertAnalysisImpl() {
        manager = new DataPrepareManager();
    }
    HashMap<FastHashString,Collection<String>[]> fastHashMap;
    @Override
    public Collection<String> alarmMonitor(String path, Collection<String> alertRules) {
        System.gc();

        TimeRange t1 = new TimeRange();
        manager.start(path, alertRules);
        manager.stop();

        ArrayList<String> ans = manager.getAnswer1();
        TimeRange t2 = new TimeRange();

        manager.prepareQ2();
        t2.point();
        firstMinute = manager.mergeThread.firstMinute;
        maxMinute = manager.mergeThread.maxMinute;
        Q2Answer=manager.Q2Answer;
        fastHashMap=new HashMap<>(4096);

        Q2Answer.forEach((key,value)->{
            FastHashString newkey=new FastHashString(key);
            int timeIndex=maxMinute-firstMinute;
            Collection<String>[] ansArray= new ArrayList[(timeIndex+2)*2];
            fastHashMap.put(newkey,ansArray);
            for(int i=0;i<timeIndex+2;++i){
                ansArray[i]=value.P99Array[i];
            }
            for(int i=timeIndex+2,j=0;i<(timeIndex+2)*2;++i,++j){
                ansArray[i]=value.SRArray[j];
            }
        });
        t2.point();
        t2.output("Q2耗时");
        t1.point();
        t1.output("read 耗时");
        System.gc();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
        for(int year=1970;year<=2020;++year){
            int y = year;
            int y2 = year;
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

    }
    FastHashString fs=new FastHashString();
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
        int timeIndex=maxMinute-firstMinute;
        t = (t < 0 || t > maxMinute) ? timeIndex + 1 : t;
        fs.fromString(caller,responder);
//        bs.fromString(caller, responder);
//        System.out.println("index="+);
//        System.out.println("pos="+pos+" "+type.charAt(0));
        return fastHashMap.get(fs)[((type.charAt(0)-'P')>>1)*(timeIndex+2) +t];
//        return Q2Answer.get(bs).ansArray[(type.charAt(0)-'P')][t];
//                    an=type.charAt(0) == 'S'?ans.SRArray[t]:ans.P99Array[t];
//        return an;

    }
}