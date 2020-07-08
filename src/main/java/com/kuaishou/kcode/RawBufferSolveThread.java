package com.kuaishou.kcode;

import java.nio.ByteBuffer;
import java.util.HashMap;
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
            HashMap<String,Integer>m=new HashMap<>();
//            m.put()
            while (true) {
                ByteBuffer buffer = unsolvedBuffer.take();

                solveLine(buffer);

                if (buffer.get(buffer.limit() - 1) == '\n') {
//                        System.out.println("正常");
                } else {
                    throw new ArrayIndexOutOfBoundsException("没有正确的切分换行");
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
        byte[] serviceA = new byte[32];
        byte[] serviceB = new byte[32];
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
            AnalyzeData.timecostMap.putIfAbsent(useTime,0);
            AnalyzeData.timecostMap.put(useTime,AnalyzeData.timecostMap.get(useTime)+1);


            ++position;
            int timeEndPos=position+9;
            int secondTime=0;
            for (int timepos = position; timepos <= timeEndPos; ++timepos) {
                secondTime = (byteArray[timepos] - 48) + secondTime * 10;
            }
            //毫秒时间戳 去掉后3位数字 是秒级时间戳 /60后是分钟时间戳
            int minTime=secondTime/60;
            AnalyzeData.timeSet.add(minTime);
            position += 13;



            int ipHash = (int) ((((ip1 - 167772160) % 3457) << 9) + ((ip2 - 167772160) % 2833)) % 2551;




        }
    }

}
