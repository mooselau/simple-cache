import static org.junit.Assert.assertEquals;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.crazygoose.starter.cache.EvictionPolicy;
import com.crazygoose.starter.cache.ICache;
import com.crazygoose.starter.cache.SimpleCache;
import org.junit.Test;


public class SimpleCacheTest {

    @Test
    public void testLRU() {
        ICache<String, String> cache = new SimpleCache<>(2, EvictionPolicy.LRU);
        cache.put("a", "1", 10000);
        cache.put("b", "2", 10000);
        cache.get("a"); // 访问 "a"，使其成为最近使用的
        cache.put("c", "3", 10000); // 缓存已满，"b" 应被淘汰

        assertEquals(Optional.empty(), cache.get("b"));
        assertEquals(Optional.of("1"), cache.get("a"));
        assertEquals(Optional.of("3"), cache.get("c"));
    }

    @Test
    public void testFIFO(){
        ICache<String, String> cache = new SimpleCache<>(2, EvictionPolicy.FIFO);
        cache.put("a", "1", 10000);
        cache.put("b", "2", 10000);
        cache.put("c", "3", 10000);

        assertEquals(Optional.empty(), cache.get("a"));
        assertEquals(Optional.of("2"), cache.get("b"));
        assertEquals(Optional.of("3"), cache.get("c"));
    }

    @Test
    public void testNone(){
        ICache<String, String> cache = new SimpleCache<>(2, EvictionPolicy.NONE);
        cache.put("a", "1", 10000);
        cache.put("b", "2", 10000);
        cache.put("c", "3", 10000); // NONE 策略达到容量则不插入新值

        assertEquals(Optional.of("1"), cache.get("a"));
        assertEquals(Optional.of("2"), cache.get("b"));
        assertEquals(Optional.empty(), cache.get("c"));
        assertEquals(2, cache.size()); // size 应该为2
    }

    @Test
    public void testPutAndGet() {
        ICache<String, String> cache = new SimpleCache<>(3, EvictionPolicy.LRU);
        cache.put("key1", "value1", 10000);
        assertEquals(Optional.of("value1"), cache.get("key1"));
        assertEquals(Optional.empty(), cache.get("key2")); // 获取不存在的 key 应返回 empty Optional
    }

    @Test
    public void testExpiration() throws InterruptedException {
        ICache<String, String> cache = new SimpleCache<>(3, EvictionPolicy.LRU);
        cache.put("key1", "value1", 100); // 设置过期时间为 100 毫秒
        Thread.sleep(200); // 等待足够长时间使 key1 过期
        assertEquals(Optional.empty(), cache.get("key1")); // 过期后获取应返回 empty Optional
    }

    @Test
    public void testRemove() {
        ICache<String, String> cache = new SimpleCache<>(3, EvictionPolicy.LRU);
        cache.put("key1", "value1", 10000);
        cache.remove("key1");
        assertEquals(Optional.empty(), cache.get("key1"));
    }

    @Test
    public void testClear() {
        ICache<String, String> cache = new SimpleCache<>(3, EvictionPolicy.LRU);
        cache.put("key1", "value1", 10000);
        cache.put("key2", "value2", 10000);
        cache.clear();
        assertEquals(0, cache.size());
        assertEquals(Optional.empty(), cache.get("key1")); // Use Optional.empty()
        assertEquals(Optional.empty(), cache.get("key2")); // Use Optional.empty()
    }

    @Test
    public void testPutAllAndRemoveAll() {
        ICache<String, String> cache = new SimpleCache<>(3, EvictionPolicy.LRU);
        Map<String, String> entries = new HashMap<>();
        entries.put("key1", "value1");
        entries.put("key2", "value2");
        cache.putAll(entries, 10000);

        assertEquals(Optional.of("value1"), cache.get("key1")); // Use Optional.of()
        assertEquals(Optional.of("value2"), cache.get("key2")); // Use Optional.of()

        Set<String> keysToRemove = new HashSet<>();
        keysToRemove.add("key1");
        keysToRemove.add("key2");
        cache.removeAll(keysToRemove);
        assertEquals(Optional.empty(), cache.get("key1")); // Use Optional.empty()
        assertEquals(Optional.empty(), cache.get("key2")); // Use Optional.empty()
    }

    @Test
    public void testSize() {
        ICache<String, String> cache = new SimpleCache<>(3, EvictionPolicy.LRU);
        cache.put("key1", "value1", 10000);
        assertEquals(1, cache.size());
    }

    @Test
    public void testHitAndMissRatio() {
        ICache<String, String> cache = new SimpleCache<>(3, EvictionPolicy.LRU);
        cache.put("key1", "value1", 10000);
        cache.get("key1"); // hit
        cache.get("key2"); // miss

        // These assertions might be flaky due to concurrent access.  A more robust approach
        // would be to use a larger number of iterations and check for approximate values.
        assertEquals(0.5, cache.getHitRatio(), 0.01);
        assertEquals(0.5, cache.getMissRatio(), 0.01);
    }
}
