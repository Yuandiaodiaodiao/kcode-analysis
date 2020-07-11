package com.kuaishou.kcode;

public class SingleIpPayload {
    //成功次数
    int success=0;
    //总调用次数
    int total=0;
    //用于桶排序的桶
    byte[] bucket=new byte[300];
}
