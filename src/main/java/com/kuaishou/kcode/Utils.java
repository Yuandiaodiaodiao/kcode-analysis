package com.kuaishou.kcode;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

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
    public static byte[] getHeapByteBufferArray(ByteBuffer b) throws IllegalAccessException {
            return (byte[]) byteBufferArrayField.get(b);
    }

}
