package com.kuaishou.kcode;

public class MulTist {
    public static void main(String[] args){

        int b=0;
        int c=151364610;
        int d=545346515;
        int e=465456465;
        int f=264556454;
        b=mula(b,c,d,e);
        b=mula(b,c,d,e);
        b=mula(b,c,d,e);
        b=mulb(b,c,d,e);
        b=mulb(b,c,d,e);
        b=mulb(b,c,d,e);


        System.out.println("开始测试!");

        long t1=System.nanoTime();
       b=mula(b,c,d,e);
    long t2=System.nanoTime();
       long t3=System.nanoTime();
        b=mulb(b,c,d,e);
        long t4=System.nanoTime();
        System.out.println("*31 耗时"+(t2-t1));
        System.out.println("乘法 耗时"+(t4-t3));
        System.out.println(b);
    }
    static int mula(int b,int c, int d, int e){
        for(int a=0;a<=100000000;++a){
            b=((((b*31+a)*31+c)*31+d)*31+e);
        }
        return b;
    }
    static int mulb(int b,int c, int d, int e){
        for(int a=0;a<=100000000;++a){
            b=b*a+a*c+a*d+a*e+b;
        }
        return b;
    }
}
