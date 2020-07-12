package com.kuaishou.kcode;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;

public class Utils {
    static Field byteBufferArrayField;
    static {
        try {
            byteBufferArrayField = ByteBuffer.allocate(1).getClass().getSuperclass().getDeclaredField("hb");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        byteBufferArrayField.setAccessible(true);
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
    public static char[] getStringByteArray(String s)  {
        try {
            return (char[])stringField.get(s);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return sss;
    }
    public static String setStringByteArray(byte[] bs,int length){
        String s= "";
        char[] c=new char[length];
        for(int a=0;a<length;++a){
            c[a]=(char)bs[a];
        }
        try {
            stringField.set(s,c);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return s;
    }
    public static byte[] getHeapByteBufferArray(ByteBuffer b) throws IllegalAccessException {
            return (byte[]) byteBufferArrayField.get(b);
    }

    static int getFirstTime(ByteBuffer buffer) {
        byte[] byteArray = null;
        try {
            byteArray = Utils.getHeapByteBufferArray(buffer);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        int position = buffer.position()-1;
        //逗号,分隔
        while ((byteArray[++position]) != 44) {
        }
        while ((byteArray[++position]) != 44) {
        }
        while ((byteArray[++position]) != 44) {
        }
        while ((byteArray[++position]) != 44) {
        }
        int success = (byteArray[++position] == 116 ? 0 : 1);
        position += 4 + success;
        while ((byteArray[++position]) != 44) {
        }


        ++position;
        int timeEndPos = position + 9;
        int secondTime = 0;
        for (int timepos = position; timepos <= timeEndPos; ++timepos) {
            secondTime = (byteArray[timepos] - 48) + secondTime * 10;
        }
        //毫秒时间戳 去掉后3位数字 是秒级时间戳 /60后是分钟时间戳
        int minTime = secondTime / 60;
        return minTime;
    }
    static void firstIp2StringBuilder(long ipTwo,StringBuilder sb){
        long ip1 = (ipTwo >>> 32);
        sb.append((int) ((ip1 >> 24) & 0x000000FF));
        sb.append('.');
        sb.append((int) ((ip1 >> 16) & 0x000000FF));
        sb.append('.');
        sb.append((int) ((ip1 >> 8) & 0x000000FF));
        sb.append('.');
        sb.append((int) (ip1 & 0x000000FF));
    }
    static void secondIp2StringBuilder(long ipTwo,StringBuilder sb){
        int ip2 = (int) (long) (ipTwo);
        sb.append((int) ((ip2 >> 24) & 0x000000FF));
        sb.append('.');
        sb.append((int) ((ip2 >> 16) & 0x000000FF));
        sb.append('.');
        sb.append((int) ((ip2 >> 8) & 0x000000FF));
        sb.append('.');
        sb.append((int) (ip2 & 0x000000FF));
    }
    static void getAnswer1Type(ArrayList<String> as){



        HashSet<Integer>set=new HashSet<>();
        for(String s:as){
            String[] ss=s.split(",");
            set.add(Integer.parseInt(ss[0]));
        }
        for (Integer integer : set) {
//            System.out.println("报警"+integer);
        }
    }

}
