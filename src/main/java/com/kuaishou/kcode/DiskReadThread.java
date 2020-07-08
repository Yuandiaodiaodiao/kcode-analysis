package com.kuaishou.kcode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ArrayBlockingQueue;


public class DiskReadThread extends Thread {
    long fileLength;
    FileChannel channel;
    private static final int CHUNCK_SIZE = DataPrepareManager.DIRECT_CHUNK_SIZE;
    private ArrayBlockingQueue<ByteBuffer> canuse;
    private ArrayBlockingQueue<ByteBuffer> canread;
    private int lastBufferNumbers;

    DiskReadThread(){
        lastBufferNumbers=2;
    }
    public void LinkBlockingQueue(ArrayBlockingQueue<ByteBuffer> canuse, ArrayBlockingQueue<ByteBuffer> canread) {
        this.canuse = canuse;
        this.canread = canread;
    }

    public static long DiskRead_waitBuffer = 0;

    public int initChannel(String path) {
        File f = new File(path);
        fileLength = f.length();
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(f, "r");
            channel = raf.getChannel();
            return (int) (fileLength / CHUNCK_SIZE + (fileLength % CHUNCK_SIZE > 0 ? 1 : 0));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 将当前读入末尾的不完整的line保存起来
     * @param lastLine 保存读入时最后的不完整的line
     * @param buffer 读入进来的buffer
     */
    private void saveLastLine(ByteBuffer lastLine,ByteBuffer buffer){
        int lastIndex=buffer.limit();
        //找到最后一个\n的位置
        if(buffer.get(lastIndex-1)=='\n'){
            lastLine.clear();
            lastLine.limit(0);
            return;
        }

        while( buffer.get(--lastIndex)!='\n'){

        }
        for(int i=lastIndex;i<buffer.limit();++i){
            lastLine.put(buffer.get(i));
        }
        lastLine.flip();
        buffer.limit(lastIndex+1);
    }

    /**
     * 将上一次读入末尾不完整的line追加到这次读入的最前面
     * @param lastLine 上一次读入时最后的不完整的line
     * @param buffer 这一次要读入的buffer
     */
    private void loadLastLine(ByteBuffer lastLine,ByteBuffer buffer){
        if(lastLine.limit()==0){
            lastLine.clear();
            return;
        }
        buffer.put(lastLine);
        lastLine.clear();
    }

    @Override
    public void run() {
        super.run();
        try {

            ByteBuffer lastLine=ByteBuffer.allocate(256);
            lastLine.clear();
            lastLine.flip();
            for (long i = 0; i < fileLength; i += CHUNCK_SIZE) {
                ByteBuffer buf=null;
                if(canuse.size()==0 && lastBufferNumbers-->0){
                    buf=ByteBuffer.allocateDirect(CHUNCK_SIZE);
                }else{
                    TimeRange t1=new TimeRange();
                    buf=canuse.take();
                    t1.point();
                    DiskRead_waitBuffer+=t1.firstTime();
                }
                buf.clear();
                loadLastLine(lastLine,buf);
                channel.read(buf);
                buf.flip();
                saveLastLine(lastLine,buf);
                canread.offer(buf);

            }

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}
