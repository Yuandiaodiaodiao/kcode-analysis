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
import java.util.*;

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
    static SimpleDateFormat DATA_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public KcodeAlertAnalysisImpl() {
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
    Collection<String>[] bucket;
    int mod;
    int hashState=0;
    @Override
    public Collection<String> alarmMonitor(String path, Collection<String> alertRules) {
        System.gc();
        TimeRange t1 = new TimeRange();
        manager.start(path, alertRules);
        manager.stop();
        t1.point();
        t1.output("read 耗时");
        ArrayList<String> ans = manager.getAnswer1();

        TimeRange t2 = new TimeRange();
        firstMinute = manager.mergeThread.firstMinute;
        maxMinute = manager.mergeThread.maxMinute;
        System.out.println("time个数=" + (maxMinute - firstMinute));
        Q2Answer=manager.prepareQ2();
        t2.point();

        fastHashMap = new HashMap<>(4096 * 1024);

        Q2Answer.forEach((key, value) -> {
            HashString newkey2 = new HashString();
            newkey2.fromByteString(key);
            int timeIndex = maxMinute - firstMinute;

            Collection<String>[] ansArray = new ArrayList[(timeIndex + 2) * 2];
            fastHashMap.put(newkey2, ansArray);
            for (int i = 0; i < timeIndex + 2; ++i) {
                ansArray[i] = value.SRArray[i];
            }
            for (int i = timeIndex + 2, j = 0; i < (timeIndex + 2) * 2; ++i, ++j) {
                ansArray[i] = value.P99Array[j];
            }
        });
        int[] bestHash = HashAnalyzer.anslyze(fastHashMap);
        bestHash=null;
        boolean canBestHash = false;
        if (bestHash != null) {
            canBestHash = true;
        }
        TimeRange doClash = new TimeRange();
        int lastMod = 0;
        doClash.point();
        doClash.output("解决哈希冲突");

        if(canBestHash){
            this.hashState=1;
        }else{
            //原生哈希
            this.hashState=0;
        }

        int mod = 20000;
        fasterHashMap = null;
        finalClass = HashClassGenerator.generateHashCoder(bestHash, fastHashMap);
        ffs = HashClassGenerator.getInstance();


        //重新hash
        boolean finalCanBestHash = canBestHash;


        fastestHashMap = new FastHashMap<>(64 * 1024 * 1024);
        fastestHashMap.mod = 4096-1;

        doClash = new TimeRange();
        lastMod = 0;
        fastestHashMap.clashNum++;
        while (fastestHashMap.getHashClash() != 0 && lastMod != fastestHashMap.mod) {
            fastestHashMap.clear();
            lastMod = fastestHashMap.mod;
            fastestHashMap.remodbig();
//            System.out.println("mod="+fasterHashMap.mod);
            fastHashMap.forEach((key, value) -> {
                if(this.hashState==0){
                    int hash = getStringHash(key.s1, key.s2);
                    hash=hash&fastestHashMap.mod;
                    fastestHashMap.put(ffs, value, hash);
                }else{
                    int hash = ffs.fromString(key.s1, key.s2);
                    hash=hash&fastestHashMap.mod;
                    fastestHashMap.put(ffs, value, hash);
                }

            });
//            System.out.println("remode"+fasterHashMap.mod);
        }
        doClash.point();
        doClash.output("解决哈希冲突用时");
        fastestHashMap.prepareReady();
        System.out.println("哈希冲突=" + fastestHashMap.getHashClash() + "/" + fastHashMap.size() + "mod=" + fastestHashMap.mod);


//        fastestHashMap.clear();
        mod = fastestHashMap.mod;
        this.mod = fastestHashMap.mod;
        fastestHashMap.clear();
        fastestHashMap = null;


        for (int i = 1; i < 30; ++i) {
            if (mod < (1 << i) - 1) {
                mod = (1 << i) - 1;
                break;
            }
        }


        bucket = new Collection[mod << 8 + 1];
        int finalMod = mod;
        this.mod = finalMod;
        fastHashMap.forEach((key, value) -> {
            HardHashInterface newKey = HashClassGenerator.getInstance();
            int hash = newKey.fromString(key.s1, key.s2);
            if(this.hashState==0){
                //原生哈希
             hash=getStringHash(key.s1,key.s2);
            }
            int timeIndex = maxMinute - firstMinute;
            for (int i = 0; i < timeIndex + 2; ++i) {
                int timebit = i;
                //sr
                int typebit = 2 & 1;
                int hashi = typebit + (timebit << 1);
                int abshash = getFinalHash(hash, hashi);
                bucket[abshash] = value[i];
//                strAndTimeHashMap.put8bit(newKey, value[i],hash,hashi);
            }
            for (int i = timeIndex + 2, j = 0; i < (timeIndex + 2) * 2; ++i, ++j) {
                int timebit = j;
                //p99
                int typebit = 3 & 1;
                int hashi = typebit + (timebit << 1);
//                strAndTimeHashMap.put8bit(newKey, value[i],hash,hashi);
                int abshash = getFinalHash(hash, hashi);
                bucket[abshash] = value[i];
            }
        });
        this.mod = mod;
        TimeRange theat = new TimeRange();
        System.gc();

        prepareTime = solvePrepareTime(firstMinute);
        timeIndex = maxMinute - firstMinute;
//预热
        System.out.println("hashstate="+this.hashState);
        System.out.println("开始预热");
        if (true) {
            final int[] heatTimes = {1499000};
            String timeFormat = DATA_FORMAT.format(new Date((maxMinute + firstMinute) / 2 * 60000L));
            fastHashMap.forEach((key, value) -> {
                Collection<String> s;
                while (heatTimes[0] > 0) {
                    heatTimes[0]--;
                    s = getLongestPath(key.s1, key.s2, timeFormat, "P");
                    s = getLongestPath(key.s1, key.s2, timeFormat, "S");
                }
            });
        }


        theat.point();
        theat.output("预热耗时");



//        Utils.getAnswer1Type(ans);

//        if (DistributeBufferThread.baseMinuteTime > 0) {
//            String s=AnalyzeData.printMemoryInfo();
//
//            throw new ArrayIndexOutOfBoundsException("RAM" + s + "耗时" + t1.firstTime() + "R=" + alertRules.size() + "K=" + manager.getServicePairNum() + "A=" + ans.size()+"M="+this.mod);
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
    HashMap<ByteString, DAGPrepare.AnswerStructure> Q2Answer;

    int timeIndex;
    public int solvePrepareTime(int firstMinute){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String starttimeFormat = format.format(new Date(firstMinute * 60000L));
        int y = 55348 - starttimeFormat.charAt(0) * 1000 - starttimeFormat.charAt(1) * 100 - starttimeFormat.charAt(2) * 10 - starttimeFormat.charAt(3);
        int M = starttimeFormat.charAt(5) * 10 + starttimeFormat.charAt(6) - 528;
        int t = timeArray[y][M] + starttimeFormat.charAt(8) * 14400 + starttimeFormat.charAt(9) * 1440 - 792528+starttimeFormat.charAt(11)*600 - firstMinute;
        return t;
    }

    public int getTime(String time) {
        char[] c1 = (char[]) THE_UNSAFE.getObject(time, 12);
        return prepareTime + c1[12] * 60 + c1[14] * 10 + c1[15];
    }

    public int getTypeHash(String type, String time) {
        return (getTime(time)<<1) + ((type.length() & 1));
    }

    public int getFinalHash(int hash1, int hash2) {
        return ((((hash1)) & mod) << 8) + (hash2);
    }

    public int getStringHash(String caller, String responder) {
        int hashcodeA=caller.hashCode();
        return (hashcodeA<<5)-hashcodeA + responder.hashCode();
    }
    @Override
    public Collection<String> getLongestPath(String caller, String responder, String time, String type) {
        int hashi=getStringHash(caller,responder);
        int typebit = getTypeHash(type, time);
        int index = getFinalHash(hashi, typebit);
        return bucket[index];
    }


}