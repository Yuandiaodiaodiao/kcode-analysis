package com.kuaishou.kcode;

import java.lang.reflect.Field;
import java.util.ArrayList;


public class HashString {

    char[] value = new char[256];
    int length = 0;
    Field f;
    int hashcode1;
    long hashcode2;

    public HashString() {
        try {
            f = String.class.getDeclaredField("value");
        } catch (Exception e) {
            e.printStackTrace();
        }
        f.setAccessible(true);
    }

    public void add3(String c, String b, String a) {
        int len_a = a.length();
        int len_b = b.length();
        int len_c = c.length();

        try {
            char[] aa = (char[]) f.get(a);
            char[] bb = (char[]) f.get(b);
            char[] cc = (char[]) f.get(c);
            System.arraycopy(aa, 0, value, 0, len_a);
            System.arraycopy(bb, 0, value, len_a, len_b);
            System.arraycopy(cc, 0, value, len_a + len_b, len_c);
            this.length = len_a + len_b + len_c;
        } catch (Exception e) {
            e.printStackTrace();
        }


        long ret = 0;
        for (int i = 0; i < length; i++) {
            ret = ret * 129 + value[i];
        }
        hashcode2 = ret;
        hashcode1 = (int) (ret % 1000000007);

    }


    @Override
    public int hashCode() {
        return hashcode1;
    }

    @Override
    public boolean equals(Object obj) {
//        if (getClass() != obj.getClass()) return false;
//        HashString hs = (HashString) obj;
//        if (this.length != hs.length) return false;
//        for (int i = 0; i <= this.length; i++) {
//            if (this.value[i] != hs.value[i]) return false;
//        }
//        return true;
        return hashcode2 == ((HashString) obj).hashcode2;
    }
}
