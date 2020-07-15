package com.kuaishou.kcode.hash;



import com.kuaishou.kcode.KcodeAlertAnalysisImpl;
import com.kuaishou.kcode.TimeRange;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

public class HashAnalyzer {
    public static void anslyze( HashMap<FashHashStringInterface, Collection<String>[]> fastHashMap){
        AtomicInteger minService1Len= new AtomicInteger(99999);
        AtomicInteger minService2Len= new AtomicInteger(99999);
        AtomicInteger service1LenAvg= new AtomicInteger();
        AtomicInteger service2LenAvg= new AtomicInteger();

        fastHashMap.forEach((key,value)->{
            minService1Len.set(Math.min(key.s1.length(), minService1Len.get()));
            minService2Len.set(Math.min(key.s1.length(), minService2Len.get()));
            service1LenAvg.getAndAdd(key.s1.length());
            service2LenAvg.getAndAdd(key.s2.length());
        });
        int avg1=service1LenAvg.get()/fastHashMap.keySet().size();
        int avg2=service2LenAvg.get()/fastHashMap.keySet().size();
        System.out.println("最小长度1="+minService1Len+"最小长度2="+minService2Len);
        System.out.println("平均长度1="+avg1+"最小长度2="+avg2);
        boolean canFront=frontUnique(minService1Len.get(),minService2Len.get(),fastHashMap);
        if(canFront)return;
        TimeRange t=new TimeRange();
        int minUsed=99999;
        int [] bestarg=new int[5];
        for(int front1=minService1Len.get();front1>=0;--front1){
            boolean success=false;
            for(int front2=minService2Len.get();front2>=0;--front2){
                for(int back1=0;back1<=minService1Len.get();++back1){
                    if(success==true)break;
                    for(int back2=0;back2<=minService2Len.get();++back2){
                        if(success==true)break;
                        if(avg1+avg2<(front1+front2+back1+back2)){
                            //无意义 太长了
                            continue;
                        }
                        boolean canFrontAndBack=frontAndBack(front1,front2,back1,back2,fastHashMap);
                        if(canFrontAndBack){
                            System.out.println("找到哈希"+front1+"~"+back1+" "+front2+"~"+back2);
                            t.pointFirst();
                            t.output("fandb找到 耗时=");
                            success=true;
//                    return;
                        }
                    }
                }
            }
            if(success==false){
                break;
            }
        }

        System.out.println("不可哈希优化");



    }
    static boolean frontUnique(int minService1Len,int minService2Len,HashMap<FashHashStringInterface, Collection<String>[]> fastHashMap){
        HashSet<KcodeAlertAnalysisImpl.FastHashString> hashMap2=new HashSet<>();
        fastHashMap.forEach((key,value)->{
            KcodeAlertAnalysisImpl.FastHashString fs1=new KcodeAlertAnalysisImpl.FastHashString();
            String news1=key.s1.substring(0,minService1Len);
            String news2=key.s2.substring(0,minService2Len);
            fs1.fromString(news1,news2);
            hashMap2.add(fs1);
        });
        if(hashMap2.size()==fastHashMap.keySet().size()){
            System.out.println("可以按照前半部分最小长度哈希");
            return true;
        }
        return false;
    }
    static boolean frontAndBack(int minService1Len,int minService2Len,int backlen1,int backlen2,HashMap<FashHashStringInterface, Collection<String>[]> fastHashMap){
        HashSet<KcodeAlertAnalysisImpl.FastHashString> hashMap2=new HashSet<>();
        fastHashMap.forEach((key,value)->{
            KcodeAlertAnalysisImpl.FastHashString fs1=new KcodeAlertAnalysisImpl.FastHashString();
            String news1=key.s1.substring(0,minService1Len);
            String news2=key.s2.substring(0,minService2Len);
            news1+=key.s1.substring(key.s1.length()-backlen1,key.s1.length());
            news2+=key.s2.substring(key.s2.length()-backlen2,key.s2.length());
            fs1.fromString(news1,news2);
            hashMap2.add(fs1);
        });
        if(hashMap2.size()==fastHashMap.keySet().size()){
            System.out.println("可以按照前半部分加后半部分最小长度哈希");
            return true;
        }
        return false;
    }
}
