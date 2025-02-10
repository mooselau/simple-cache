import static org.junit.Assert.assertEquals;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.example.cache.EvictionPolicy;
import org.example.cache.ICache;
import org.example.cache.SimpleCache;
import org.junit.Test;


public class ConcurrencyTest {
    @Test
    public void testConcurrentPutAndGet() throws InterruptedException {
        ICache<String, Integer> cache = new SimpleCache<>(100, EvictionPolicy.LRU);
        int numThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        AtomicInteger counter = new AtomicInteger(0);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                for (int j = 0; j < 1000; j++) {
                    int value = counter.incrementAndGet();
                    cache.put("key" + value, value, 10000);
                    assertEquals(Optional.of(value), cache.get("key" + value));
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
    }
}