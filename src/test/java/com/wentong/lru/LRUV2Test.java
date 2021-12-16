package com.wentong.lru;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class LRUV2Test {

    @Test
    public void testGetWhenNoElement() {
        LRUV21<String, String> lru = new LRUV21<>();
        String a = lru.get("a");
        Assert.assertNull(a);
    }

    @Test
    public void testPut() {
        LRUV21<String, String> lru = new LRUV21<>();
        lru.put("a", "a");
        Assert.assertEquals("a", lru.get("a"));
    }

    @Test
    public void testSameElementPut() {
        LRUV21<String, String> lru = new LRUV21<>();
        lru.put("a", "a");
        lru.put("a", "b");
        Assert.assertEquals(1, lru.size());
        Assert.assertEquals("b", lru.get("a"));
    }

    @Test
    public void testKeysOrder() {
        LRUV21<String, String> lru = new LRUV21<>();
        lru.put("a", "a");
        lru.put("b", "b");
        lru.put("a", "b");
        List<String> orders = lru.keyOrders();
        Assert.assertEquals("a", orders.get(0));
    }

    @Test
    public void testNoNeedEvict() {
        LRUV21<String, String> lru = new LRUV21<>();
        lru.setMaxCapacity(5);
        lru.put("a", "a");
        lru.put("b", "b");
        lru.put("c", "c");
        lru.put("d", "d");
        lru.put("e", "e");
        lru.put("a", "a");
        List<String> orders = lru.keyOrders();
        Assert.assertEquals("a", orders.get(0));
        Assert.assertEquals("e", orders.get(1));
        Assert.assertEquals("b", orders.get(4));
    }

    @Test
    public void testNeedEvict() {
        LRUV21<String, String> lru = new LRUV21<>();
        lru.setMaxCapacity(5);
        lru.put("a", "a");
        lru.put("b", "b");
        lru.put("c", "c");
        lru.put("d", "d");
        lru.put("e", "e");
        lru.put("a", "a");
        lru.put("f", "f");
        List<String> orders = lru.keyOrders();
        Assert.assertEquals("a", orders.get(1));
        Assert.assertEquals("e", orders.get(2));
        Assert.assertFalse(orders.contains("b"));
    }


    @Test
    public void testRemoveLastNode() {
        LRUV21<String, String> lru = new LRUV21<>();
        lru.setMaxCapacity(5);
        lru.put("a", "a");
        lru.put("b", "b");
        lru.put("c", "c");
        lru.put("d", "d");
        lru.put("e", "e");
        lru.put("b", "b");
        List<String> orders = lru.keyOrders();
        Assert.assertEquals("b", orders.get(0));
    }


}
