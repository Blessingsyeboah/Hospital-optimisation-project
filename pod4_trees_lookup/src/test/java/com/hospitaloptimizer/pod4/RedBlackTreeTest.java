package com.hospitaloptimizer.pod4;

import java.util.List;
import static com.hospitaloptimizer.shared.util.SimpleTest.*;

public class RedBlackTreeTest {

    public void testInsertAndSearch_normalCase() {
        RedBlackTree<Integer, String> rb = new RedBlackTree<>();
        for (int k : new int[]{5, 3, 8, 1, 4}) rb.insert(k, "v" + k);
        assertEquals("v4", rb.search(4));
        assertNull(rb.search(999));
    }

    /** Before/after rotation diagram evidence: inserting an ascending run into a
     *  plain BST gives height n-1 (a linked list); RedBlackTree keeps it ~log2(n). */
    public void testHeightStaysLogarithmic_vsPlainBST() {
        CustomBST<Integer, String> bst = new CustomBST<>();
        RedBlackTree<Integer, String> rb = new RedBlackTree<>();
        for (int i = 1; i <= 100; i++) {
            bst.insert(i, "v" + i);
            rb.insert(i, "v" + i);
        }
        assertEquals(99, bst.height()); // degenerates into a linked list
        assertTrue(rb.height() <= 2 * (Math.log(101) / Math.log(2)),
                "red-black height " + rb.height() + " should stay O(log n)");
    }

    public void testRotationTrace_recordedOnInsert() {
        RedBlackTree<Integer, String> rb = new RedBlackTree<>();
        rb.insert(1, "a");
        rb.insert(2, "b"); // triggers a left-rotate in LLRB on ascending inserts
        rb.insert(3, "c"); // triggers rotation + color flip
        List<String> trace = rb.lastInsertTrace();
        assertFalse(trace.isEmpty(), "inserting 3rd ascending key should trigger a rotation/flip");
    }

    public void testInvariant_perfectBlackBalance() {
        RedBlackTree<Integer, String> rb = new RedBlackTree<>();
        for (int i = 1; i <= 50; i++) rb.insert(i, "v" + i);
        assertTrue(rb.isBalanced(), "every root-to-null path must have equal black-link count");
    }

    public void testInorderTraversal_sortedOutput() {
        RedBlackTree<Integer, String> rb = new RedBlackTree<>();
        for (int k : new int[]{5, 3, 8, 1, 4, 7, 9}) rb.insert(k, "v" + k);
        assertEquals(List.of(1, 3, 4, 5, 7, 8, 9), rb.inorderKeys());
    }

    public void testBoundaryCase_emptyTree() {
        RedBlackTree<Integer, String> rb = new RedBlackTree<>();
        assertTrue(rb.isEmpty(), "new tree should be empty");
        assertNull(rb.search(1));
    }

    public static void main(String[] args) {
        run(RedBlackTreeTest.class);
    }
}
