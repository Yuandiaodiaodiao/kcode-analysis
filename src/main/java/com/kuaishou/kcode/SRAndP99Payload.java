package com.kuaishou.kcode;

public class SRAndP99Payload extends SingleIpPayload{
    SRAndP99Payload(){
        super();
    }
   SRAndP99Payload(SingleIpPayload payload){
       this.bucket=payload.bucket;
       payload.bucket=null;
       this.success=payload.success;
       this.total=payload.total;
   }
    double rate=0;
    int p99=0;
}
