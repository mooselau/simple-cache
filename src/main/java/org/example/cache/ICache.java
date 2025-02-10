package org.example.cache;

import java.util.Map;
import java.util.Optional;
import java.util.Set;


public interface ICache<K, V> {
    // 放入缓存，指定过期时间（单位：毫秒）
    void put(K key, V value, long expireAfterMillis);

    // 获取缓存值
    Optional<V> get(K key);

    // 删除缓存
    void remove(K key);

    // 清空所有缓存
    void clear();

    // 获取缓存大小
    int size();

    // 获取命中率
    double getHitRatio();

    // 获取丢失率
    double getMissRatio();

    // 获取所有缓存项及其过期时间
    Map<K, CacheEntry<V>> getAllEntries();

    // 批量放入缓存
    void putAll(Map<K, V> entries, long expireAfterMillis);

    // 批量删除缓存
    void removeAll(Set<K> keys);
}
