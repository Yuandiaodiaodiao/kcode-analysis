package com.kuaishou.kcode;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.RecursiveAction;

public class ThreadAnalyse extends RecursiveAction {

    boolean running = false;
    ByteBuffer buffer = null;
    KcodeAlertAnalysisImpl main_class;
    HashMap<String, unit> main_map;

    public ThreadAnalyse(KcodeAlertAnalysisImpl main_class) {
        this.main_class = main_class;
        this.main_map = main_class.main_map;
    }

    public void start(ByteBuffer buf) {
        this.buffer = buf;
        this.running = true;
        this.fork();
    }

    public void stop() {
        if (this.running) {
            this.join();
            this.running = false;
        }
        this.reinitialize();
    }

    private final StringBuilder string_builder_service = new StringBuilder();
    private final StringBuilder string_builder_ip = new StringBuilder();
    private final char[] ch = new char[200];
    int index;

    String service;
    String ip;
    int is_right;
    int time_use;
    byte b;
    int minute;

    public void compute() {
        int i = 0, end = this.buffer.limit();
        while (i < end) {

            // service_a
            index = 0;
            while ((ch[index] = (char) buffer.get(i)) != ',') {
                index += 1;
                i += 1;
            }
            i += 1;
            string_builder_service.append(ch, 0, index).append('#');


            // ip_a -----------------------
            index = 0;
            while ((ch[index] = (char) buffer.get(i)) != ',') {
                index += 1;
                i += 1;
            }
            i += 1;
            string_builder_ip.append(ch, 0, index).append('#');

            // service_b ---------------------------
            index = 0;
            while ((ch[index] = (char) buffer.get(i)) != ',') {
                index += 1;
                i += 1;
            }
            i += 1;
            string_builder_service.append(ch, 0, index);


            // ip_b -----------------------
            index = 0;
            while ((ch[index] = (char) buffer.get(i)) != ',') {
                index += 1;
                i += 1;
            }
            i += 1;
            string_builder_ip.append(ch, 0, index);


            // 开始读 true false
            if (buffer.get(i) == 't') {
                is_right = 1;
                i += 5;
            } else {
                is_right = 0;
                i += 6;
            }

            // 开始读 time use
            time_use = 0;
            while ((b = buffer.get(i)) != ',') {
                time_use = time_use * 10 + b - '0';
                i += 1;
            }
            i += 1; // 逗号下一个

            // 开始读 timestamp 秒级
            minute = 0;
            for (int j = 0; j < 10; j++) {
                b = buffer.get(i + j);
                minute = minute * 10 + b - '0';
            }
            minute /= 60;
            i += 14;

            // 更新 main class
            main_class.minute_begin = main_class.minute_begin == -1 ? minute - 1 : main_class.minute_begin;
            main_class.minute_end = Math.max(main_class.minute_end, minute);

            int minute_index = minute - main_class.minute_begin;

            service = string_builder_service.toString();
            string_builder_service.delete(0, string_builder_service.length());
            ip = string_builder_ip.toString();
            string_builder_ip.delete(0, string_builder_ip.length());

            unit tmp_unit = main_map.computeIfAbsent(service, k -> new unit());
            unit1 tmp_unit1 = tmp_unit.problem1.computeIfAbsent(ip, k -> new unit1(service));
            tmp_unit1.add(minute_index, time_use, is_right);
            tmp_unit.problem2_add(minute_index, time_use, is_right);

            if (minute_index > main_class.calc_minute_index + 3) {
                main_class.calc_minute_index += 1;
                main_class.thread_worker.start(main_class.calc_minute_index);
                main_class.thread_worker.stop(); // map 添加元素导致冲突
            }
//            System.out.println(service + " " + ip + " " + minute + " " + minute_index);
        }
        this.running = false;
    }
}
