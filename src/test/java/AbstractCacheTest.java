import static org.junit.Assert.assertEquals;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.example.cache.AbstractCache;
import org.example.cache.CacheEntry;
import org.example.cache.EvictionPolicy;
import org.example.cache.ICache;
import org.junit.Test;


public class AbstractCacheTest {
    // DummyCache for testing AbstractCache's general logic
    static class DummyCache<K, V> extends AbstractCache<K, V> {
        protected DummyCache(int capacity, EvictionPolicy policy) {
            super(capacity, policy);
        }

        @Override
        protected void evict(K key, CacheEntry<V> entry) {
            // Do nothing for this dummy cache
        }
    }

    @Test
    public void testAbstractCachePutAllAndRemoveAll() {
        ICache<String, String> cache = new DummyCache<>(3, EvictionPolicy.LRU);
        Map<String, String> entries = new HashMap<>();
        entries.put("key1", "value1");
        entries.put("key2", "value2");
        cache.putAll(entries, 10000);

        assertEquals(Optional.of("value1"), cache.get("key1"));
        assertEquals(Optional.of("value2"), cache.get("key2"));

        Set<String> keysToRemove = new HashSet<>();
        keysToRemove.add("key1");
        keysToRemove.add("key2");
        cache.removeAll(keysToRemove);
        assertEquals(Optional.empty(), cache.get("key1"));
        assertEquals(Optional.empty(), cache.get("key2"));
    }


    @Test
    public void testHitAndMissRatio() {
        ICache<String, String> cache = new DummyCache<>(3, EvictionPolicy.LRU);
        cache.put("key1", "value1", 10000);
        assertEquals(Optional.of("value1"), cache.get("key1")); // hit
        assertEquals(Optional.empty(), cache.get("key2")); // miss

        assertEquals(0.5, cache.getHitRatio(), 0.001); // delta = 0.001
        assertEquals(0.5, cache.getMissRatio(), 0.001); // delta = 0.001
    }

}