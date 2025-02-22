import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.crazygoose.starter.cache.EvictionPolicy;
import com.crazygoose.starter.cache.ICache;
import com.crazygoose.starter.cache.SimpleCache;
import org.junit.Test;


public class BoundaryConditionTest {

    @Test
    public void testZeroCapacity() {
        ICache<String, String> cache = new SimpleCache<>(0, EvictionPolicy.LRU);
        cache.put("key1", "value1", 10000);
        assertEquals(Optional.empty(), cache.get("key1")); // 容量为 0，put 操作无效
    }

    @Test
    public void testNegativeCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new SimpleCache<>(-1, EvictionPolicy.LRU));
    }

    @Test
    public void testZeroExpiration() throws InterruptedException {
        ICache<String, String> cache = new SimpleCache<>(3, EvictionPolicy.LRU);
        cache.put("key1", "value1", 0); // 过期时间为 0
        Thread.sleep(100); // 稍等片刻
        assertEquals(Optional.empty(), cache.get("key1")); // 应立即过期
    }

    @Test
    public void testNullKeyAndValue() {
        ICache<String, String> cache = new SimpleCache<>(3, EvictionPolicy.LRU);
        assertThrows(NullPointerException.class, () -> cache.put(null, "value1", 10000));
        assertThrows(NullPointerException.class, () -> cache.put("key1", null, 10000));

        Map<String, String> entries = new HashMap<>();
        entries.put("key1", null);
        assertThrows(NullPointerException.class, () -> cache.putAll(entries, 1000));
    }

}