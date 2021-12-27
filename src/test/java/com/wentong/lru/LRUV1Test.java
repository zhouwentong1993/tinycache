package com.wentong.lru;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LRUV1Test {

    @Test
    void testGetWhenNoElement() {
        LRUV1<String, String> lru = new LRUV1<>();
        String a = lru.get("a");
        assertNull(a);
    }

    @Test
    void testPut() {
        LRUV1<String, String> lru = new LRUV1<>();
        lru.put("a", "a");
        assertEquals("a", lru.get("a"));
    }

    @Test
    void testSameElementPut() {
        LRUV1<String, String> lru = new LRUV1<>();
        lru.put("a", "a");
        lru.put("a", "b");
        assertEquals(1, lru.size());
        assertEquals("b", lru.get("a"));
    }

    @Test
    void testKeysOrder() {
        LRUV1<String, String> lru = new LRUV1<>();
        lru.put("a", "a");
        lru.put("b", "b");
        lru.put("a", "b");
        List<String> orders = lru.keyOrders();
        assertEquals("a", orders.get(0));
    }

    @Test
    void testNoNeedEvict() {
        LRUV1<String, String> lru = new LRUV1<>();
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
        LRUV1<String, String> lru = new LRUV1<>();
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
        LRUV1<String, String> lru = new LRUV1<>();
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


}
