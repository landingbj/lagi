package ai.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LRUCacheTest {
    private LRUCache<String, Integer> cache;

    @BeforeEach
    void setUp() {
        cache = new LRUCache<>(2);
    }

    @Test
    void testPutAndGet() {
        cache.put("key1", 1);
        assertEquals(1, cache.get("key1"));
    }

    @Test
    void testLRUOrder() {
        cache.put("key1", 1);
        cache.put("key2", 2);
        cache.put("key3", 3);
        assertNull(cache.get("key1"));
        assertNotNull(cache.get("key2"));
        assertNotNull(cache.get("key3"));
    }

    @Test
    void testRemove() {
        cache.put("key1", 1);
        cache.remove("key1");
        assertNull(cache.get("key1"));
    }
}