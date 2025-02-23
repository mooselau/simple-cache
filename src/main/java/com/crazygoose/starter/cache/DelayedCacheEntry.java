package com.crazygoose.starter.cache;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;


public class DelayedCacheEntry<K, V> implements Delayed {
    private final K key;
    private final CacheEntry<V> entry;

    public DelayedCacheEntry(K key, CacheEntry<V> entry) {
        this.key = key;
        this.entry = entry;
    }

    public K getKey() {
        return key;
    }

    // 获取元素的到期时间
    @Override
    public long getDelay(TimeUnit unit) {
        // 和当前时间比较，当结果小于等于0时候才会取出元素
        return unit.convert(entry.getExpireAt() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    // 确定元素的过期排列顺序
    @Override
    public int compareTo(Delayed o) {
        return Long.compare(this.getDelay(TimeUnit.MILLISECONDS), o.getDelay(TimeUnit.MILLISECONDS));
    }
}