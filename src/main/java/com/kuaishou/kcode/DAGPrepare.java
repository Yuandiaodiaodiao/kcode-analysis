package com.kuaishou.kcode;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 将分钟级数据结构聚合为以service-service为粒度的DAG图
 */
public class DAGPrepare {
    int firstMinute;

    //入边 出边 value
    //首先要计算
    DAGPrepare(int firstMinute) {
        this.firstMinute = firstMinute;
    }

    HashMap<ByteString, HashMapMergeThread.RuleIpPayload> serviceMapAll;

    HashMap<ByteString, AnswerStructure> Q2Answer;

    //每个
    static class AnswerStructure {

        AnswerStructure(){
            for(int i=0;i<DataPrepareManager.MAXTIME;++i){
                P99Array[i]=new ArrayList<>();
                SRArray[i]=new ArrayList<>();
            }


        }

        Collection<String>[] P99Array = new ArrayList[DataPrepareManager.MAXTIME];
        Collection<String>[] SRArray = new ArrayList[DataPrepareManager.MAXTIME];
    }

    /**
     * 边结构 保存了出边入边 和边上的数据
     */
    static class Edge {
        int from, to;
        SRAndP99Payload[] payloadArray;

        Edge(int from, int to, SRAndP99Payload[] payloadArray) {
            this.from = from;
            this.to = to;
            this.payloadArray = payloadArray;
        }
    }

    static class Vertex {
        Vertex(ByteString bs, int id) {
            this.bs = bs;
            this.id = id;
            path.v = this;
            Tpath.v = this;
        }

        int id;
        ByteString bs;
        int GMaxLength = 0;
        int GTMaxLength = 0;
        int inDegree = 0;
        int TinDegree = 0;
        Path path = new Path();
        Path Tpath = new Path();
    }

    HashMap<ByteString, Vertex> vertexMap;
    ArrayList<Vertex> vertexArray;

    /**
     * 将name-name 压入map中 并给予唯一id 建立name-id的双向映射
     */
    void convertServiceName2VertexId() {
        vertexMap = new HashMap<>();
        vertexArray = new ArrayList<>();
        serviceMapAll.forEach((key, value) -> {
            vertexMap.computeIfAbsent(key.first(), k -> {
                Vertex v = new Vertex(key.first(), vertexArray.size());
                vertexArray.add(v);
                return v;
            });
            vertexMap.computeIfAbsent(key.second(), k -> {
                Vertex v = new Vertex(key.second(), vertexArray.size());
                vertexArray.add(v);
                return v;
            });
        });
    }

    //正图
    ArrayList<Edge>[] G;
    //反图
    ArrayList<Edge>[] GT;
    Edge[][] GMatrix;

    /**
     * 给定v-v 建正图和反图
     */
    void buildDAG() {
        GMatrix = new Edge[vertexArray.size()][vertexArray.size()];
//        for(int i=0;i<GMatrix.length;++i){
//            GMatrix[i]=new Edge[GMatrix.length];
//        }
        G = new ArrayList[vertexArray.size()];
        for (int i = 0; i < G.length; i++) {
            G[i] = new ArrayList<>();
        }
        GT = new ArrayList[vertexArray.size()];
        for (int i = 0; i < GT.length; i++) {
            GT[i] = new ArrayList<>();
        }
        AtomicInteger allIn = new AtomicInteger();
        serviceMapAll.forEach((key, value) -> {
            int from = vertexMap.get(key.first()).id;
            int to = vertexMap.get(key.second()).id;
            vertexArray.get(to).inDegree++;
            allIn.getAndIncrement();
            Edge e1 = new Edge(from, to, value.serviceLevelPayload);
            GMatrix[from][to] = e1;
            G[from].add(e1);
            vertexArray.get(from).TinDegree++;
            GT[to].add(new Edge(to, from, value.serviceLevelPayload));
        });
//        System.out.println("入度总数=" + allIn);
    }

    static class point {
        point(int from, int to, int dist) {
            this.from = from;
            this.to = to;
            this.dist = dist;
        }

        point(int from) {
            this.from = from;
        }

        int from, to, dist;
    }

    /**
     * 拓扑排序 找出G和GT的拓扑序
     */
    ArrayList<Vertex> Gans;
    ArrayList<Vertex> GTans;

    void topsort() {
        ArrayList<Vertex> ans = new ArrayList<>();
        Stack<Vertex> stack = new Stack<>();
        int len = vertexArray.size();
        for (Vertex v : vertexArray) {
            if (v.inDegree == 0) {
                stack.add(v);
            }
        }
        while (!stack.empty()) {
            Vertex v = stack.pop();
            ans.add(v);
            for (int i = 0; i < G[v.id].size(); ++i) {
                Edge e = G[v.id].get(i);
                Vertex to = vertexArray.get(e.to);
                to.inDegree--;
                if (to.inDegree == 0) {
                    stack.add(to);
                }
            }
        }
        Gans = ans;
        ArrayList<Vertex> ans2 = new ArrayList<>();
        Stack<Vertex> stack2 = new Stack<>();
        int len2 = vertexArray.size();
        for (Vertex v : vertexArray) {
            if (v.TinDegree == 0) {
                stack2.add(v);
            }
        }
        while (!stack2.empty()) {
            Vertex v = stack2.pop();
            ans2.add(v);
            for (int i = 0; i < GT[v.id].size(); ++i) {
                Edge e = GT[v.id].get(i);
                Vertex to = vertexArray.get(e.to);
                to.TinDegree--;
                if (to.TinDegree == 0) {
                    stack2.add(to);
                }
            }
        }
        GTans = ans2;
    }

    int maxLength = 0;

    /**
     * 逆拓扑序进行dp 得出最长路径长度
     */
    void solveMaxPathLength() {
        int len = Gans.size();
        for (int i = len - 1; i >= 0; --i) {
            Vertex v = Gans.get(i);
            int GMaxLength = 0;
            for (Edge e : G[v.id]) {
                GMaxLength = Math.max(vertexArray.get(e.to).GMaxLength + 1, GMaxLength);
            }
            v.GMaxLength = Math.max(GMaxLength, v.GMaxLength);
            maxLength = Math.max(maxLength, v.GMaxLength);
        }

        int len2 = GTans.size();
        for (int i = len2 - 1; i >= 0; --i) {
            Vertex v = GTans.get(i);
            int GMaxLength = 0;
            for (Edge e : GT[v.id]) {
                GMaxLength = Math.max(vertexArray.get(e.to).GTMaxLength + 1, GMaxLength);
            }
            v.GTMaxLength = Math.max(GMaxLength, v.GTMaxLength);
        }
    }

    static class Path {
        Vertex v;
        LinkedList<Path> pathArray = new LinkedList<>();
    }

    /**
     * 对于一个点P 寻找点P的正向最长路和反向最长路
     * 若点P有子节点Q 则 若P.maxlength=1+Q.maxlength 则 P->Q 位于P开始的最长路上
     */
    void solveVertexPath() {
        int len = Gans.size();
        for (int i = len - 1; i >= 0; --i) {
            Vertex v = Gans.get(i);
            for (Edge e : G[v.id]) {
                Vertex to = vertexArray.get(e.to);
                if (to.GMaxLength + 1 == v.GMaxLength) {
                    //在最短路上
                    v.path.pathArray.add(to.path);
                }
            }
        }

        int len2 = GTans.size();
        for (int i = len2 - 1; i >= 0; --i) {
            Vertex v = GTans.get(i);
            for (Edge e : GT[v.id]) {
                Vertex to = vertexArray.get(e.to);
                if (to.GTMaxLength + 1 == v.GTMaxLength) {
                    //在最短路上
                    v.Tpath.pathArray.add(to.Tpath);
                }
            }
        }
        return;
    }

    /**
     * 枚举每条边 处理出每分钟的最短路
     */
    static class ByteStringAndVertex {
        ByteStringAndVertex(ByteString bs, ArrayList<Integer> vertexArrayList) {
            this.bs = bs;
            this.vertexArrayList = vertexArrayList;
        }

        ByteString bs;
        ArrayList<Integer> vertexArrayList;
    }

    static DecimalFormat DFORMAT = new DecimalFormat("#.00%");

    void generateAnswer(int firstMinute,int maxMinute) {
        int minuteIndex=maxMinute-firstMinute+3;
        Q2Answer = new HashMap<>(4096);
        serviceMapAll.forEach((key, value) -> {
            Vertex from = vertexMap.get(key.first());
            Vertex to = vertexMap.get(key.second());
            ArrayList<ByteStringAndVertex> bav1 = genPathFromTree(from, 1);
            ArrayList<ByteStringAndVertex> bav2 = genPathFromTree(to, 0);
            AnswerStructure ans = new AnswerStructure();

            FastStringBuilder srbuilder = new FastStringBuilder((maxLength + 1) * 128 * 2);
            FastStringBuilder p99builder = new FastStringBuilder((maxLength + 1) * 128 * 2);
            for (ByteStringAndVertex b1 : bav1) {
                for (ByteStringAndVertex b2 : bav2) {
                    srbuilder.setLength(0);
                    p99builder.setLength(0);
                    srbuilder.append(b1.bs);
                    srbuilder.append("->");
                    srbuilder.append(b2.bs);
                    srbuilder.append('|');
                    p99builder.append(b1.bs);
                    p99builder.append("->");
                    p99builder.append(b2.bs);
                    p99builder.append('|');
                    int index1 = srbuilder.index;
                    int index2 = p99builder.index;
                    //处理minute SR

                    for (int i = 0; i < minuteIndex; ++i) {
                        for (int j = b1.vertexArrayList.size() - 1; j >= 1; --j) {
                            int f = b1.vertexArrayList.get(j);
                            int t = b1.vertexArrayList.get(j - 1);
                            Edge e = GMatrix[f][t];
                            SRAndP99Payload payload = e.payloadArray[i];
                            if (payload != null && payload.total > 0) {
                                srbuilder.append(DFORMAT.format(payload.rate));
                                p99builder.append(payload.p99);
                            } else {
                                p99builder.append("-1");
                                srbuilder.append("-1%");
                            }
                            p99builder.append("ms,");
                            srbuilder.append(',');
                        }
                        {
                            Edge e = GMatrix[from.id][to.id];
                            SRAndP99Payload payload = e.payloadArray[i];
                            if (payload != null && payload.total > 0) {
                                srbuilder.append(DFORMAT.format(payload.rate));
                                p99builder.append(payload.p99);
                            } else {
                                p99builder.append("-1");
                                srbuilder.append("-1%");
                            }
                            p99builder.append("ms,");
                            srbuilder.append(',');

                        }
                        for (int j = 0; j < b2.vertexArrayList.size() - 1; ++j) {
                            int f = b2.vertexArrayList.get(j);
                            int t = b2.vertexArrayList.get(j + 1);
                            Edge e = GMatrix[f][t];
                            if(e==null){
                                System.out.println("e=null");
                                continue;
                            }
                            SRAndP99Payload payload = e.payloadArray[i];
                            if (payload != null && payload.total > 0) {
                                srbuilder.append(DFORMAT.format(payload.rate));
                                p99builder.append(payload.p99);
                            } else {
                                p99builder.append("-1");
                                srbuilder.append("-1%");
                            }
                            p99builder.append("ms,");
                            srbuilder.append(',');
                        }
                        //退掉最后一个,
                        srbuilder.setLength(srbuilder.index-1);
                        ans.SRArray[i].add(srbuilder.toString());
                        srbuilder.setLength(index1);

                        p99builder.setLength(p99builder.index-1);
                        ans.P99Array[i].add(p99builder.toString());
                        p99builder.setLength(index2);
                    }

                }
            }
//            ans.ansNum=a1.size()*a2.size();
//            ans.s1=a1;
//            ans.s2=a2;
            Q2Answer.put(key, ans);
        });
    }

    ArrayList<ByteStringAndVertex> genPathFromTree(Vertex from, int mode) {
//        ArrayList<String> bsArray=new ArrayList<>();
        ArrayList<ByteStringAndVertex> bsArray = new ArrayList<>();
        Path p = (mode == 1) ? from.Tpath : from.path;
        FastStringBuilder fsb = new FastStringBuilder((maxLength + 1) * 128);
        fsb.setAppendMode(mode);
        ArrayList<Integer> vertexArrayList = new ArrayList<>();
        dfsCopyTree(p, bsArray, fsb, vertexArrayList);
        return bsArray;
    }

    void dfsCopyTree(Path p, ArrayList<ByteStringAndVertex> answerList, FastStringBuilder fsb, ArrayList<Integer> vertexArrayList) {
        if (p.pathArray.size() == 0) {
            //到尾节点了 append
            int index = fsb.index;
            fsb.append(p.v.bs);
            vertexArrayList.add(p.v.id);

            answerList.add(new ByteStringAndVertex(fsb.toByteString(), (ArrayList<Integer>) vertexArrayList.clone()));
            vertexArrayList.remove(vertexArrayList.size() - 1);
            fsb.setLength(index);
//            answerList.add(new ByteString());
        } else {
            int index = fsb.index;
            fsb.append(p.v.bs);
            fsb.append("->");
            vertexArrayList.add(p.v.id);
            for (Path to : p.pathArray) {
                dfsCopyTree(to, answerList, fsb, vertexArrayList);
            }
            vertexArrayList.remove(vertexArrayList.size() - 1);

            fsb.setLength(index);
        }
    }
}
