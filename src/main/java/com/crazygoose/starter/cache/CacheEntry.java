package com.crazygoose.starter.cache;

import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
// 最基本的缓存项
public class CacheEntry<V> {
    private V value;
    private long expireAt;
}
