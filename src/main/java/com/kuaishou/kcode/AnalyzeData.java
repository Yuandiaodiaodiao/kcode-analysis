package com.kuaishou.kcode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class AnalyzeData {
    static HashSet<Integer>timeSet=new HashSet<>();
    public  static void printTimeInfo(){
        System.out.println("time共有"+timeSet.size()+"种");

        for(int t : timeSet){
            System.out.print(" "+t);
        }
        System.out.println();
    }

    static HashMap<Integer,Integer> timecostMap=new HashMap<>();
    public static void printTimeCostMap(){
        int maxT=0;
        for(int i:timecostMap.keySet()){
            maxT=Math.max(i,maxT);
        }
        System.out.println("共有"+timecostMap.keySet().size()+"种 最大值为"+maxT);
    }
    public static String printMemoryInfo(){
        String s=""+Runtime.getRuntime().freeMemory()/1024/1024;
        System.out.println("剩余内存"+s+"MB");
        return s;
    }
}
