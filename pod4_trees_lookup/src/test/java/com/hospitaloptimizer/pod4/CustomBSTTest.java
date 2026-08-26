package com.hospitaloptimizer.pod4;

import java.util.List;
import static com.hospitaloptimizer.shared.util.SimpleTest.*;

public class CustomBSTTest {

    public void testInsertAndSearch_normalCase() {
        CustomBST<Integer, String> bst = new CustomBST<>();
        bst.insert(5, "five"); bst.insert(3, "three"); bst.insert(8, "eight");
        assertEquals("three", bst.search(3));
        assertEquals("eight", bst.search(8));
        assertNull(bst.search(100));
    }

    public void testInorderTraversal_sortedOutput() {
        CustomBST<Integer, String> bst = new CustomBST<>();
        int[] keys = {5, 3, 8, 1, 4, 7, 9};
        for (int k : keys) bst.insert(k, "v" + k);
        List<Integer> inorder = bst.inorderKeys();
        assertEquals(List.of(1, 3, 4, 5, 7, 8, 9), inorder);
    }

    public void testSearchPath_trace() {
        CustomBST<Integer, String> bst = new CustomBST<>();
        for (int k : new int[]{5, 3, 8, 1, 4}) bst.insert(k, "v" + k);
        List<Integer> path = bst.searchPath(4);
        assertEquals(List.of(5, 3, 4), path);
    }

    public void testBoundaryCase_emptyTree() {
        CustomBST<Integer, String> bst = new CustomBST<>();
        assertNull(bst.search(1));
        assertTrue(bst.inorderKeys().isEmpty(), "empty tree has no inorder keys");
        assertEquals(-1, bst.height());
    }

    public void testDuplicateKeys_updatesValue() {
        CustomBST<Integer, String> bst = new CustomBST<>();
        bst.insert(5, "first");
        bst.insert(5, "second");
        assertEquals("second", bst.search(5));
        assertEquals(1, bst.size());
    }

    public static void main(String[] args) {
        run(CustomBSTTest.class);
    }
}
