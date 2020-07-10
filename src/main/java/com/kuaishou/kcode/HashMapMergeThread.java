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
                            newValue2.bucket=null;
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
    static int maxBucket=0;
    int solveP99(byte[] bucket,int allNum){
        double i=0.99*allNum;
        int p99= (int) Math.ceil(i);
        int bucketIndex=bucket.length;
        while(--bucketIndex>=0){
//            maxBucket=Math.max(bucket[bucketIndex],maxBucket);
            allNum-=bucket[bucketIndex];
            if(allNum<p99){
                return bucketIndex;
            }
        }
        return 0;
    }
    void SolveMinuteP99AndSR(int minute){
        HashMap<ByteString, HashMap<Long, SingleIpPayload>> thisMinute=timeNameIpStore[minute-firstMinute];
        if(thisMinute==null)return;
        thisMinute.forEach((key,value)->{
            value.forEach((key2,value2)->{
                SRAndP99Payload payload=new SRAndP99Payload(value2);
                payload.p99= solveP99(payload.bucket,payload.total);
                payload.rate=((double) payload.success)/payload.total;
                //释放bucket内存
                payload.bucket=null;
                value.put(key2,payload);
            });
        });
//        System.out.println("最大bucker="+maxBucket);
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
//                    System.out.println("正在处理"+i);
                    //合并数据
                    mergeHashmap(i);
                    //进行桶排 处理p99和sr
                    SolveMinuteP99AndSR(i);
                    solvedMinute=i+1;
                }
                if(bl.id==-1){
                    //退出 表示处理完成
                    break;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
