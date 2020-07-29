package com.kuaishou.kcode.hash;



import com.kuaishou.kcode.KcodeAlertAnalysisImpl;
import com.kuaishou.kcode.TimeRange;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import com.kuaishou.kcode.KcodeAlertAnalysisImpl.HashString;

import com.kuaishou.kcode.KcodeAlertAnalysisImpl.HashString;
import sun.misc.Unsafe;

public class HashAnalyzer {
    public static class fourArray{
        ArrayList<Integer> frontA ;
        ArrayList<Integer> backA ;
        ArrayList<Integer> frontB ;
        ArrayList<Integer> backB ;
        fourArray(ArrayList<Integer> frontA, ArrayList<Integer> backA, ArrayList<Integer> frontB, ArrayList<Integer> backB){
            this.frontA=frontA;
            this.backA=backA;
            this.frontB=frontB;
            this.backB=backB;
        }
    }
    static int THREHOLD =0;

    public static fourArray tryDeletePoint(int[] args, HashMap<HashString, Collection<String>[]> fastHashMap) {
ArrayList<Integer>backup;
        ArrayList<Integer> frontA = new ArrayList<>();
        ArrayList<Integer> backA = new ArrayList<>();
        ArrayList<Integer> frontB = new ArrayList<>();
        ArrayList<Integer> backB = new ArrayList<>();
        int starta = args[4];
        int startb = args[5];
        int front1 = args[0];
        int front2 = args[1];
        int back1 = args[2];
        int back2 = args[3];
        //index=i
        for (int i = starta; i < front1; ++i) {
            frontA.add(i);
        }
        for (int i = startb; i < front2; ++i) {
            frontB.add(i);
        }
        //index=length-i
        for (int i = back1; i > 0; --i) {
            backA.add(i);
        }
        for (int i = back2; i > 0; --i) {
            backB.add(i);
        }
        boolean test1=testPointA(frontA,backA,frontB,backB,fastHashMap);
        System.out.println("基础测试"+test1);


        TimeRange t=new TimeRange();
        //index=length-i
        for (int i = 0; i < backA.size(); ++i) {
            backup= (ArrayList<Integer>) backA.clone();

            int element=backA.remove(i);
            boolean success=testPointA(frontA,backA,frontB,backB,fastHashMap);
            if(!success){
                backA=backup;
            }else{
                System.out.println("删除backA"+element);
            }

        }
        for (int i = 0; i < backB.size(); ++i) {
            backup= (ArrayList<Integer>) backB.clone();

            int element=backB.remove(i);
            boolean success=testPointA(frontA,backA,frontB,backB,fastHashMap);
            if(!success){
                backB=backup;
            }else{
                System.out.println("删除backB"+element);
            }

        }

        for (int i = 0; i < frontA.size(); ++i) {
            backup= (ArrayList<Integer>) frontA.clone();

            int element=frontA.remove(i);
            boolean success=testPointA(frontA,backA,frontB,backB,fastHashMap);
            if(!success){
                frontA=backup;
            }else{
                System.out.println("删除frontA"+element);
            }
        }
        for (int i = 0; i < frontB.size(); ++i) {
            backup= (ArrayList<Integer>) frontB.clone();

            int element=frontB.remove(i);
            boolean success=testPointA(frontA,backA,frontB,backB,fastHashMap);
            if(!success){
                frontB=backup;
            }else{
                System.out.println("删除frontB"+element);
            }
        }
        t.point();
        t.output("测试完成 用时");
        return new fourArray(frontA,backA,frontB,backB);


    }

    static boolean testPointA(ArrayList<Integer> frontA, ArrayList<Integer> backA, ArrayList<Integer> frontB, ArrayList<Integer> backB, HashMap<HashString, Collection<String>[]> fastHashMap) {
        HashSet<Long> hashMap2 = new HashSet<>();
        fastHashMap.forEach((key, value) -> {
            char[] c1 = (char[]) THE_UNSAFE.getObject(key.s1, 12);
            char[] c2 = (char[]) THE_UNSAFE.getObject(key.s2, 12);
            int c1length = c1.length;
            int c2length = c2.length;
            int len1 = frontA.size() + backA.size();
            long hashA = 0;
            for (int i : frontA) {
                hashA = hashA * 31 + c1[i];
            }
            for (int i : backA) {
                hashA = hashA * 31 + c1[c1length - i];
            }
            long hashB = 0;
            for (int i : frontB) {
                hashB = hashB * 31 + c2[i];
            }
            for (int i : backB) {
                hashB = hashB * 31 + c2[c2length - i];
            }
            long hashc=hashA*HashString.powArray[len1]+hashB;
            hashMap2.add(hashc);
        });
        if (hashMap2.size() == fastHashMap.keySet().size()) {
//            System.out.println("可以按照前半部分加后半部分最小长度哈希");
            return true;
        }
        return false;
    }

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

    public static int[] anslyze(HashMap<HashString, Collection<String>[]> fastHashMap) {
        AtomicInteger minService1Len = new AtomicInteger(99999);
        AtomicInteger minService2Len = new AtomicInteger(99999);
        AtomicInteger service1LenAvg = new AtomicInteger();
        AtomicInteger service2LenAvg = new AtomicInteger();

        fastHashMap.forEach((key, value) -> {
            minService1Len.set(Math.min(key.s1.length(), minService1Len.get()));
            minService2Len.set(Math.min(key.s1.length(), minService2Len.get()));
            service1LenAvg.getAndAdd(key.s1.length());
            service2LenAvg.getAndAdd(key.s2.length());
        });
        int avg1 = service1LenAvg.get() / fastHashMap.keySet().size();
        int avg2 = service2LenAvg.get() / fastHashMap.keySet().size();
        System.out.println("最小长度1=" + minService1Len + "最小长度2=" + minService2Len);
        System.out.println("平均长度1=" + avg1 + "最小长度2=" + avg2);
        int minUseda = 99999;
        int[] bestarg = new int[6];


        for (int front1 = minService1Len.get(); front1 >= 0; --front1) {
            boolean success = false;
            for (int front2 = minService2Len.get(); front2 >= 0; --front2) {
                boolean canFront = frontUnique(0, 0, front1, front2, fastHashMap);
                if (!canFront) {
                    break;
                } else {
                    success = true;
                    int cost = front1 + front2;
                    if (cost < minUseda && cost<avg1+avg1) {
                        minUseda = cost;
                        bestarg[0] = front1;
                        bestarg[1] = front2;
                    }
                }
            }
            if (success == false) {
                break;
            }
        }


        if (minUseda < 99999) {
            int front1 = bestarg[0];
            int front2 = bestarg[1];
            for (int starta = 1; starta < front1; ++starta) {
                boolean success = false;
                for (int startb = 1; startb < front2; ++startb) {
                    boolean canFront = frontUnique(starta, startb, front1, front2, fastHashMap);
                    if (!canFront) {
                        break;
                    } else {
                        success = true;
                        int cost = front1 + front2 - starta - startb;
                        if (cost < minUseda && cost<avg1+avg2-THREHOLD) {
                            minUseda = cost;
                            bestarg[4] = starta;
                            bestarg[5] = startb;
                        }
                    }
                }
                if (!success) {
                    break;
                }
            }


            System.out.println("最优参数=" + bestarg[0] + " " + bestarg[1] + " " + bestarg[4] + " " + bestarg[5]);
            return bestarg;
        }
        TimeRange t = new TimeRange();
        int minUsed = 99999;
        for (int front1 = minService1Len.get(); front1 >= 0; --front1) {
            boolean success = false;
            for (int front2 = minService2Len.get(); front2 >= 0; --front2) {
                for (int back1 = 0; back1 <= minService1Len.get(); ++back1) {
                    if (success == true) break;
                    for (int back2 = 0; back2 <= minService2Len.get(); ++back2) {
                        if (success == true) break;
                        if (avg1 + avg2< (front1 + front2 + back1 + back2)) {
                            //无意义 太长了
                            continue;
                        }
                        int tot = front1 + front2 + back1 + back2;

                        boolean canFrontAndBack = frontAndBack(0, 0, front1, front2, back1, back2, fastHashMap);
                        if (canFrontAndBack) {
                            if (tot < minUsed) {
                                minUsed = tot;
                                bestarg[0] = front1;
                                bestarg[1] = front2;
                                bestarg[2] = back1;
                                bestarg[3] = back2;
                            }
//                            System.out.println("找到哈希"+front1+"~"+back1+" "+front2+"~"+back2);
                            t.pointFirst();
//                            t.output("fandb找到 耗时=");
                            success = true;
//                    return;
                        }
                    }
                }
            }
            if (success == false) {
                break;
            }
        }
        if (minUsed == 99999) {


            System.out.println("不可哈希优化");
            return null;
        } else {

            int front1 = bestarg[0];
            int front2 = bestarg[1];
            int back1 = bestarg[2];
            int back2 = bestarg[3];
            for (int starta = 1; starta < front1; ++starta) {
                boolean success = false;
                for (int startb = 1; startb < front2; ++startb) {
                    boolean canFront = frontAndBack(starta, startb, front1, front2, back1, back2, fastHashMap);
                    if (!canFront) {
                        break;
                    } else {
                        success = true;
                        int cost = front1 + front2 + back1 + back2 - starta - startb;
                        if (cost < minUseda && cost<avg1+avg2-THREHOLD ) {
                            minUseda = cost;
                            bestarg[4] = starta;
                            bestarg[5] = startb;
                        }
                    }
                }
                if (!success) {
                    break;
                }
            }
            return bestarg;
        }


    }

    static boolean frontUnique(int start1, int start2, int minService1Len, int minService2Len, HashMap<HashString, Collection<String>[]> fastHashMap) {
        HashSet<Long> hashMap2 = new HashSet<>();
        fastHashMap.forEach((key, value) -> {
            char[] c1 = (char[]) THE_UNSAFE.getObject(key.s1, 12);
            char[] c2 = (char[]) THE_UNSAFE.getObject(key.s2, 12);
            int c1length = c1.length;
            int c2length = c2.length;
            long hashA = 0;
            for (int i=start1;i<minService1Len;++i) {
                hashA = hashA * 31 + c1[i];
            }

            long hashB = 0;
            for (int i=start2;i<minService2Len;++i) {
                hashB = hashB * 31 + c1[i];
            }

            long hashc=hashA*HashString.powArray[c1length]+hashB;
            hashMap2.add(hashc);
        });
        if (hashMap2.size() == fastHashMap.keySet().size()) {
//            System.out.println("可以按照前半部分最小长度哈希");
            return true;
        }
        return false;
    }

    static boolean frontAndBack(int starta, int startb, int minService1Len, int minService2Len, int backlen1, int backlen2, HashMap<HashString, Collection<String>[]> fastHashMap) {
        HashSet<HashString> hashMap2 = new HashSet<>();
        fastHashMap.forEach((key, value) -> {
            HashString fs1 = new HashString();
            String news1 = key.s1.substring(starta, minService1Len);
            String news2 = key.s2.substring(startb, minService2Len);
            news1 += key.s1.substring(key.s1.length() - backlen1, key.s1.length());
            news2 += key.s2.substring(key.s2.length() - backlen2, key.s2.length());
            fs1.fromString(news1, news2);
            hashMap2.add(fs1);
        });
        if (hashMap2.size() == fastHashMap.keySet().size()) {
//            System.out.println("可以按照前半部分加后半部分最小长度哈希");
            return true;
        }
        return false;
    }
}
