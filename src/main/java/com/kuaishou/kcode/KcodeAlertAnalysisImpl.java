package com.kuaishou.kcode;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author KCODE
 * Created on 2020-07-04
 */
class unit_rule {
    public int id;
    public String service_a;
    public String service_b;
    public double sr = -1;
    public int p99 = Integer.MAX_VALUE;
    public int cnt;
}

class unit2 {
    public double sr = -1;
    public int p99 = Integer.MAX_VALUE;
    public int right_cnt;
    public ArrayList<Integer> array = new ArrayList<>();

    public void calc() {
        if (array.size() == 0) { // 如果没有调用
            sr = 1;
            p99 = 0;
            System.out.println("不会被调用");
            return;
        }
        sr = 1.0 * right_cnt / array.size();
        int p99_index = (int) Math.ceil(array.size() * 0.99);
        Collections.sort(array);
        p99 = array.get(p99_index - 1);
        array = null;
    }
}

class unit1 {
    public ArrayList<unit2> data = new ArrayList<>();  // 维度时间
    public ArrayList<Integer> rule = new ArrayList<>();
    public String service_a;
    public String service_b;

    public unit1(String service) {
        String[] strings = service.split("#");
        service_a = strings[0];
        service_b = strings[1];
    }

    public void add(int minute_index, int time_use, int is_right) {
//        System.out.println("add: " + minute_index);
        while (data.size() <= minute_index) data.add(new unit2());
        data.get(minute_index).array.add(time_use);
        data.get(minute_index).right_cnt += is_right;
    }
}

class unit {
    public HashMap<String, unit1> problem1 = new HashMap<>(); // ip
    public ArrayList<unit2> problem2 = new ArrayList<>();

    public void problem2_add(int minute_index, int time_use, int is_right) {
//        System.out.println("add: " + minute_index);
        while (problem2.size() <= minute_index) problem2.add(new unit2());
        problem2.get(minute_index).array.add(time_use);
        problem2.get(minute_index).right_cnt += is_right;
    }
}


public class KcodeAlertAnalysisImpl implements KcodeAlertAnalysis {
    public final int READ_SIZE = 1024 * 1024 * 4;
    public int minute_begin = -1;
    public int minute_end = -1;
    public int calc_minute_index = -1;

    public final ArrayList<unit_rule> main_rule = new ArrayList<>(20);
    public HashMap<String, unit> main_map = new HashMap<>();
    public final LinkedList<String> main_ans1 = new LinkedList<>();

    public final HashMap<HashString, ArrayList<ArrayList<String>>> main_ans2 = new HashMap<>(5000, 0.3f);
    public HashMap<String, HashMap<String, Integer>> edge = new HashMap<>();
    public HashMap<String, Integer> d_in = new HashMap<>();
    public ArrayList<ArrayList<String>> dfs_result = new ArrayList<>();

    public final ThreadWorker thread_worker = new ThreadWorker(this);
    private final ThreadAnalyse thread_analyse = new ThreadAnalyse(this);

    //    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private final DecimalFormat decimal_format = new DecimalFormat("#.00%");
    private final HashString hs = new HashString();

    public KcodeAlertAnalysisImpl() {
        Tools.init();
    }

    @Override
    public Collection<String> alarmMonitor(String path, Collection<String> alertRules) {
        try {
            // ----------------------- rule -----------------------------
            prepare_rule(alertRules);
            // ----------------------- io   -----------------------------
            FileInputStream input_stream = new FileInputStream(path);
            ReadableByteChannel chan = Channels.newChannel(input_stream);
            ByteBuffer[] buffers = new ByteBuffer[2];
            buffers[0] = ByteBuffer.allocateDirect(READ_SIZE);
            buffers[1] = ByteBuffer.allocateDirect(READ_SIZE);
            ByteBuffer tmp_buffer = ByteBuffer.allocateDirect(READ_SIZE); // 存放边角料

            ByteBuffer buf = null;
            int index = 0;

            int r = 0;
            tmp_buffer.limit(0); // 存放边角料
            while (r != -1) {
                buf = buffers[index];
                index = 1 - index;
                buf.clear();

                // 边角料放入
                buf.put(tmp_buffer);
                while (buf.hasRemaining() && r != -1) {
                    r = chan.read(buf);
                }

                int end = buf.position();
                //noinspection StatementWithEmptyBody
                while (buf.get(--end) != '\n') ;
                int old_end = buf.position();

                tmp_buffer.clear();
                for (int i = end + 1; i < old_end; i++) {
                    tmp_buffer.put(buf.get(i));
                }
                tmp_buffer.flip();

                buf.position(0);
                buf.limit(end + 1);

                thread_analyse.stop();
                thread_analyse.start(buf);
            }
            thread_analyse.stop();
            // 处理剩下 2 分钟
            thread_worker.stop();
            thread_worker.start(calc_minute_index + 1);
            thread_worker.stop();
            thread_worker.start(calc_minute_index + 2);
            thread_worker.stop();
            thread_worker.start(calc_minute_index + 3);
            thread_worker.stop();
            // ----------------------- s2   -----------------------------
//            long s = System.currentTimeMillis();
            for (unit tmp : main_map.values()) tmp.problem1 = null;
            prepare_ans2();
//            long e = System.currentTimeMillis();
//            System.out.println("q1 prepare_ans2: " + (e - s) / 1000 + "s");

        } catch (IOException e) {
            e.printStackTrace();
        }
//        for (String s : main_ans1) {
//            System.out.println(s);
//        }
        tt = 0;
        return main_ans1;
    }

    public static long tt = 0;

    @Override
    public final Collection<String> getLongestPath(String caller, String responder, String time, String type) {
//        long s = System.nanoTime();

        // 时间可能不合法

        // -------------------------- get minute ------------------------------
        char[] ch = Tools.get_ch(time);
        int y = ch[0] * 1000 + ch[1] * 100 + ch[2] * 10 + ch[3] - '0' * 1111;
        int M = ch[5] * 10 + ch[6] - '0' * 11;
        int d = ch[8] * 10 + ch[9] - '0' * 11;
        int H = ch[11] * 10 + ch[12] - '0' * 11;
        int m = ch[14] * 10 + ch[15] - '0' * 11;
        M -= 2;
        y -= M <= 0 ? 1 : 0;
        M += M <= 0 ? 12 : 0;
        int day = y / 4 - y / 100 + y / 400 + 367 * M / 12 + d + y * 365 - 719499;
        int t = (day * 24 + H - 8) * 60 + m - minute_begin;
        // -------------------------- get minute ------------------------------
        int nt = t < 0 || t > minute_end ? minute_end + 1 : t;
        hs.add3(caller, responder, type);
//        long e = System.nanoTime();

//        System.out.println(caller + " " + responder + " " + type + " " + time + ": ");
//        System.out.println(ret);
//        tt += e - s;
        return main_ans2.get(hs).get(nt);
    }

    private void prepare_rule(Collection<String> alertRules) {
        for (String rule : alertRules) {
            unit_rule tmp = new unit_rule();
            String[] strings = rule.split(",");
            tmp.id = Integer.parseInt(strings[0]);
            tmp.service_a = strings[1];
            tmp.service_b = strings[2];

            if (strings[3].equals("SR")) {
                tmp.cnt = Integer.parseInt(strings[4].replace("<", ""));
                tmp.p99 = Integer.MAX_VALUE;
                tmp.sr = Double.parseDouble(strings[5].replace("%", "")) / 100;
            } else {
                tmp.cnt = Integer.parseInt(strings[4].replace(">", ""));
                tmp.p99 = Integer.parseInt(strings[5].replace("ms", ""));
                tmp.sr = -1;
            }
            main_rule.add(tmp);
        }
    }

    private void prepare_ans2() {

        for (Map.Entry<String, Integer> entry : d_in.entrySet()) {
//            System.out.println(entry.getKey() + ": " + entry.getValue());
            if (entry.getValue() == 0) {
                dfs(entry.getKey(), new ArrayList<>());
            }
        }
        d_in = null;
        dfs_result.sort((check1, check2) -> {
            if (check1.size() == check2.size()) return 0;
            return check1.size() > check2.size() ? -1 : 1;
        });

        for (ArrayList<String> t : dfs_result) {
//            String sr = null;
//            String p99 = null;
            for (int i = 1; i < t.size(); i++) {
                String a = t.get(i - 1);
                String b = t.get(i);
                if (edge.get(a).get(b) <= t.size()) {
//                    if (sr == null || p99 == null) {
//                        sr = gen_sr(t);
//                        p99 = gen_p99(t);
//                    }
                    edge.get(a).put(b, t.size());
                    gen_ans2(a, b, t);
                }
            }
        }

//        for (ArrayList<String> t : dfs_result) {
//            System.out.println(t.toString());
//        }
    }

    private void gen_ans2(String a, String b, ArrayList<String> t) {
        hs.add3(a, b, "SR");

        ArrayList<ArrayList<String>> array_sr = main_ans2.computeIfAbsent(hs, k -> {
            ArrayList<ArrayList<String>> tmp = new ArrayList<>();
            for (int i = minute_begin; i <= minute_end + 1; i++) tmp.add(new ArrayList<>());
            return tmp;
        });

        hs.add3(a, b, "P99");
        ArrayList<ArrayList<String>> array_p99 = main_ans2.computeIfAbsent(hs, k -> {
            ArrayList<ArrayList<String>> tmp = new ArrayList<>();
            for (int i = minute_begin; i <= minute_end + 1; i++) tmp.add(new ArrayList<>());
            return tmp;
        });

        for (int i = minute_begin; i <= minute_end + 1; i++) {
            int index = i - minute_begin;
            StringBuilder sb0 = new StringBuilder();
            StringBuilder sb_sr = new StringBuilder();
            StringBuilder sb_p99 = new StringBuilder();

            sb0.append(t.get(0));
            for (int j = 1; j < t.size(); j++) {
                sb0.append("->").append(t.get(j));
                char c = j == 1 ? '|' : ',';
                double sr;
                int p99;
                if (main_map.get(t.get(j - 1) + "#" + t.get(j)).problem2.size() <= index) {
                    sr = -1;
                    p99 = Integer.MAX_VALUE;
                } else {
                    unit2 tmp = main_map.get(t.get(j - 1) + "#" + t.get(j)).problem2.get(index);
                    sr = tmp.sr;
                    p99 = tmp.p99;
                }
                sb_sr.append(c).append(sr == -1 ? -1 + "%" : decimal_format.format(sr));
                sb_p99.append(c).append(p99 == Integer.MAX_VALUE ? -1 : p99).append("ms");
            }
            array_sr.get(index).add(sb0.toString() + sb_sr.toString());
            array_p99.get(index).add(sb0.toString() + sb_p99.toString());
        }
    }

    private void dfs(String key, ArrayList<String> array) {
        array.add(key);
        if (!edge.containsKey(key)) {
            dfs_result.add(array);
            return;
        }
        for (String key_b : edge.get(key).keySet()) {
            dfs(key_b, new ArrayList<>(array)); // 复制
        }
    }

    private int get_minute(String time) {
        int y = time.charAt(0) * 1000 + time.charAt(1) * 100 + time.charAt(2) * 10 + time.charAt(3) - '0' * 1111;
        int M = time.charAt(5) * 10 + time.charAt(6) - '0' * 11;
        int d = time.charAt(8) * 10 + time.charAt(9) - '0' * 11;
        int H = time.charAt(11) * 10 + time.charAt(12) - '0' * 11;
        int m = time.charAt(14) * 10 + time.charAt(15) - '0' * 11;
//        int y = (time.charAt(0) - '0') * 1000 + (time.charAt(1) - '0') * 100 + (time.charAt(2) - '0') * 10 + time.charAt(3);
//        int M = Integer.parseInt(time.substring(5, 7));
//        int M = Integer.parseInt(time.substring(5, 7));
//        int d = Integer.parseInt(time.substring(8, 10));
//        int H = Integer.parseInt(time.substring(11, 13));
//        int m = Integer.parseInt(time.substring(14, 16));

        M -= 2;
        y -= M <= 0 ? 1 : 0;
        M += M <= 0 ? 12 : 0;
//        if (M <= 0) {
//            M += 12;
//            y -= 1;
//        }

        int day = y / 4 - y / 100 + y / 400 + 367 * M / 12 + d + y * 365 - 719499;

        return (day * 24 + H - 8) * 60 + m;
    }
//    private void get_hashcode(String a, String b, String type) {
//
//
//    }
}