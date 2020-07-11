package com.kuaishou.kcode;

import java.nio.ByteBuffer;

public final class ByteString {
    byte[] value;
    int length;
    int middle;
    ByteString() {

    }
    ByteString(String s) {
        length=s.length();
        middle=0;
        try {
            value=Utils.getStringByteArray(s);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    ByteString(String s1,String s2) {
        length=s1.length()+s2.length();
        middle=s1.length();
        value=new byte[length];
        try {
            byte[] value1=Utils.getStringByteArray(s1);
            System.arraycopy(value1,0,value,0,s1.length());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        try {
            byte[] value1=Utils.getStringByteArray(s2);
            System.arraycopy(value1,0,value,s1.length(),s2.length());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param value 装载字符的数组
     * @param length 两个serviceName拼接起来的长度
     * @param middle 第一个serviceName的长度
     */
    ByteString(byte[] value, int length,int middle) {
        this.value = value;
        this.length = length;
        this.middle=middle;
    }
    public ByteString deepClone(){
        byte[] value2=new byte[length];
        System.arraycopy(value,0,value2,0,length);
        return new ByteString(value2,length,middle);
    }
    public int hashCode() {
        int hash = 0;
        for (int a = 0; a < length; ++a) {
            hash = 31 * hash + value[a];
        }
        return hash;
    }

    public boolean equals(Object obj) {
        ByteString bs=(ByteString)obj;
        if(this.length==bs.length&&this.middle==bs.middle){
            //倒着比较
            for(int i=length-1;i>=0;--i){
                if(value[i]!=bs.value[i]){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}
