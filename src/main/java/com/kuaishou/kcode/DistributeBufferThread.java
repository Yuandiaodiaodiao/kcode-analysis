package com.kuaishou.kcode;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

public class DistributeBufferThread extends Thread{
    private ArrayBlockingQueue<ByteBuffer> canuse;
    private ArrayBlockingQueue<ByteBuffer> canread;
    private ArrayBlockingQueue<ByteBuffer> unsolvedBuffer;
    private ArrayBlockingQueue<ByteBuffer> solvedBuffer;
    private static final int CHUNCK_SIZE = DataPrepareManager.DIRECT_CHUNK_SIZE;
    private int readTimes;
    private int lastBufferNumbers;
    DistributeBufferThread(){
    }
    public void setReadTimes(int readTimes){
        this.readTimes=readTimes;
        lastBufferNumbers=DataPrepareManager.THREAD_NUMBER+1;
    }
    public void LinkDirectBufferBlockingQueue(ArrayBlockingQueue<ByteBuffer> canuse,ArrayBlockingQueue<ByteBuffer> canread){
        this.canuse=canuse;
        this.canread=canread;
    }
    public void LinkHeapBufferBlockingQueue(ArrayBlockingQueue<ByteBuffer> unsolvedBuffer,ArrayBlockingQueue<ByteBuffer> solvedBuffer){
        this.unsolvedBuffer=unsolvedBuffer;
        this.solvedBuffer=solvedBuffer;
    }

    @Override
    public void run() {
        super.run();
        try {

            for (long i = 0; i <readTimes; ++i) {
                ByteBuffer buf =canread.take();
                //拷贝 并传送buf给子任务
                ByteBuffer bufOutput=null;
                if(solvedBuffer.size()==0 && lastBufferNumbers-->0){
                    //没有可用 并且内存可分配
                    bufOutput=ByteBuffer.allocate(CHUNCK_SIZE);
                }else{
                    //从可用队列中取一个
                    bufOutput=solvedBuffer.take();
                }
                bufOutput.clear();

                //进行拷贝 拷贝使用了unsafe.copyMemory 比较高效
                bufOutput.put(buf);
                bufOutput.flip();
                //拷贝完成 归还directBuffer
                canuse.offer(buf);
                //将含有数据的buffer扔给任务队列
                unsolvedBuffer.offer(bufOutput);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
