package com.kuaishou.kcode;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

public class BufferWithLatch {
    int id;
    int minute;
    ByteBuffer buffer;
    CountDownLatch countdown;
    BufferWithLatch(ByteBuffer buffer){
        this.buffer=buffer;
    }
    BufferWithLatch(CountDownLatch countdown,int id,int minute){
        this.countdown=countdown;
        this.id=id;
        this.minute=minute;
    }
    BufferWithLatch(ByteBuffer buffer,CountDownLatch countdown,int id){
        this.buffer=buffer;
        this.countdown=countdown;
        this.id=id;
    }
}
