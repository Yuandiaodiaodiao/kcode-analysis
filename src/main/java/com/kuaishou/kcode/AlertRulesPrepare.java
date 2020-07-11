package com.kuaishou.kcode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class AlertRulesPrepare {
    public static class Rule{
        int id;
        String service1,service2;
        //1是 SR 2是P99
        int type;
        int timeThreshold;
        boolean judgeType;
        double thresholdSR;
        int thresholdP99;
    }
    public static class RuleMaps{
        //主调-被调 作为索引
        HashMap<ByteString,ArrayList<Rule>> name2name=new HashMap<>();
        //被调服务固定 作为索引
        HashMap<ByteString,ArrayList<Rule>> all2name=new HashMap<>();
        //主调服务固定 作为索引
        HashMap<ByteString,ArrayList<Rule>> name2all=new HashMap<>();

        ArrayList<Rule> checkIfInRule(ByteString bs){
            ArrayList<Rule>ans;
            ArrayList<Rule>name2nameValue=name2name.get(bs);
            ArrayList<Rule>all2nameValue=all2name.get(bs.second());
            ArrayList<Rule>name2allValue=name2all.get(bs.first());
            if(name2allValue!=null || all2nameValue!=null || name2nameValue!=null){
                ans=new ArrayList<>();
                if(name2nameValue!=null){
                    for(Rule r:name2nameValue){
                        ans.add(r);
                    }
                }
                if(all2nameValue!=null){
                    for(Rule r:all2nameValue){
                        ans.add(r);
                    }
                }
                if(name2allValue!=null){
                    for(Rule r:name2allValue){
                        ans.add(r);
                    }
                }
                return ans;
            }else{
                return null;
            }
        }
    }

    static RuleMaps prepare3HashMap(ArrayList<Rule> ruleArrayList){
        RuleMaps rm=new RuleMaps();
        for(Rule r:ruleArrayList){
            if(r.service1.equals("ALL")){
                //固定被调
                ByteString bs=new ByteString(r.service2);
                rm.all2name.computeIfAbsent(bs,k->new ArrayList<>()).add(r);
            }else if(r.service2.equals("ALL")){
                //固定主调
                ByteString bs=new ByteString(r.service1);
                rm.name2all.computeIfAbsent(bs,k->new ArrayList<>()).add(r);
            }else{
                //主被调都固定
                ByteString bs=new ByteString(r.service1,r.service2);
                rm.name2name.computeIfAbsent(bs,k->new ArrayList<>()).add(r);
            }
        }
        return rm;
    }

    static ArrayList<Rule> prepare(Collection<String> alertRules){
        ArrayList<Rule>array=new ArrayList<>(10000);
        alertRules.forEach((value)->{
            String[] stringArray=value.split(",");
            Rule r=new Rule();
            r.id=Integer.parseInt(stringArray[0]);
            r.service1=stringArray[1];
            r.service2=stringArray[2];
            r.type=(stringArray[3].charAt(0)=='S')?1:2;
            String timeThreshold=stringArray[4];
            timeThreshold=timeThreshold.substring(0,timeThreshold.length()-1);
            r.timeThreshold=Integer.parseInt(timeThreshold);
            r.judgeType=(stringArray[4].charAt(stringArray[4].length()-1)=='>');
            if(r.type==1){
                //SR
                r.thresholdSR=String2Double(stringArray[5]);
            }else{
                r.thresholdP99=String2Int(stringArray[5]);
            }
            array.add(r);
        });



        return array;
    }
    static int String2Int(String s){
        int length=s.length();
        int ans=0;
        for(int i=0;i<length;++i){
            char c=s.charAt(i);
            if(c>='0'&&c<='9'){
                ans=ans*10+(c-'0');
            }else{
                break;
            }
        }
        return ans;
    }
    static double String2Double(String s){
        int length=s.length();
        s=s.substring(0,length-1);
        double ans=Double.parseDouble(s);

        return ans;
    }
}

