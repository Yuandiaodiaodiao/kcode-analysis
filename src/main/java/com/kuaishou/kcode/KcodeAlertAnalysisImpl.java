package com.kuaishou.kcode;

import java.util.Collection;

/**
 * @author KCODE
 * Created on 2020-07-04
 */
public class KcodeAlertAnalysisImpl implements KcodeAlertAnalysis {
    private DataPrepareManager manager;
    public KcodeAlertAnalysisImpl(){
        manager=new DataPrepareManager();
    }
    @Override
    public Collection<String> alarmMonitor(String path, Collection<String> alertRules) {
        TimeRange t1=new TimeRange();
        manager.start(path,alertRules);
        manager.stop();
        t1.point();
        t1.output("read 耗时");
        String s=AnalyzeData.printMemoryInfo();
        if(DistributeBufferThread.baseMinuteTime>0){

            throw new ArrayIndexOutOfBoundsException("RAM"+s+"耗时"+t1.firstTime());
        }
        return null;
    }

    @Override
    public Collection<String> getLongestPath(String caller, String responder, String time, String type) {
        return null;
    }
}