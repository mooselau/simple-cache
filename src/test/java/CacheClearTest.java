import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.example.cache.CacheClear;
import org.example.cache.CacheEntry;
import org.example.cache.ICache;
import org.junit.Test;
import org.mockito.Mockito;


public class CacheClearTest {

    @Test
    public void testAddAndCacheClear() throws InterruptedException {
        ICache<String, String> mockCache = Mockito.mock(ICache.class);
        CacheClear<String, String> cacheClear = new CacheClear<>(mockCache);

        CacheEntry<String> entry = new CacheEntry<>();
        entry.setValue("value1");
        entry.setExpireAt(System.currentTimeMillis() + 100);
        cacheClear.add("key1", entry);

        Thread.sleep(200); // 等待条目过期

        verify(mockCache, times(1)).remove("key1");
    }

}