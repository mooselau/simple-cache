package com.crazygoose.starter.cache;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.Getter;


// 缓存清理器
public class CacheClear<K, V> {

    // 使用 delayqueue 来做成高效率的 延迟队列，当满足过期时间的时候完成自动清除
    @Getter
    private final DelayQueue<DelayedCacheEntry<K, V>> expireQueue = new DelayQueue<>();
    // 缓存清理 单线程
    private final ExecutorService cacheClearExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        // 定时清理线程，Daemon 线程 -- 不阻拦JVM退出
        t.setDaemon(true);
        t.setName("CacheClear-Thread");
        return t;
    });

    private final ICache<K, V> cache;

    public CacheClear(ICache<K, V> cache) {
        this.cache = cache;
        cacheClearExecutor.execute(this::cacheClear);
    }

    public void add(K key, CacheEntry<V> entry) {
        // 包装成 delayed entry 入queue
        expireQueue.offer(new DelayedCacheEntry<>(key, entry));
    }

    /**
     * This method is responsible for clearing cached entries that have expired.
     * It continuously checks an expiration queue for entries that are ready to be removed from the main cache.
     * The method runs in a loop until the current thread is interrupted.
     */
    private void cacheClear() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    DelayedCacheEntry<K, V> delayedEntry = expireQueue.take();
                    cache.remove(delayedEntry.getKey()); // 从缓存中移除
                } catch (InterruptedException e) {
                    // TODO: log here
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } finally {
            cacheClearExecutor.shutdown(); // 确保 ExecutorService 关闭
        }
    }

}