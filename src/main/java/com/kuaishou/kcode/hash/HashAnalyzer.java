package com.kuaishou.kcode.hash;



import com.kuaishou.kcode.KcodeAlertAnalysisImpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class HashAnalyzer {
    public static void anslyze( HashMap<KcodeAlertAnalysisImpl.FastHashString, Collection<String>[]> fastHashMap){
        AtomicInteger minService1Len= new AtomicInteger(99999);
        AtomicInteger minService2Len= new AtomicInteger(99999);
        fastHashMap.forEach((key,value)->{
            minService1Len.set(Math.min(key.s1.length(), minService1Len.get()));
            minService2Len.set(Math.min(key.s1.length(), minService2Len.get()));
        });
        System.out.println("最小长度1="+minService1Len+"最小长度2="+minService2Len);
        HashMap<KcodeAlertAnalysisImpl.FastHashString, Collection<String>[]> hashMap2=new HashMap<>();
        fastHashMap.forEach((key,value)->{
            KcodeAlertAnalysisImpl.FastHashString fs1=new KcodeAlertAnalysisImpl.FastHashString();
            fs1.fromString(key.s1.substring(0,minService1Len.get()),key.s2.substring(0,minService2Len.get()));

            hashMap2.put(fs1,value);
        });
        if(hashMap2.keySet().size()==fastHashMap.keySet().size()){
            System.out.println("可以按照最小长度哈希");
        }

    }
}
