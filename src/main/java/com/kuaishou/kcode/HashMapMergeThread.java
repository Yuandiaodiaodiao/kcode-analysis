package com.kuaishou.kcode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

public class HashMapMergeThread extends Thread {
    ArrayBlockingQueue<BufferWithLatch> bufferQueue;

    public void LinkCountDownBuffer(ArrayBlockingQueue<BufferWithLatch> countDownQueue) {
        this.bufferQueue = countDownQueue;
    }

    AlertRulesPrepare.RuleMaps ruleMaps;

    public void setRuleMaps(AlertRulesPrepare.RuleMaps ruleMaps) {
        this.ruleMaps = ruleMaps;
    }

    static class RuleState {
        int warningTimes = 0;
        int lastCheckTime = -1;
    }

    static class RuleStatePayload {
        RuleStatePayload(SingleIpPayload payload, int timeIndex) {
            this.payload = new SRAndP99Payload[64];
        }

        SRAndP99Payload[] payload;
        ArrayList<RuleState> states;
    }

    static class RuleIpPayload {
        RuleIpPayload() {
            this.ipHashMap = new HashMap<>();
        }

        ArrayList<AlertRulesPrepare.Rule> rules;
        HashMap<Long, RuleStatePayload> ipHashMap;
    }

    HashMap<ByteString, RuleIpPayload> serviceMapWithRule;
    HashMap<ByteString, RuleIpPayload> serviceMapAll;
    int firstMinute = -1;
    int solvedMinute = -1;
    RawBufferSolveThread[] threads;
    HashMap<ByteString, HashMap<Long, SingleIpPayload>>[] timeNameIpStore;

    void doWarning(int minute) {
        HashMap<ByteString, HashMap<Long, SingleIpPayload>> thisMinute = timeNameIpStore[minute - firstMinute];
        thisMinute.forEach((key, value) -> {
            RuleIpPayload ruleIpPayload = serviceMapWithRule.get(key);
            if (ruleIpPayload == null) {
                //没有规则加入 检查这个是否符合其他规则
                ArrayList<AlertRulesPrepare.Rule> rules = ruleMaps.checkIfInRule(key);
                if (rules != null) {
                    //这个是新规则 要进行初始化
                    ruleIpPayload = new RuleIpPayload();
                    serviceMapWithRule.put(key, ruleIpPayload);
                    ruleIpPayload.ipHashMap = new HashMap<>();
                    ruleIpPayload.rules = rules;
                    value.forEach((ip, SRP99) -> {

                    });
                } else {
                    //这个不在规则里
                    return;
                }
            }
            //进行报警检查


        });

    }

    void SolveMinuteP99AndSR2(int minute) {
        int timeIndex = minute - firstMinute;

        serviceMapAll.forEach((key, value) -> {
            value.ipHashMap.forEach((key2, value2) -> {
                SRAndP99Payload payload = value2.payload[timeIndex];
                if (payload == null) return;
                payload.p99 = solveP99(payload.bucket, payload.total);
                payload.rate = ((double) payload.success) / payload.total;
            });
        });
//        System.out.println("最大bucker="+maxBucket);
    }

    void mergeHashmap2(int minute) {
        for (int a = 0; a < DataPrepareManager.THREAD_NUMBER; ++a) {
            RawBufferSolveThread t = DataPrepareManager.rawBufferSolveThreadArray[a];
            if (t.timeNameIpStore == null) continue;
            int timeIndex = minute - firstMinute;
            HashMap<ByteString, HashMap<Long, SingleIpPayload>> serviceMap = t.timeNameIpStore[timeIndex];
            if (serviceMap == null) continue;

            serviceMap.forEach((key, value) -> {

                RuleIpPayload valuePackage = serviceMapAll.computeIfAbsent(key, k -> new RuleIpPayload());

                value.forEach((ip, srp99) -> {

                    RuleStatePayload payload = valuePackage.ipHashMap.computeIfAbsent(ip, k -> new RuleStatePayload(srp99, timeIndex));
                    //merge
                    SRAndP99Payload p = payload.payload[timeIndex];
                    if (p == null) {
                        payload.payload[timeIndex] = new SRAndP99Payload(srp99);
                    } else {
                        p.success += srp99.success;
                        p.total += srp99.total;
                        for (int i = 0; i < 300; ++i) {
                            p.bucket[i] += srp99.bucket[i];
                        }
                        srp99.bucket = null;
                    }

                });

            });
            //合并好了就可以释放内存
            t.timeNameIpStore[minute - firstMinute] = null;

        }
    }

    void mergeHashmap(int minute) {
        for (int a = 0; a < DataPrepareManager.THREAD_NUMBER; ++a) {
            RawBufferSolveThread t = DataPrepareManager.rawBufferSolveThreadArray[a];
            if (t.timeNameIpStore == null) continue;
            HashMap<ByteString, HashMap<Long, SingleIpPayload>> serviceMap = t.timeNameIpStore[minute - firstMinute];
            if (serviceMap == null) continue;
            HashMap<ByteString, HashMap<Long, SingleIpPayload>> thisMinute = timeNameIpStore[minute - firstMinute];
            serviceMap.forEach((key, value) -> {
                thisMinute.merge(key, value, (oldValue, newValue) -> {
                    //合并ip集合
                    newValue.forEach((key2, value2) -> {
                        oldValue.merge(key2, value2, (oldValue2, newValue2) -> {
                            oldValue2.success += newValue2.success;
                            oldValue2.total += newValue2.total;
                            for (int i = 0; i < 300; ++i) {
                                oldValue2.bucket[i] += newValue2.bucket[i];
                            }
                            newValue2.bucket = null;
                            return oldValue2;
                        });
                    });
                    return oldValue;
                });
            });
            //合并好了就可以释放内存
            t.timeNameIpStore[minute - firstMinute] = null;

        }
    }

    static int maxBucket = 0;

    int solveP99(int[] bucket, int allNum) {
        double i = 0.99 * allNum;
        int p99 = (int) Math.ceil(i);
        int bucketIndex = bucket.length;
        while (--bucketIndex >= 0) {
//            maxBucket=Math.max(bucket[bucketIndex],maxBucket);
            allNum -= bucket[bucketIndex];
            if (allNum < p99) {
                return bucketIndex;
            }
        }
        return 0;
    }

    void SolveMinuteP99AndSR(int minute) {
        HashMap<ByteString, HashMap<Long, SingleIpPayload>> thisMinute = timeNameIpStore[minute - firstMinute];
        if (thisMinute == null) return;
        thisMinute.forEach((key, value) -> {
            value.forEach((key2, value2) -> {
                SRAndP99Payload payload = new SRAndP99Payload(value2);
                payload.p99 = solveP99(payload.bucket, payload.total);
                payload.rate = ((double) payload.success) / payload.total;
                //释放bucket内存
                payload.bucket = null;
                value.put(key2, payload);
            });
        });
//        System.out.println("最大bucker="+maxBucket);
    }

    @Override
    public void run() {
        super.run();
        while (true) {
            try {
                BufferWithLatch bl = bufferQueue.take();
                //等待这个buffer处理完毕
                bl.countdown.await();
                if (firstMinute == -1) {

                    firstMinute = DistributeBufferThread.baseMinuteTime;
                    solvedMinute = firstMinute;
                    threads = DataPrepareManager.rawBufferSolveThreadArray;
                    serviceMapWithRule = new HashMap<>();
                }
                if (timeNameIpStore == null) {
                    //初始化线程独有的数据结构 [time][name][ip]
//                    timeNameIpStore = new HashMap[64];
//                    for (int a = 0; a < 32; ++a) {
//                        timeNameIpStore[a] = new HashMap<>(128);
//                    }
                }
                if (serviceMapAll == null) {
                    serviceMapAll = new HashMap<>(256);
                }

                int minute = bl.minute;

                for (int i = solvedMinute; i < minute - 3; ++i) {
//                    System.out.println("正在处理"+i);
                    //合并数据
                    mergeHashmap2(i);
                    //进行桶排 处理p99和sr
                    SolveMinuteP99AndSR2(i);

                    //进行报警处理

                    solvedMinute = i + 1;
                }
                if (bl.id == -1) {
                    //退出 表示处理完成
                    break;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
