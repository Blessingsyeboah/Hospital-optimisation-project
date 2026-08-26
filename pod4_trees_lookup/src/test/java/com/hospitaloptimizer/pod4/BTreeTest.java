package com.hospitaloptimizer.pod4;

import static com.hospitaloptimizer.shared.util.SimpleTest.*;

public class BTreeTest {

    public void testInsertAndSearch_normalCase() {
        BTree<Integer, String> bt = new BTree<>(4);
        for (int k : new int[]{10, 20, 5, 6, 12, 30, 7, 17}) bt.insert(k, "v" + k);
        assertEquals("v12", bt.search(12));
        assertNull(bt.search(999));
    }

    public void testNodeSplit_occursOnOverflow() {
        BTree<Integer, String> bt = new BTree<>(3); // order 3 -> max 2 keys/node before split
        bt.insert(1, "a");
        bt.insert(2, "b");
        bt.insert(3, "c"); // should overflow root and split
        assertFalse(bt.lastInsertSplitTrace().isEmpty(), "3rd insert into order-3 tree should split");
        assertEquals("c", bt.search(3));
    }

    public void testHeightGrowsSlowly_manyKeys() {
        BTree<Integer, String> bt = new BTree<>(5);
        for (int i = 0; i < 200; i++) bt.insert(i, "v" + i);
        assertTrue(bt.height() <= 5, "B-tree of order 5 with 200 keys should stay very shallow, got height " + bt.height());
        assertEquals("v150", bt.search(150));
    }

    public void testBoundaryCase_emptyTree() {
        BTree<Integer, String> bt = new BTree<>(4);
        assertNull(bt.search(1));
        assertEquals(0, bt.height());
    }

    public void testInvalidInput_orderTooSmallThrows() {
        assertThrows(IllegalArgumentException.class, () -> new BTree<Integer, String>(2));
    }

    public static void main(String[] args) {
        run(BTreeTest.class);
    }
}
