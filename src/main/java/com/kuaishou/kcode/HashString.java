package com.kuaishou.kcode;

import java.lang.reflect.Field;
import java.util.ArrayList;


public class HashString {

    char[] value = new char[256];
    int length = 0;
    Field f;

    public HashString() {
        try {
            f = String.class.getDeclaredField("value");
        } catch (Exception e) {
            e.printStackTrace();
        }
        f.setAccessible(true);
    }

    public void add3(String c, String b, String a) {
        this.length = 0;
        int len_a = a.length();
        int len_b = b.length();
        int len_c = c.length();

        try {
            char[] aa = (char[]) f.get(a);
            char[] bb = (char[]) f.get(b);
            char[] cc = (char[]) f.get(c);
            for (int i = 0; i < len_a; i++) value[this.length++] = aa[i];
            for (int i = 0; i < len_b; i++) value[this.length++] = bb[i];
            for (int i = 0; i < len_c; i++) value[this.length++] = cc[i];
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    @Override
    public int hashCode() {
        int ret = 0;
        for (int i = 0; i < length; i++) {
            ret = ret * 31 + value[i];
        }
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass()) return false;
        HashString hs = (HashString) obj;
        if (this.length != hs.length) return false;
        for (int i = 0; i <= this.length / 2; i++) {
            if (this.value[i] != hs.value[i]) return false;
            if (this.value[this.length - i - 1] != hs.value[this.length - i - 1]) return false;
        }
        return true;
    }
}
