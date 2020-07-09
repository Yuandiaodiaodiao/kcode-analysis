package com.kuaishou.kcode;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

public class HashMapMergeThread extends Thread{
    ArrayBlockingQueue<BufferWithLatch> bufferQueue;
    public void LinkCountDownBuffer(ArrayBlockingQueue<BufferWithLatch> countDownQueue){
        this.bufferQueue=countDownQueue;
    }

    int firstMinute=-1;
    int solvedMinute=-1;
    RawBufferSolveThread[] threads;
    HashMap<ByteString, HashMap<Long, SingleIpPayload>>[] timeNameIpStore;
    void mergeHashmap(int minute){
        for(int a=0;a<DataPrepareManager.THREAD_NUMBER;++a){
            RawBufferSolveThread t=DataPrepareManager.rawBufferSolveThreadArray[a];
            if(t.timeNameIpStore==null)continue;
            HashMap<ByteString, HashMap<Long, SingleIpPayload>> serviceMap=t.timeNameIpStore[minute-firstMinute];
            if(serviceMap==null)continue;
            HashMap<ByteString, HashMap<Long, SingleIpPayload>> thisMinute=timeNameIpStore[minute-firstMinute];
            serviceMap.forEach((key,value)->{
                thisMinute.merge(key,value,(oldValue,newValue)->{
                    //合并ip集合
                    newValue.forEach((key2,value2)->{
                        oldValue.merge(key2,value2,(oldValue2,newValue2)->{
                            oldValue2.success+=newValue2.success;
                            oldValue2.total+=newValue2.total;
                            for(int i=0;i<300;++i){
                                oldValue2.bucket[i]+=newValue2.bucket[i];
                            }
                            return oldValue2;
                        });
                    });
                    return oldValue;
                });
            });
            //合并好了就可以释放内存
            t.timeNameIpStore[minute-firstMinute]=null;

        }
    }
    @Override
    public void run() {
        super.run();
        while(true){
            try {
                BufferWithLatch bl=bufferQueue.take();
                //等待这个buffer处理完毕
                bl.countdown.await();
                if(firstMinute==-1){

                    firstMinute=DistributeBufferThread.baseMinuteTime;
                    solvedMinute=firstMinute;
                    threads=DataPrepareManager.rawBufferSolveThreadArray;
                }
                if (timeNameIpStore == null) {
                    //初始化线程独有的数据结构 [time][name][ip]
                    timeNameIpStore = new HashMap[64];
                    for (int a = 0; a < 32; ++a) {
                        timeNameIpStore[a] = new HashMap<>(128);
                    }
                }

                int minute=bl.minute;

                for(int i=solvedMinute;i<minute-3;++i){
                    System.out.println("正在处理"+i);
                    mergeHashmap(i);
                    solvedMinute=i+1;
                }
                if(bl.id==-1){
                    break;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
