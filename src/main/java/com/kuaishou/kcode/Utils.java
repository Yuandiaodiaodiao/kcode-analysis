package com.kuaishou.kcode;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

public class Utils {
    static Field byteBufferArrayField;
    static Field stringField;
    static {
        try {
            byteBufferArrayField = ByteBuffer.allocate(1).getClass().getSuperclass().getDeclaredField("hb");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        byteBufferArrayField.setAccessible(true);
    }
    static {
        try {
            stringField = String.class.getDeclaredField("value");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        stringField.setAccessible(true);
    }
    public static byte[] getStringByteArray(String s) throws IllegalAccessException {
        return (byte[])stringField.get(s);
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

}
