package com.kuaishou.kcode;

import java.util.HashMap;

public class FastHashMap<K,V> {
    class Node<K,V>{
        Node(K key,V value){
            this.key=key;
            this.value=value;
            this.hash=value.hashCode();
        }

        K key;
        V value;
        int hash;
        Node<K,V>next;
    }
    Node<K,V>[]bucket;
    static final int MAXIMUM_CAPACITY=1<<30;
    int capacity;
    int mod;
    FastHashMap(int cap){
        this.capacity=tableSizeFor(cap);
        bucket=new Node[this.capacity];
        mod=maxPrime(this.capacity);
    }

    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }
    public V get(K key){
        int hash=key.hashCode();
        Node<K,V> p=bucket[hash%mod];
        if(p==null)return null;
        //取出节点p
        while(p.next!=null){
            if(p.hash!=hash || !p.key.equals(key)){
                p=p.next;
            }else{
                return p.value;
            }
        }
        return p.value;
    }
    public V put(K key,V value){
        int hash=key.hashCode();
        Node<K,V> p=bucket[hash%mod];
        if(p==null){
            bucket[hash%mod]=new Node<>(key,value);
            return value;
        }
        //取出节点p

        while(p.next!=null){
            if(p.hash!=hash || !p.key.equals(key)){
                p=p.next;
            }else{
                return p.value;
            }
        }
        p.key=key;
        p.value=value;
        p.hash=hash;
        return p.value;
    }
    static int maxPrime(int n){
        for (int i =n; i >2; i--) {
            if(i % 2 == 0)  continue; //偶数和1排除

            boolean sgin= true;
            for (int j = 2; j <= Math.sqrt(i) ; j++) {
                if (i % j == 0) {
                    sgin = false;
                    break;
                }
            }
            //打印
            if (sgin) {
                return i;
            }
        }
        return n;
    }
}
