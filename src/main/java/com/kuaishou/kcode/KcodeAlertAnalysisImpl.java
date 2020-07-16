package com.kuaishou.kcode;

import com.kuaishou.kcode.compiler.CompilerTest;
import com.kuaishou.kcode.hash.*;
import jdk.nashorn.internal.objects.NativeInt8Array;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
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

    public static class HashString {

        public int length;
        public int middle, s2length;
        public String s1, s2;
        public static long[] powArray;
        public long hashcodelong;
        public int hashint;
        public int bestHash = 1;
        public int HN1;
        public int HN2;
        public int HN3;
        public int HN4;

        static {
            powArray = new long[128];
            powArray[0] = 1;
            for (int a = 1; a < 128; ++a) {
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
            if (bestHash == 0) {
                char[] c1 = (char[]) THE_UNSAFE.getObject(s1, 12);
                char[] c2 = (char[]) THE_UNSAFE.getObject(s2, 12);
                hashcodelong = 0;
                for (int a = 0; a < HN1; ++a) {
                    hashcodelong = c1[a] + hashcodelong * 31;
                }
                middle = c1.length;
                for (int a = middle - HN3; a < middle; ++a) {
                    hashcodelong = c1[a] + hashcodelong * 31;
                }
                for (int a = 0; a < HN2; ++a) {
                    hashcodelong = c2[a] + hashcodelong * 31;
                }
                s2length = c2.length;
                for (int a = s2length - HN4; a < s2length; ++a) {
                    hashcodelong = c2[a] + hashcodelong * 31;
                }
                length = middle + s2length;
                hashint = (int) (hashcodelong % 1000000007);
            } else {
                middle = s1.length();
                s2length = s2.length();
                length = middle + s2length;
                hashcodelong = s1.hashCode() * powArray[s2length] + s2.hashCode();
                hashint = (int) (hashcodelong % 1000000007);
            }

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

        try {
            Field f = String.class.getDeclaredField("value");
            long offset = THE_UNSAFE.objectFieldOffset(f);
            String aaa = "12345678";
            int a = THE_UNSAFE.getInt(aaa, offset);
            char ccc = THE_UNSAFE.getChar(aaa, offset);
            char cccd = THE_UNSAFE.getChar(aaa, offset + 1);
            byte cc = THE_UNSAFE.getByte(aaa, offset);
            int b = THE_UNSAFE.getInt(aaa, offset + 4);
            int c = 1;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        manager = new DataPrepareManager();
    }

    HashMap<HashString, Collection<String>[]> fastHashMap;
    FastHashMap<HashString, Collection<String>[]> fasterHashMap;
    private static final Unsafe THE_UNSAFE;

    static {
        try {
            final PrivilegedExceptionAction<Unsafe> action = new PrivilegedExceptionAction<Unsafe>() {
                public Unsafe run() throws Exception {
                    Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
                    theUnsafe.setAccessible(true);
                    return (Unsafe) theUnsafe.get(null);
                }
            };
            THE_UNSAFE = AccessController.doPrivileged(action);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load unsafe", e);
        }
    }

    FastHashMap<HardHashInterface, Collection<String>[]> fastestHashMap;
    Class<?> finalClass;
    FastHashMap<HardHashInterface, Collection<String>> strAndTimeHashMap;
    Collection<String>[]bucket;
    int mod;
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
//        AnalyzeData.printMemoryInfo();
//
//        fasterHashMap = new FastHashMap<>(20000);
//        AnalyzeData.printMemoryInfo();
//        fasterHashMap.mod = 20000;
//        HashClassGenerator.test();
        fs = new HashString();
        Q2Answer.forEach((key, value) -> {
//            FastHashString newkey = new FastHashString();
//            newkey.fromByteString(key);
//            FashHashStringInterface newkey2 = HashClassGenerator.getInstance(key);
            HashString newkey2 = new HashString();
            newkey2.fromByteString(key);
            int timeIndex = maxMinute - firstMinute;


            Field f = null;
            try {
                f = String.class.getDeclaredField("value");
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
//            long offset=THE_UNSAFE.objectFieldOffset(f);
//            long offset2=THE_UNSAFE.arrayBaseOffset(char[].class);
//            String aaa=newkey2.s1;
//            char[] c=(char[])THE_UNSAFE.getObject(aaa,12);
//            int a=THE_UNSAFE.getInt(c,offset2);
//            char ccc=THE_UNSAFE.getChar(c,offset2);
//            char cccd=THE_UNSAFE.getChar(c,offset2);
//            byte cc=THE_UNSAFE.getByte(c,offset2);
//            int b=THE_UNSAFE.getInt(c,offset2+4);

            Collection<String>[] ansArray = new ArrayList[(timeIndex + 2) * 2];
            fastHashMap.put(newkey2, ansArray);

//            fasterHashMap.put(newkey2, ansArray);
            for (int i = 0; i < timeIndex + 2; ++i) {
                ansArray[i] = value.SRArray[i];
            }
            for (int i = timeIndex + 2, j = 0; i < (timeIndex + 2) * 2; ++i, ++j) {
                ansArray[i] = value.P99Array[j];
            }
        });
        int[] bestHash = HashAnalyzer.anslyze(fastHashMap);
        if(bestHash!=null){
        }
        boolean canBestHash = false;
        if (bestHash != null) {
            canBestHash = true;
        }
        if (canBestHash) {
            fs.bestHash = 0;
            fs.HN1 = bestHash[0];
            fs.HN2 = bestHash[1];
            fs.HN3 = bestHash[2];
            fs.HN4 = bestHash[3];
        }
        //重新hash
        boolean finalCanBestHash = canBestHash;
//        if (finalCanBestHash) {
//            fasterHashMap.clear();
//            fastHashMap.forEach((key, value) -> {
//                HashString hs = new HashString();
//                if (finalCanBestHash) {
//                    hs.bestHash = 0;
//                    hs.HN1 = bestHash[0];
//                    hs.HN2 = bestHash[1];
//                    hs.HN3 = bestHash[2];
//                    hs.HN4 = bestHash[3];
//                }
//                hs.fromString(key.s1, key.s2);
//                fasterHashMap.put(hs, value);
//            });
//        }
        TimeRange doClash = new TimeRange();
        int lastMod = 0;
//        while (fasterHashMap.getHashClash() != 0 && lastMod != fasterHashMap.mod) {
//            fasterHashMap.clear();
//            lastMod = fasterHashMap.mod;
//            fasterHashMap.remodbig();
////            System.out.println("mod="+fasterHashMap.mod);
//            fastHashMap.forEach((key, value) -> {
//                if (finalCanBestHash) {
//                    HashString hs = new HashString();
//                    hs.bestHash = 0;
//                    hs.HN1 = bestHash[0];
//                    hs.HN2 = bestHash[1];
//                    hs.HN3 = bestHash[2];
//                    hs.HN4 = bestHash[3];
//                    hs.fromString(key.s1, key.s2);
//                    fasterHashMap.put(hs, value);
//                } else {
//                    fasterHashMap.put(key, value);
//                }
//
//            });
////            System.out.println("remode"+fasterHashMap.mod);
//        }


        doClash.point();
        doClash.output("解决哈希冲突");
//        System.out.println("哈希冲突=" + fasterHashMap.getHashClash() + "/" + fastHashMap.size());
//        fasterHashMap.prepareReady();
        int mod = 20000;
        fasterHashMap = null;
        finalClass = HashClassGenerator.generateHashCoder(bestHash,fastHashMap);
        ffs = HashClassGenerator.getInstance();

        fastestHashMap = new FastHashMap<>(64 * 1024 * 1024);
        fastestHashMap.mod = 20000;
        fastHashMap.forEach((key, value) -> {
            HardHashInterface newKey = HashClassGenerator.getInstance(key.s1, key.s2);
            fastestHashMap.put(newKey, value);
        });
        doClash = new TimeRange();
        lastMod = 0;
        while (fastestHashMap.getHashClash() != 0 && lastMod != fastestHashMap.mod) {
            fastestHashMap.clear();
            lastMod = fastestHashMap.mod;
            fastestHashMap.remodbig();
//            System.out.println("mod="+fasterHashMap.mod);
            fastHashMap.forEach((key, value) -> {
                HardHashInterface newKey = HashClassGenerator.getInstance(key.s1, key.s2);
                fastestHashMap.put(newKey, value);

            });
//            System.out.println("remode"+fasterHashMap.mod);
        }
        doClash.point();
        doClash.output("解决哈希冲突用时");
        fastestHashMap.prepareReady();
        System.out.println("哈希冲突=" + fastestHashMap.getHashClash() + "/" + fastHashMap.size() + "mod=" + fastestHashMap.mod);


//        fastestHashMap.clear();
        mod=fastestHashMap.mod;
        this.mod=fastestHashMap.mod;
        fastestHashMap.clear();
        fastestHashMap=null;



//        strAndTimeHashMap=new FastHashMap<>(512 * 1024 * 1024);
//        strAndTimeHashMap.mod=mod;
//
//        fastHashMap.forEach((key, value) -> {
//            HardHashInterface newKey = HashClassGenerator.getInstance();
//            int hash=newKey.fromString(key.s1, key.s2);
//
//            int timeIndex = maxMinute - firstMinute;
//            for(int i=0;i<timeIndex+2;++i){
//                int timebit=i;
//                //sr
//                int typebit=2&1;
//                int hashi=(timebit<<1)+typebit;
//                strAndTimeHashMap.put8bit(newKey, value[i],hash,hashi);
//            }
//            for(int i=timeIndex+2,j=0;i<(timeIndex+2)*2;++i,++j){
//                int timebit=j;
//                //p99
//                int typebit=3&1;
//                int hashi=(timebit<<1)+typebit;
//                strAndTimeHashMap.put8bit(newKey, value[i],hash,hashi);
//            }
//        });
//        System.out.println("strAndTimeHashMap=" + strAndTimeHashMap.getHashClash() + "/" + fastHashMap.size() + "mod=" + strAndTimeHashMap.mod);
//
//
//        strAndTimeHashMap.clear();
//        strAndTimeHashMap=null;

        for(int i=1;i<30;++i){
            if(mod<(1<<i)-1){
                mod=(1<<i)-1;
                break;
            }
        }

        bucket=new Collection[mod<<8+1];
        int finalMod = mod;
        this.mod=finalMod;
        fastHashMap.forEach((key, value) -> {
            HardHashInterface newKey = HashClassGenerator.getInstance();
            int hash=newKey.fromString(key.s1, key.s2);

            int timeIndex = maxMinute - firstMinute;
            for(int i=0;i<timeIndex+2;++i){
                int timebit=i;
                //sr
                int typebit=2&1;
                int hashi=timebit+(typebit<<7);
                int abshash =getFinalHash(hash,hashi);
                bucket[abshash]=value[i];
//                strAndTimeHashMap.put8bit(newKey, value[i],hash,hashi);
            }
            for(int i=timeIndex+2,j=0;i<(timeIndex+2)*2;++i,++j){
                int timebit=j;
                //p99
                int typebit=3&1;
                int hashi=timebit+(typebit<<7);
//                strAndTimeHashMap.put8bit(newKey, value[i],hash,hashi);
                int abshash =getFinalHash(hash,hashi);
                bucket[abshash]=value[i];
            }
        });
        TimeRange theat = new TimeRange();
        System.gc();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String starttimeFormat = format.format(new Date(firstMinute * 60000L));
        int y = 55348 - starttimeFormat.charAt(0) * 1000 - starttimeFormat.charAt(1) * 100 - starttimeFormat.charAt(2) * 10 - starttimeFormat.charAt(3);
        int M = starttimeFormat.charAt(5) * 10 + starttimeFormat.charAt(6) - 528;
        int t = timeArray[y][M] + starttimeFormat.charAt(8) * 14400 + starttimeFormat.charAt(9) * 1440 - 792528 - firstMinute;
        prepareTime = t;
        timeIndex = maxMinute - firstMinute;
//预热
        System.out.println("开始预热");
        if (true) {
            final int[] heatTimes = {1499};
            String timeFormat = format.format(new Date((maxMinute - firstMinute) / 2 * 60000L));
            fastHashMap.forEach((key, value) -> {
                Collection<String> s;
                while (heatTimes[0] > 0) {
                    heatTimes[0]--;
//                    s=getLongestPath2(key.s1, key.s2, timeFormat, "P");
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


//        Utils.getAnswer1Type(ans);

//        if (DistributeBufferThread.baseMinuteTime > 0) {
//            String s=AnalyzeData.printMemoryInfo();
//
//            throw new ArrayIndexOutOfBoundsException("RAM" + s + "耗时" + t1.firstTime() + "R=" + alertRules.size() + "K=" + manager.getServicePairNum() + "A=" + ans.size()+"M="+mod);
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

    HardHashInterface ffs;
    HashString fs;
    HashMap<ByteString, DAGPrepare.AnswerStructure> Q2Answer;
    public static long tttt;
    public static long tttt2;
    int timeIndex;

    public int getTime(String time) {
//        int t12 = time.charAt(12);
//        int t14 = time.charAt(14);
        int t = prepareTime + time.charAt(11) * 600 + time.charAt(12) * 60 + time.charAt(14) * 10 + time.charAt(15);
        t = (t < 0 || t > timeIndex) ? timeIndex + 1 : t;
        return t;
    }
    public int getTypeHash(String type,String time){
        int timebit=getTime(time);
        int typebit=(type.length() & 1);
        typebit=(timebit)+(typebit<<7);
        return typebit;
    }
    public int getFinalHash(int hash1,int hash2){
        return ((((hash1)^(hash1>>>16))&mod)<<8)+(hash2);
    }
    @Override
    public Collection<String> getLongestPath(String caller, String responder, String time, String type) {
        int hashi=ffs.fromString(caller, responder);
        //time最大126来计算 是7bit
        //0是SR 1是p99
        int typebit=getTypeHash(type,time);
        int index=getFinalHash(hashi,typebit);
        return bucket[index];
    }

    public Collection<String> getLongestPath3(String caller, String responder, String time, String type) {
        int hashi=ffs.fromString(caller, responder);
        //time最大126来计算 是7bit
        //0是SR 1是p99
        int typebit=getTypeHash(type,time);
        return strAndTimeHashMap.get8bit(hashi,typebit);
    }

    public Collection<String> getLongestPath2(String caller, String responder, String time, String type) {
        fs.fromString(caller, responder);
        return fasterHashMap.get(fs)[(type.length() & 1) * (timeIndex + 2) + getTime(time)];
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