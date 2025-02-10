package org.example.cache;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;


public class SimpleCache<K extends Serializable, V extends Serializable> extends AbstractCache<K, V> {

    private Queue<K> fifoQueue; // 用于 FIFO 策略

    public SimpleCache(int capacity, EvictionPolicy evictionPolicy) {
        super(capacity, evictionPolicy);
        if (evictionPolicy == EvictionPolicy.LRU) {
            this.cache = new LinkedHashMap<K, CacheEntry<V>>(capacity + 1, 1.1f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<K, CacheEntry<V>> eldest) {
                    return size() > capacity;
                }
            };
        } else if (evictionPolicy == EvictionPolicy.FIFO) {
            fifoQueue = new LinkedList<>();
        }
    }

    @Override
    protected void evict(K key, CacheEntry<V> entry) {
        switch (evictionPolicy) {
            case FIFO:
                K oldestKey = fifoQueue.poll();
                if (oldestKey != null) {
                    cache.remove(oldestKey);
                }
                fifoQueue.offer(key);  // 新插入的 key 加入队列
                break;
            case LRU:
                // LRU 的淘汰策略已在 LinkedHashMap 中实现，无需在此额外处理
                break;
            case NONE:
                // do nothing
                break;
        }
    }

    @Override
    public void put(K key, V value, long expireAfterMillis) {
        super.put(key, value, expireAfterMillis); // 调用父类的 put 方法
        // 仅在 FIFO的情况下，维护该队列，目的是用于淘汰策略
        if(evictionPolicy == EvictionPolicy.FIFO) {
            fifoQueue.offer(key);
        }
    }

}
