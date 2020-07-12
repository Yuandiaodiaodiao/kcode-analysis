package com.kuaishou.kcode;

public class FastStringBuilder {
    byte[] value;
    int mode;
    int index=0;
    FastStringBuilder(int cap){
        this.value=new byte[cap];
    }
    void setAppendMode(int mode){
        this.mode=mode;
    }
    void setLength(int index){
        this.index=index;
    }
    void append(char c){
        value[index++]=(byte)c;
    }
    void append(ByteString bs){
        //正向写入
        if(mode==0){
            for (int a = bs.offset; a < bs.length; ++a) {
                value[index++]=bs.value[a];
            }
        }else{
            //逆向写入
            for (int a = bs.length-1; a >= bs.offset; --a) {
                value[index++]=bs.value[a];
            }
        }
    }
    void append(String str){
        if(mode==0){
            for (int a = 0; a < str.length(); ++a) {
                value[index++]=(byte) str.charAt(a);
            }
        }else{
            //逆向写入
            for (int a = str.length()-1; a >= 0; --a) {
                value[index++]=(byte) str.charAt(a);
            }
        }
    }
    ByteString toByteString(){
        ByteString bs=new ByteString(index);
        bs.offset=0;
        bs.middle=0;
        bs.length=index;
        if(mode==0){
            if (index >= 0) System.arraycopy(value, 0, bs.value, 0, index);
        }else{
            for (int a =index-1,j=0; a >=0;++j,--a) {
                bs.value[j]=value[a];
            }
        }
        return bs;
    }
}
