package com.kuaishou.kcode.hash;

import com.kuaishou.kcode.ByteString;
import com.kuaishou.kcode.KcodeAlertAnalysisImpl;

public interface HardHashInterface {




    public static long[] powArray = new long[128];







    public void fromString(String s1, String s2);

    public int hashCode();

    public boolean equals(Object obj);
}
