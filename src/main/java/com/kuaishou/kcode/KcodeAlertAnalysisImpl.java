package com.kuaishou.kcode;

import com.kuaishou.kcode.compiler.CompilerTest;
import com.kuaishou.kcode.hash.FashHashStringInterface;
import com.kuaishou.kcode.hash.HashAnalyzer;
import com.kuaishou.kcode.hash.HashClassGenerator;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

/**
 * @author KCODE
 * Created on 2020-07-04
 */


public class KcodeAlertAnalysisImpl implements KcodeAlertAnalysis {
    private DataPrepareManager manager;
    static class HashString {

        public int length ;
        public int middle , s2length ;
        public String s1 , s2 ;
        public static long[] powArray;
        public long hashcodelong;
        public int hashint;
        static {
            powArray = new long[256];
            powArray[0] = 1;
            for (int a = 1; a <= 200; ++a) {
                powArray[a] = powArray[a - 1] * 31;
            }
        }

        public HashString() {
        }


        public HashString fromByteString(ByteString bs) {
            StringBuilder sb = new StringBuilder();
            for (int a = bs.offset; a < bs.middle; ++a) {
                sb.append((char) bs.value[a]);
            }
            s1 = sb.toString();
            sb.setLength(0);
            for (int a = bs.middle; a < bs.length; ++a) {
                sb.append((char) bs.value[a]);
            }
            s2 = sb.toString();
            fromString(s1, s2);
            return this;
        }


        public void fromString(String s1, String s2) {
            this.s1 = s1;
            this.s2 = s2;
            middle = s1.length();
            s2length = s2.length();
            length = middle + s2length;
            doHash();
        }

        public void doHash() {
            hashcodelong = s1.hashCode() * powArray[s2length] + s2.hashCode();
            hashint=(int) (hashcodelong % 1000000007);
        }

        public int hashCode() {
            return hashint;
        }

        public boolean equals(Object obj) {
            return this.hashcodelong == ((HashString) obj).hashcodelong;
//            return this.hashcodelong == ((HashString) obj).hashcodelong && this.middle == ((HashString) obj).middle;
//        return this.length==fs.length && this.middle == fs.middle &&fs.s1.equals(s1) &&fs.s2.equals(s2);
        }

    }
    public static class FastHashString extends FashHashStringInterface {


        static {
            powArray = new long[256];
            powArray[0] = 1;
            for (int a = 1; a <= 200; ++a) {
                powArray[a] = powArray[a - 1] * 31;
            }
        }

        public FastHashString() {
        }


        public FastHashString fromByteString(ByteString bs) {
            StringBuilder sb = new StringBuilder();
            for (int a = bs.offset; a < bs.middle; ++a) {
                sb.append((char) bs.value[a]);
            }
            s1 = sb.toString();
            sb.setLength(0);
            for (int a = bs.middle; a < bs.length; ++a) {
                sb.append((char) bs.value[a]);
            }
            s2 = sb.toString();
            fromString(s1, s2);
            return this;
        }

        public long hashcodelong;

        public void fromString(String s1, String s2) {
            this.s1 = s1;
            this.s2 = s2;
            middle = s1.length();
            s2length = s2.length();
            length = middle + s2length;
            doHash();
        }

        public void doHash() {
            hashcodelong = s1.hashCode() * powArray[s2length] + s2.hashCode();
        }

        public int hashCode() {
            return (int) (hashcodelong % 1000000007);
        }

        public boolean equals(Object obj) {
            FastHashString fs = (FastHashString) obj;
            return this.hashcodelong == fs.hashcodelong && this.middle == fs.middle;
//        return this.length==fs.length && this.middle == fs.middle &&fs.s1.equals(s1) &&fs.s2.equals(s2);
        }

    }

    public KcodeAlertAnalysisImpl() {
        manager = new DataPrepareManager();
    }

    HashMap<HashString, Collection<String>[]> fastHashMap;
    FastHashMap<HashString, Collection<String>[]> fasterHashMap;

    @Override
    public Collection<String> alarmMonitor(String path, Collection<String> alertRules) {
        System.gc();
        TimeRange t1 = new TimeRange();
        manager.start(path, alertRules);
        manager.stop();

        ArrayList<String> ans = manager.getAnswer1();
        TimeRange t2 = new TimeRange();

        t1.point();
        firstMinute = manager.mergeThread.firstMinute;

//        try {
//            Thread.sleep(1000*70);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        boolean dr=manager.diskRead.isAlive();
//        boolean db=manager.distributeBuffer.isAlive();
//        boolean mr=manager.mergeThread.isAlive();
//
//        int rbAlive=0;
//        for (int i = 0; i < DataPrepareManager.THREAD_NUMBER; ++i) {
//            if(DataPrepareManager.rawBufferSolveThreadArray[i].isAlive()){
//                rbAlive++;
//            }
//        }
//        String sx="DR"+(dr?1:0)+"DB"+(db?1:0)+"MR"+(mr?1:0)+"rb"+rbAlive;
//        ArrayList<BufferWithLatch> bl=manager.distributeBuffer.latchArray;
//        for(BufferWithLatch b:bl){
//            if(b.countdown.getCount()>0){
//                sx+="m"+(b.minute-firstMinute)+"id"+(b.id);
//                break;
//            }
//        }
//        if (DistributeBufferThread.baseMinuteTime > 0) {
//            throw new IndexOutOfBoundsException(sx);
//        }
        manager.prepareQ2();

        t2.point();
        firstMinute = manager.mergeThread.firstMinute;
        maxMinute = manager.mergeThread.maxMinute;
        System.out.println("time个数=" + (maxMinute - firstMinute));
        Q2Answer = manager.Q2Answer;

        fastHashMap = new HashMap<>(4096 * 1024);
        AnalyzeData.printMemoryInfo();

        fasterHashMap = new FastHashMap<>(64 * 1024 * 1024);
        AnalyzeData.printMemoryInfo();
        fasterHashMap.mod = 10;
        HashClassGenerator.test();
        fs =new HashString();
        Q2Answer.forEach((key, value) -> {
//            FastHashString newkey = new FastHashString();
//            newkey.fromByteString(key);
//            FashHashStringInterface newkey2 = HashClassGenerator.getInstance(key);
            HashString newkey2=new HashString();
            newkey2.fromByteString(key);
            int timeIndex = maxMinute - firstMinute;

            Collection<String>[] ansArray = new ArrayList[(timeIndex + 2) * 2];
            fastHashMap.put(newkey2, ansArray);

            fasterHashMap.put(newkey2, ansArray);
            for (int i = 0; i < timeIndex + 2; ++i) {
                ansArray[i] = value.P99Array[i];
            }
            for (int i = timeIndex + 2, j = 0; i < (timeIndex + 2) * 2; ++i, ++j) {
                ansArray[i] = value.SRArray[j];
            }
        });
//        HashAnalyzer.anslyze(fastHashMap);
        TimeRange doClash = new TimeRange();
        doClash.pointFirst();
        while (fasterHashMap.getHashClash() != 0 && doClash.firstTime() < 1000) {
            fasterHashMap.clear();
            fasterHashMap.remodbig();
            fastHashMap.forEach((key, value) -> {
                fasterHashMap.put(key, value);
            });
//            System.out.println("remode"+fasterHashMap.mod);
            doClash.pointFirst();
        }
        doClash.point();
        doClash.output("解决哈希冲突");
        System.out.println(doClash.firstTime());
        System.out.println("哈希冲突=" + fasterHashMap.getHashClash() + "/" + fastHashMap.size());
        fasterHashMap.prepareReady();
        TimeRange theat = new TimeRange();
        System.gc();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String starttimeFormat = format.format(new Date(firstMinute * 60000L));
        int y = 55348 - starttimeFormat.charAt(0) * 1000 - starttimeFormat.charAt(1) * 100 - starttimeFormat.charAt(2) * 10 - starttimeFormat.charAt(3);
        int M = starttimeFormat.charAt(5) * 10 + starttimeFormat.charAt(6) - 528;
        int t = timeArray[y][M] + starttimeFormat.charAt(8) * 14400 + starttimeFormat.charAt(9) * 1440 - 792528 - firstMinute;
        prepareTime = t;
        timeIndex = maxMinute - firstMinute;

        if (true) {
            final int[] heatTimes = {100000};
            String timeFormat = format.format(new Date((maxMinute - firstMinute) / 2 * 60000L));
            fastHashMap.forEach((key, value) -> {
                Collection<String> s;
                while (heatTimes[0] >0){
                    heatTimes[0]--;
//                    int tt=getTime(timeFormat);
//                    s=realgetLongestPath(key.s1, key.s2, timeFormat, "P");
//                    s=realgetLongestPath(key.s1, key.s2, timeFormat, "S");
                    s = getLongestPath(key.s1, key.s2, timeFormat, "P");
                    s = getLongestPath(key.s1, key.s2, timeFormat, "S");
                }
            });
        }


        theat.point();
        theat.output("预热耗时");
        t2.point();
//        t2.output("Q2耗时");
        t1.point();
        t1.output("read 耗时");

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
    int prepareTime;

    static {
        try {
            stringField = String.class.getDeclaredField("value");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        stringField.setAccessible(true);
    }

    static char[] sss;
    ByteString bs = new ByteString(128);
    ArrayList<String> NOANSWER = new ArrayList<>();
    int firstMinute;
    int maxMinute;
    char[] ch;
    public static int timeArray[][] = new int[100][13];

    static {
        for (int year = 1970; year <= 2020; ++year) {
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


    HashString fs;
    HashMap<ByteString, DAGPrepare.AnswerStructure> Q2Answer;
    public static long tttt;
    public static long tttt2;
    int timeIndex;

    public int getTime(String time) {
//        int t12 = time.charAt(12);
//        int t14 = time.charAt(14);
        int t = prepareTime + time.charAt(11) * 600 + time.charAt(12)*60 + time.charAt(14)*10 + time.charAt(15);
        t = (t < 0 || t > timeIndex) ? timeIndex + 1 : t;
        return t;
    }

    public Collection<String> realgetLongestPath(String caller, String responder, String time, String type) {
        fs.fromString(caller, responder);
        int t=getTime(time);
        return fasterHashMap.get(fs)[((type.charAt(0) - 'P') >> 1) * (timeIndex + 2) + t];
    }

    @Override
    public Collection<String> getLongestPath(String caller, String responder, String time, String type) {
        fs.fromString(caller, responder);
        return fasterHashMap.get(fs)[((type.charAt(0) - 'P') >> 1) * (timeIndex + 2) + getTime(time)];
//        return realgetLongestPath(caller, responder, time, type);
//        try {
//            ch=(char[])stringField.get(time);
//        } catch (IllegalAccessException e) {
//
//        }
//        int y = 55348 - time.charAt(0) * 1000 - time.charAt(1) * 100 - time.charAt(2) * 10 - time.charAt(3);
//        int M = time.charAt(5) * 10 + time.charAt(6) - 528;
//        int t = timeArray[y][M] + time.charAt(8) * 14400 + time.charAt(9) * 1440 - 792528  - firstMinute;
//        int t12 = time.charAt(12);
//        int t14 = time.charAt(14);
//
//        int t = prepareTime + time.charAt(11) * 600 + (t12 << 6) - (t12 << 2) + (t14 << 3) + (t14 << 1) + time.charAt(15);
//        t = (t < 0 || t > timeIndex) ? timeIndex + 1 : t;
//
//        fs.fromString(caller, responder);
//        bs.fromString(caller, responder);
//        System.out.println("index="+);
//        System.out.println("pos="+pos+" "+type.charAt(0));

//        return fasterHashMap.get(fs)[((type.charAt(0) - 'P') >> 1) * (timeIndex + 2) + t];
//        return fasterHashMap.get(fs)[((type.charAt(0) - 'P') >> 1) * (timeIndex + 2) + t];
//        return Q2Answer.get(bs).ansArray[(type.charAt(0)-'P')][t];
//                    an=type.charAt(0) == 'S'?ans.SRArray[t]:ans.P99Array[t];
//        return an;

    }
}