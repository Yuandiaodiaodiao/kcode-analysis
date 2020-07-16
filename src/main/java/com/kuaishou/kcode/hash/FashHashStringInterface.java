package com.kuaishou.kcode.hash;

import com.kuaishou.kcode.ByteString;
import com.kuaishou.kcode.KcodeAlertAnalysisImpl;

public abstract class FashHashStringInterface {

    public int length ;
    public int middle , s2length ;
    public String s1 , s2 ;
    public static long[] powArray;


    public FashHashStringInterface fromByteString(ByteString bs) {

        return null;
    }

    public long hashcodelong;

    public void fromString(String s1, String s2) {
    }

    public void doHash() {

    }

    public int hashCode() {
        return 0;
    }

    public boolean equals(Object obj) {
        return false;
    }
}
