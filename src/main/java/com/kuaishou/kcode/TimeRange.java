package com.kuaishou.kcode;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class TimeRange {
    ArrayList<Long>timeArray=new ArrayList<>();
    public TimeRange(){
        timeArray.add(System.nanoTime());
    }
    public synchronized void pointsync(){timeArray.add(System.nanoTime());}
    public void point(){
        timeArray.add(System.nanoTime());
    }
    public void pointFirst(){
        if(timeArray.size()==1){
            timeArray.add(System.nanoTime());

        }else{
            timeArray.set(1,System.nanoTime());
        }
    }
    public void outputus(){
        for(int i=1;i<timeArray.size();++i){
            System.out.print(" "+Math.round((timeArray.get(i)-timeArray.get(i-1))*1.0/1000));
        }
        System.out.println();
    }
    public void outputns(){
        for(int i=1;i<timeArray.size();++i){
            System.out.print(" "+Math.round((timeArray.get(i)-timeArray.get(i-1))));
        }
        System.out.println();
    }
    public void output(){
        for(int i=1;i<timeArray.size();++i){
            System.out.print(" "+Math.round((timeArray.get(i)-timeArray.get(i-1))*1.0/1000000));
        }
        System.out.println();
    }
    public int firstTime(){
        return (int)Math.round((timeArray.get(1)-timeArray.get(0))*1.0/1000000);
    }

    public void output(String str){
        System.out.print(str);
        output();
    }
}
