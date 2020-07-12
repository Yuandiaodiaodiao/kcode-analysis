package com.kuaishou.kcode;

import java.nio.ByteBuffer;

public final class ByteString {
    byte[] value;
    int length;
    int middle;
    int offset = 0;
    public String toString(){
        StringBuilder sb=new StringBuilder();
        for (int a = offset; a < length; ++a) {
            sb.append((char)value[a]);
        }
        return sb.toString();
    }
    void appendStringBuilder(StringBuilder sb){
        for (int a = offset; a < length; ++a) {
            sb.append((char)value[a]);
        }
    }

    ByteString() {

    }

    ByteString first() {
        return new ByteString(value, middle, 0);
    }

    ByteString second() {
        return new ByteString(value, length, 0, middle);
    }

    ByteString(int cap){
        value=new byte[cap];
    }
    void fromString(String s1, String s2) {
        length = s1.length() + s2.length();
        middle = s1.length();
        for (int i = 0; i < middle; ++i) {
            value[i] = (byte) s1.charAt(i);
        }
        for (int i = middle; i < length; ++i) {
            value[i] = (byte) s2.charAt(i - middle);
        }
    }
    ByteString(String s) {
        length = s.length();
        middle = 0;
        value = s.getBytes();

//        try {
//            value=Utils.getStringByteArray(s);
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
    }

    ByteString(String s1, String s2) {
        length = s1.length() + s2.length();
        middle = s1.length();
        value = new byte[length];
        for (int i = 0; i < middle; ++i) {
            value[i] = (byte) s1.charAt(i);
        }
        for (int i = middle; i < length; ++i) {
            value[i] = (byte) s2.charAt(i - middle);
        }

//        try {
//            byte[] value1=Utils.getStringByteArray(s1);
//            System.arraycopy(value1,0,value,0,s1.length());
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
//        try {
//            byte[] value1=Utils.getStringByteArray(s2);
//            System.arraycopy(value1,0,value,s1.length(),s2.length());
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * @param value  装载字符的数组
     * @param length 两个serviceName拼接起来的长度
     * @param middle 第一个serviceName的长度
     */
    ByteString(byte[] value, int length, int middle) {
        this.value = value;
        this.length = length;
        this.middle = middle;
    }

    ByteString(byte[] value, int length, int middle, int offset) {
        this.value = value;
        this.length = length;
        this.middle = middle;
        this.offset = offset;
    }

    public ByteString deepClone() {
        byte[] value2 = new byte[length];
        System.arraycopy(value, offset, value2, 0, length);
        return new ByteString(value2, length, middle,offset);
    }

    public int hashCode() {
        int hash = 0;
        for (int a = offset; a < length; ++a) {
            hash = 31 * hash + value[a];
        }
        return hash;
    }

    public boolean equals(Object obj) {
        ByteString bs = (ByteString) obj;
        if (this.length-offset == bs.length-bs.offset && this.middle == bs.middle) {
            //倒着比较
            for (int i = length-1,j=bs.length-1; i >= offset; --i,--j) {
                if (value[i] != bs.value[j]) {
                    return false;
                }
            }


            return true;
        }
        return false;
    }

}
