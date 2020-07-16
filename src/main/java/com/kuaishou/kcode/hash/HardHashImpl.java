package com.kuaishou.kcode.hash;

import com.kuaishou.kcode.ByteString;
import com.kuaishou.kcode.KcodeAlertAnalysisImpl;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

public class HardHashImpl  implements HardHashInterface{
    public int length = 0;
    public int middle = 0, s2length = 0;
    public String s1 = null, s2 = null;
    public long hashcodelong = 0;
    public int hashint = 0;
    public int bestHash = 1;
    public int HN1 = 0;
    public int HN2 = 0;
    public int HN3 = 0;
    public int HN4 = 0;
    private static final Unsafe THE_UNSAFE;

    static {
        try {
            final PrivilegedExceptionAction<Unsafe> action = new PrivilegedExceptionAction<Unsafe>() {
                public Unsafe run() throws Exception {
                    Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
                    theUnsafe.setAccessible(true);
                    return (Unsafe) theUnsafe.get(null);
                }
            };
            THE_UNSAFE = AccessController.doPrivileged(action);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load unsafe", e);
        }
    }


    public HardHashImpl() {
    }




    public int fromString(String s1, String s2) {
        this.s1 = s1;
        this.s2 = s2;
        if (bestHash == 0) {
            char[] c1 = (char[]) THE_UNSAFE.getObject(s1, 12);
            char[] c2 = (char[]) THE_UNSAFE.getObject(s2, 12);
            hashcodelong=0;
            for (int a = 0; a < HN1; ++a) {
                hashcodelong = c1[a] + hashcodelong * 31;
            }
            middle = c1.length;
            for (int a = middle - HN3; a < middle; ++a) {
                hashcodelong = c1[a] + hashcodelong * 31;
            }
            for (int a = 0; a < HN2; ++a) {
                hashcodelong = c2[a] + hashcodelong * 31;
            }
            s2length = c2.length;
            for (int a = s2length - HN4; a < s2length; ++a) {
                hashcodelong = c2[a] + hashcodelong * 31;
            }
            length = middle + s2length;
            hashint = (int) (hashcodelong % 1000000007);
        } else {
            middle = s1.length();
            s2length = s2.length();
            length = middle + s2length;
            hashint = (int) (hashcodelong % 1000000007);
        }
        return hashint;
    }

    public int hashCode() {
        return hashint;
    }

    public boolean equals(Object obj) {
        return this.hashcodelong == ((KcodeAlertAnalysisImpl.HashString) obj).hashcodelong;
//            return this.hashcodelong == ((HashString) obj).hashcodelong && this.middle == ((HashString) obj).middle;
//        return this.length==fs.length && this.middle == fs.middle &&fs.s1.equals(s1) &&fs.s2.equals(s2);
    }
}
