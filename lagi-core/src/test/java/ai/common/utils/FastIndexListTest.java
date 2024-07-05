package ai.common.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FastIndexListTest {
    private FastIndexList<String> list;

    @BeforeEach
    void setUp() {
        list = new FastIndexList<>();
    }

    @Test
    void testAdd() {
        list.add("element1");
        assertEquals(1, list.size());
        assertEquals(0, list.indexOf("element1"));
    }

    @Test
    void testAddAtIndex() {
        list.add(0, "element1");
        assertEquals(1, list.size());
        assertEquals(0, list.indexOf("element1"));
    }

    @Test
    void testSet() {
        list.add("element1");
        list.set(0, "element2");
        assertEquals(1, list.size());
        assertEquals(0, list.indexOf("element2"));
    }

    @Test
    void testRemoveAtIndex() {
        list.add("element1");
        list.remove(0);
        assertEquals(0, list.size());
        assertEquals(-1, list.indexOf("element1"));
    }

    @Test
    void testRemove() {
        list.add("element1");
        list.remove("element1");
        assertEquals(0, list.size());
        assertEquals(-1, list.indexOf("element1"));
    }

    @Test
    void testClear() {
        list.add("element1");
        list.clear();
        assertEquals(0, list.size());
        assertEquals(-1, list.indexOf("element1"));
    }
}
