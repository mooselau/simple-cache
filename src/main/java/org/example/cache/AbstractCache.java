package org.example.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


public abstract class AbstractCache<K, V> implements ICache<K, V> {

    protected Map<K, CacheEntry<V>> cache = new ConcurrentHashMap<>();
    protected int capacity;
    protected EvictionPolicy evictionPolicy;
    protected final AtomicLong hitCount = new AtomicLong();
    protected final AtomicLong missCount = new AtomicLong();

    private final CacheClear<K, V> cacheClear;

    protected AbstractCache(int capacity, EvictionPolicy evictionPolicy) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.capacity = capacity;
        this.evictionPolicy = evictionPolicy;
        this.cacheClear = new CacheClear<>(this);
    }

    // 提供抽象的淘汰方法，具体策略由子类实现
    protected abstract void evict(K key, CacheEntry<V> entry);

    @Override
    public void put(K key, V value, long expireAfterMillis) {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(value, "value cannot be null");

        // policy 检查处理
        if (cache.size() >= capacity) {
            if (evictionPolicy != EvictionPolicy.NONE) {  // NONE 策略不淘汰
                evict(key, new CacheEntry<>()); // 执行淘汰逻辑，由子类实现
            } else {
                return; // NONE 策略达到容量则直接返回，不插入新值
            }
        }

        // 组装 entry
        CacheEntry<V> entry = new CacheEntry<>();
        entry.setValue(value);
        entry.setExpireAt(System.currentTimeMillis() + expireAfterMillis);
        cache.put(key, entry);

        // 使用 CacheClear 添加到过期队列
        cacheClear.add(key, entry);
    }

    // TODO: 补充日志
    @Override
    public Optional<V> get(K key) {
        CacheEntry<V> entry = cache.get(key);

        if (Objects.isNull(entry)) {
            missCount.incrementAndGet();
            return Optional.empty();
        }

        // found but expired
        if (System.currentTimeMillis() > entry.getExpireAt()) {
            cache.remove(key);
            missCount.incrementAndGet();
            return Optional.empty();
        } else {
            // found and valid
            hitCount.incrementAndGet();
            return Optional.of(entry.getValue());
        }
    }

    @Override
    public void remove(K key) {
        cache.remove(key);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public double getHitRatio() {
        long hits = hitCount.get();
        long misses = missCount.get();
        long total = hits + misses;
        return total == 0 ? 0 : (double) hits / total;
    }

    @Override
    public double getMissRatio() {
        return 1 - getHitRatio();
    }

    @Override
    public Map<K, CacheEntry<V>> getAllEntries() {
        return Collections.unmodifiableMap(new HashMap<>(cache)); // 返回一个不可修改的副本
    }

    @Override
    public void putAll(Map<K, V> entries, long expireAfterMillis) {
        entries.forEach((key, value) -> put(key, value, expireAfterMillis));
    }

    @Override
    public void removeAll(Set<K> keys) {
        cache.keySet().removeAll(keys);
    }
}
