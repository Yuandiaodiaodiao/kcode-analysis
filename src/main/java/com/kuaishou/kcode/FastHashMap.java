package com.kuaishou.kcode;

import java.util.HashMap;

public class FastHashMap<K, V> {
    static class Node<K, V> {
        Node(K key, V value) {
            this.key = key;
            this.value = value;
            this.hash = value.hashCode();
        }

        K key;
        V value;
        int hash;
        Node<K, V> next;
    }

    public static boolean[] eratos_prime(int n)// 埃拉托色尼 素数筛选法
    {
        boolean[] ans = new boolean[(int) n + 1];
        for (int i = 0; i < n; i++) {
            ans[i] = true;
        }
        ans[0] = ans[1] = false;
        for (int i = 2; i <= n; i++) {

            if (ans[i]) {
                int j = i + i;
                while (j <= n) {
                    ans[j] = false;
                    j += i;
                }
            }
        }
        return ans;
    }

    Node[] bucket;
    static final int MAXIMUM_CAPACITY = 1 << 30;
    int capacity;
    int mod;
    boolean[] primeArray;

    FastHashMap(int cap) {
        this.capacity = tableSizeFor(cap);
        bucket = new Node[this.capacity];
        primeArray = eratos_prime(this.capacity - 1);
        mod = maxPrime(this.capacity - 1);
    }

    public void remodsmall() {
        this.mod = maxPrime(this.mod - 1);
    }

    public void remodbig() {
        this.mod = nextPrime((int)(this.mod*1.75));
        this.mod = Math.min(this.capacity-1, this.mod);
    }

    static int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    int hasClash = 1;

    public void prepareReady() {
        if (getHashClash() == 0) {
            hasClash = 0;
        }
    }

    int clashNum = 0;

    public void clear() {
        int hasClash = 1;
        clashNum = 0;
        for (int i = 0; i < this.capacity; ++i) {
            bucket[i] = null;
        }
    }

    public int getHashClash() {
        return clashNum;
    }
    public V get8bit(int hash1,int hash2){
       return (V)bucket[((((hash1<<1)>>>1)%mod)<<8)+(hash2&0xFF)].value;
    }
    public V get(int hash){
        return (V) bucket[((hash<<1)>>>1)%mod].value;
    }
    public V get(K key) {

        int hash = key.hashCode();
        Node<K, V> p = bucket[((hash<<1)>>>1)%mod];
        //取出节点p
//        if (p == null) return null;

        if (hasClash == 0) {
            return p.value;
        } else {
            while (p.next != null) {
                if (p.hash != hash || !p.key.equals(key)) {
                    p = p.next;
                } else {
                    return p.value;
                }
            }
            return p.value;
        }

    }
    public V put8bit(K key,V value,int hash1,int hash2){
        int abshash =((((hash1<<1)>>>1)%mod)<<8)+(hash2&0xFF);
        Node<K, V> p = bucket[abshash];
        //没有就直接放
        if (p == null) {
            bucket[abshash] = new Node<>(key, value);
            return value;
        }
        //有的话顺着next找到一样的
        Node<K, V> lastp = null;


        while (p != null) {

            if (p.hash != hash1 || !p.key.equals(key)) {
                //如果不一样就找下一个
                lastp = p;
                p = p.next;
            } else {
                //一样了就修改
                p.key = key;
                p.value = value;
                p.hash = hash1;
                return p.value;
            }
        }
        //整个链表上都没有

        clashNum++;
        lastp.next = new Node<>(key, value);
        return lastp.next.value;
    }

    public V put(K key,V value,int hash){
        int abshash =((hash<<1)>>>1)%mod;
        Node<K, V> p = bucket[abshash];
        //没有就直接放
        if (p == null) {
            bucket[abshash] = new Node<>(key, value);
            return value;
        }
        //有的话顺着next找到一样的
        Node<K, V> lastp = null;


        while (p != null) {

            if (p.hash != hash || !p.key.equals(key)) {
                //如果不一样就找下一个
                lastp = p;
                p = p.next;
            } else {
                //一样了就修改
                p.key = key;
                p.value = value;
                p.hash = hash;
                return p.value;
            }
        }
        //整个链表上都没有

        clashNum++;
        lastp.next = new Node<>(key, value);
        return lastp.next.value;


    }

    public V put(K key, V value) {
        int hash = key.hashCode();
        int abshash =(((hash)^(hash>>>16))&mod);
        Node<K, V> p = bucket[abshash];
        //没有就直接放
        if (p == null) {
            bucket[abshash] = new Node<>(key, value);
            return value;
        }
        //有的话顺着next找到一样的
        Node<K, V> lastp = null;


        while (p != null) {

            if (p.hash != hash || !p.key.equals(key)) {
                //如果不一样就找下一个
                lastp = p;
                p = p.next;
            } else {
                //一样了就修改
                p.key = key;
                p.value = value;
                p.hash = hash;
                return p.value;
            }
        }
        //整个链表上都没有

        clashNum++;
        lastp.next = new Node<>(key, value);
        return lastp.next.value;


    }

    int maxPrime(int n) {
        int i = 0;
        for (i = n; i >= 2; --i) {
            if (primeArray[i]) {
                return i;
            }
        }
        return i;
    }
    int nextPrime(int n) {
        int i = 0;
        for (i = n; i <capacity; ++i) {
            if (primeArray[i]) {
                return i;
            }
        }
        return i;
    }
}
