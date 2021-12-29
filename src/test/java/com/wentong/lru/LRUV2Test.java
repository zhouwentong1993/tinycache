package com.wentong.lru;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LRUV2Test {

    @Test
    void testGetWhenNoElement() {
        LRUV2<String, String> lru = new LRUV2<>();
        String a = lru.get("a");
        assertNull(a);
    }

    @Test
    void testPut() {
        LRUV2<String, String> lru = new LRUV2<>();
        lru.put("a", "a");
        assertEquals("a", lru.get("a"));
    }

    @Test
    void testSameElementPut() {
        LRUV2<String, String> lru = new LRUV2<>();
        lru.put("a", "a");
        lru.put("a", "b");
        assertEquals(1, lru.size());
        assertEquals("b", lru.get("a"));
    }

    @Test
    void testKeysOrder() {
        LRUV2<String, String> lru = new LRUV2<>();
        lru.put("a", "a");
        lru.put("b", "b");
        lru.put("a", "b");
        List<String> orders = lru.keyOrders();
        assertEquals("a", orders.get(0));
    }

    @Test
    void testNoNeedEvict() {
        LRUV2<String, String> lru = new LRUV2<>();
        lru.setMaxCapacity(5);
        lru.put("a", "a");
        lru.put("b", "b");
        lru.put("c", "c");
        lru.put("d", "d");
        lru.put("e", "e");
        lru.put("a", "a");
        List<String> orders = lru.keyOrders();
        assertEquals("a", orders.get(0));
        assertEquals("e", orders.get(1));
        assertEquals("b", orders.get(4));
    }

    @Test
    void testNeedEvict() {
        LRUV2<String, String> lru = new LRUV2<>();
        lru.setMaxCapacity(5);
        lru.put("a", "a");
        lru.put("b", "b");
        lru.put("c", "c");
        lru.put("d", "d");
        lru.put("e", "e");
        lru.put("a", "a");
        lru.put("f", "f");
        List<String> orders = lru.keyOrders();
        assertEquals("a", orders.get(1));
        assertEquals("e", orders.get(2));
        assertFalse(orders.contains("b"));
    }


    @Test
    void testRemoveLastNode() {
        LRUV2<String, String> lru = new LRUV2<>();
        lru.setMaxCapacity(5);
        lru.put("a", "a");
        lru.put("b", "b");
        lru.put("c", "c");
        lru.put("d", "d");
        lru.put("e", "e");
        lru.put("b", "b");
        List<String> orders = lru.keyOrders();
        assertEquals("b", orders.get(0));
    }

    @Test
    void testRemove() {
        LRUV2<String, String> lru = new LRUV2<>();
        lru.put("a", "a");
        lru.put("b", "b");
        lru.put("c", "c");
        lru.put("d", "d");
        lru.remove("b");
        List<String> orders = lru.keyOrders();
        assertFalse(orders.contains("b"));
        assertEquals("d", orders.get(0));
        assertEquals("c", orders.get(1));
        assertEquals("a", orders.get(2));
    }


}
