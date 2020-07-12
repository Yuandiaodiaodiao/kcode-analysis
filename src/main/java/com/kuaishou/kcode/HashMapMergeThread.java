package com.kuaishou.kcode;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.Date;

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
        SRAndP99Payload[] serviceLevelPayload=new SRAndP99Payload[64];
        int lastRefreshTime=-1;
        int checked=0;
        ArrayList<AlertRulesPrepare.Rule> rules;
        HashMap<Long, RuleStatePayload> ipHashMap;
    }

    HashMap<ByteString, RuleIpPayload> serviceMapAll;
    int firstMinute = -1;
    int maxMinute=0;
    int solvedMinute = -1;
    RawBufferSolveThread[] threads;
    HashMap<ByteString, HashMap<Long, SingleIpPayload>>[] timeNameIpStore;
    ArrayList<String>warningList;
     static SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    static  DecimalFormat DFORMAT = new DecimalFormat("#.00%");

    void checkAllIPWarning(ByteString serviceName, RuleIpPayload ruleIpPayload,int minute){
        StringBuilder stringBuilder=new StringBuilder(400);
        if(ruleIpPayload.lastRefreshTime==minute&& ruleIpPayload.ipHashMap!=null && ruleIpPayload.ipHashMap.size()>0){
            //检查
            ruleIpPayload.ipHashMap.forEach((ip,srp99)->{
                SRAndP99Payload payload=srp99.payload[minute-firstMinute];
                if(payload==null)return;
                final ArrayList<AlertRulesPrepare.Rule> rules = ruleIpPayload.rules;
                final ArrayList<RuleState> states = srp99.states;
                if(states ==null){
                    srp99.states=new ArrayList<>(rules.size());
                    for(AlertRulesPrepare.Rule r: rules){
                        srp99.states.add(new RuleState());
                    }
                }
                int len= rules.size();
                for(int i=0;i<len;++i){
                    AlertRulesPrepare.Rule r= rules.get(i);
                    RuleState state=srp99.states.get(i);
                    if(state.lastCheckTime!=minute-1){
                        state.warningTimes=0;
                    }
                    if(r.matchOnce(payload)){

                        state.warningTimes++;
                        if(state.warningTimes>=r.timeThreshold){
                            //报警
                            stringBuilder.setLength(0);
                            stringBuilder.append(r.id);
                            stringBuilder.append(',');
                            stringBuilder.append(dataFormat.format(new Date(((long)minute)*60000)));
                            stringBuilder.append(',');
                            serviceName.first().appendStringBuilder(stringBuilder);
                            stringBuilder.append(',');
                            Utils.firstIp2StringBuilder(ip,stringBuilder);
                            stringBuilder.append(',');
                            serviceName.second().appendStringBuilder(stringBuilder);
                            stringBuilder.append(',');
                            Utils.secondIp2StringBuilder(ip,stringBuilder);
                            stringBuilder.append(',');
                            if(r.type==1){
                                stringBuilder.append(DFORMAT.format(payload.rate));
                            }else {
                                stringBuilder.append(payload.p99);
                                stringBuilder.append('m');
                                stringBuilder.append('s');
                            }
                            warningList.add(stringBuilder.toString());
                        }
                    }else{
                        state.warningTimes=0;
                    }
                    state.lastCheckTime=minute;
                }
            });

        }
    }
    void doWarning(int minute) {
        serviceMapAll.forEach((serviceName,value)->{
            if(value.rules!=null){
                //要检查的!
                checkAllIPWarning(serviceName,value,minute);
            }else if(value.checked==0){
                //新key 检查一下
                ArrayList<AlertRulesPrepare.Rule> rules = ruleMaps.checkIfInRule(serviceName);
                if (rules != null) {
                    //这个是新规则 要进行初始化
                    value.rules=rules;
                    //然后进行检查
                    checkAllIPWarning(serviceName,value,minute);
                }
                value.checked=1;
            }//跳过

        });


    }
    void SolveMinuteP99AndSR2(int minute){
        int timeIndex = minute - firstMinute;

        serviceMapAll.forEach((key, value) -> {
            //这分钟没merge 说明是空的
            if(value.lastRefreshTime<minute)return;
            value.ipHashMap.forEach((key2, value2) -> {
                SRAndP99Payload payload = value2.payload[timeIndex];
                if (payload == null) return;
                payload.p99 = solveP99(payload.bucket, payload.total);
                payload.rate = ((double) payload.success) / payload.total;
            });
        });
    }
    void SolveServiceLevelSRAndP99(int minute) {
        int timeIndex = minute - firstMinute;

        serviceMapAll.forEach((key, value) -> {
            //这分钟没merge 说明是空的
            if(value.lastRefreshTime<minute)return;
            SRAndP99Payload servicePayload=new SRAndP99Payload();
            value.serviceLevelPayload[timeIndex]=servicePayload;
            value.ipHashMap.forEach((key2, value2) -> {
                SRAndP99Payload payload = value2.payload[timeIndex];
                if (payload == null) return;
                servicePayload.success += payload.success;
                servicePayload.total += payload.total;
                for (int i = 0; i < 300; ++i) {
                    servicePayload.bucket[i] += payload.bucket[i];
                }
                //释放内存
                payload.bucket=null;
            });
            servicePayload.p99=solveP99(servicePayload.bucket,servicePayload.total);
            servicePayload.rate=((double)servicePayload.success)/servicePayload.total;
            servicePayload.bucket=null;
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
                valuePackage.lastRefreshTime=minute;
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

    static int solveP99(int[] bucket, int allNum) {
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
                    warningList=new ArrayList<>(5000);
                    firstMinute = DistributeBufferThread.baseMinuteTime;
                    solvedMinute = firstMinute;
                    threads = DataPrepareManager.rawBufferSolveThreadArray;
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
                    doWarning(i);
                    //进行service-service粒度的聚合
                    SolveServiceLevelSRAndP99(i);
                    maxMinute=Math.max(maxMinute,i);
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
