package com.kuaishou.kcode;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

public class DistributeBufferThread extends Thread{
    private ArrayBlockingQueue<ByteBuffer> canuse;
    private ArrayBlockingQueue<ByteBuffer> canread;
    private ArrayBlockingQueue<BufferWithLatch> unsolvedBuffer;
    private ArrayBlockingQueue<ByteBuffer> solvedBuffer;
    private ArrayBlockingQueue<BufferWithLatch> countDownQueue;
    private static final int CHUNCK_SIZE = DataPrepareManager.DIRECT_CHUNK_SIZE;
     static int lastBufferNumbers=DataPrepareManager.THREAD_NUMBER+1;
    DistributeBufferThread(){
        baseMinuteTime=-1;
        lastMinuteTime=-1;
    }
    public void LinkDirectBufferBlockingQueue(ArrayBlockingQueue<ByteBuffer> canuse,ArrayBlockingQueue<ByteBuffer> canread){
        this.canuse=canuse;
        this.canread=canread;
    }
    public void LinkHeapBufferBlockingQueue(ArrayBlockingQueue<BufferWithLatch> unsolvedBuffer,ArrayBlockingQueue<ByteBuffer> solvedBuffer){
        this.unsolvedBuffer=unsolvedBuffer;
        this.solvedBuffer=solvedBuffer;
    }
    public void LinkCountDownBuffer(ArrayBlockingQueue<BufferWithLatch> countDownQueue){
        this.countDownQueue=countDownQueue;
    }
    static volatile int baseMinuteTime=-1;

    int lastMinuteTime=-1;
    volatile ArrayList<BufferWithLatch> latchArray=new ArrayList<>();
    @Override
    public void run() {
        super.run();
        try {
            int bufferId=0;
            while(true) {
                ByteBuffer buf =canread.take();
                //拷贝 并传送buf给子任务
                if(buf.limit()==0){
                    canread.clear();
                    break;
                }
                ByteBuffer bufOutput=null;
                if(solvedBuffer.size()==0 && lastBufferNumbers-->0){
                    //没有可用 并且内存可分配
//                    System.out.println("分配内存");
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
                //首次处理的时候要读入一次time 得到基准时间戳
                if(baseMinuteTime==-1){
                    //因为时间戳+-1浮动 所以取最早可能时间作为基准
                    baseMinuteTime=Utils.getFirstTime(bufOutput)-3;
                    lastMinuteTime=baseMinuteTime;
                    System.out.println("基准time="+baseMinuteTime);
                }
                int thisMinuteTime=Utils.getFirstTime(bufOutput);
                lastMinuteTime=thisMinuteTime;


                CountDownLatch countdown=new CountDownLatch(1);
                countDownQueue.offer(new BufferWithLatch(countdown,bufferId,thisMinuteTime));

                latchArray.add(new BufferWithLatch(countdown,bufferId,thisMinuteTime));
                //将含有数据的buffer扔给任务队列
                unsolvedBuffer.offer(new BufferWithLatch(bufOutput,countdown,bufferId));
                bufferId++;
            }
            //要让每个线程都coutdown一下
            CountDownLatch countdown=new CountDownLatch(1);
            countDownQueue.offer(new BufferWithLatch(countdown,-1,lastMinuteTime+7));
            latchArray.add(new BufferWithLatch(countdown,-1,lastMinuteTime+7));
            //通过id==-1结束rawbaffer处理线程
            unsolvedBuffer.offer(new BufferWithLatch(countdown,-1,-1));


        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
