package com.kuaishou.kcode;

import java.nio.ByteBuffer;

public final class ByteString {
    byte[] value;
    int length;
    public ByteString DeepClone(){
        ByteString s=new ByteString();
        s.value=new byte[length];
        System.arraycopy(value,0,s.value,0,length);
        s.length=length;
        return s;
    }
    ByteString() {

    }

    ByteString(byte[] value, int length) {
        this.value = value;
        this.length = length;
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
        if(this.length==bs.length){
            for(int i=0;i<length;++i){
                if(value[i]!=bs.value[i]){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}
