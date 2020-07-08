package com.kuaishou.kcode;

import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.BiFunction;

public class RawBufferSolveThread extends Thread {


    private ArrayBlockingQueue<ByteBuffer> unsolvedBuffer;
    private ArrayBlockingQueue<ByteBuffer> solvedBuffer;

    public void LinkHeapBufferBlockingQueue(ArrayBlockingQueue<ByteBuffer> unsolvedBuffer, ArrayBlockingQueue<ByteBuffer> solvedBuffer) {
        this.unsolvedBuffer = unsolvedBuffer;
        this.solvedBuffer = solvedBuffer;
    }

    @Override
    public void run() {
        super.run();
        try {

            while (true) {
                ByteBuffer buffer = unsolvedBuffer.take();
                //14秒
//                    while(buffer.hasRemaining()){
//                        buffer.get();
////                        System.out.print((char)buffer.get());
//                    }
                //4秒

//                    int limit=buffer.limit();
//                    for(int i=0;i<limit;++i){
//                        buffer.get();
//                    }
                //3秒
                solveLine(buffer);

                if (buffer.get(buffer.limit() - 1) == '\n') {
//                        System.out.println("正常");
                } else {
                    System.out.println("异常");
                }
                solvedBuffer.offer(buffer);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void solveLine(ByteBuffer buffer) {
        byte[] byteArray = null;
        try {
            byteArray = Utils.getHeapByteBufferArray(buffer);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        byte b = 0;

        int position = buffer.position();
        int limit = buffer.limit()-1;
        byte[] serviceA = new byte[64];
        byte[] serviceB = new byte[64];
        while (position < limit) {
            long ip1 = 0;
            long ip2 = 0;
            int serviceALength = -1;
            int serviceBLength = -1;
            //逗号,分隔
            while ((serviceA[++serviceALength] = byteArray[++position]) != 44) {
            }


            int numBuff = 0;
            while ((b = byteArray[++position]) != 44) {
                ip1 = (b != 46) ? ip1 : (ip1 << 8) + numBuff;
                numBuff = (b != 46) ? (b - 48) + numBuff * 10 : 0;
            }
            ip1 = (ip1 << 8) + numBuff;

            while ((serviceB[++serviceBLength] = byteArray[++position]) != 44) {
            }

            numBuff = 0;
            while ((b = byteArray[++position]) != 44) {
                ip2 = (b != 46) ? ip2 : (ip2 << 8) + numBuff;
                numBuff = (b != 46) ? (b - 48) + numBuff * 10 : 0;
            }
            ip2 = (ip2 << 8) + numBuff;


            int success = (byteArray[++position] == 116 ? 0 : 1);
            position += 4 + success;


            int useTime = 0;
            while ((b = byteArray[++position]) != 44) {
                useTime = (b - 48) + useTime * 10;
            }

            int timeEndPos=++position+9;
            int minTime=0;
            for (int timepos = position; timepos <= timeEndPos; ++timepos) {
                minTime = (byteArray[timepos] - 48) + minTime * 10;
            }
            position += 13;



            int ipHash = (int) ((((ip1 - 167772160) % 3457) << 9) + ((ip2 - 167772160) % 2833)) % 2551;




        }
    }

}
