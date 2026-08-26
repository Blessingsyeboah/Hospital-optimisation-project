package com.hospitaloptimizer.pod4;

import com.hospitaloptimizer.shared.model.ServiceRequest;

import java.util.List;

/**
 * Pod 4 -- builds search indexes over service requests using the BST,
 * Red-Black tree and B-tree, so requests can be looked up by id in
 * O(log n) instead of scanning the whole list.
 */
public class IndexEngine {

    private final CustomBST<String, ServiceRequest> bstIndex = new CustomBST<>();
    private final RedBlackTree<String, ServiceRequest> rbIndex = new RedBlackTree<>();
    private final BTree<String, ServiceRequest> btreeIndex = new BTree<>(5);

    public void buildAll(List<ServiceRequest> requests) {
        for (ServiceRequest r : requests) {
            bstIndex.insert(r.getRequestId(), r);
            rbIndex.insert(r.getRequestId(), r);
            btreeIndex.insert(r.getRequestId(), r);
        }
    }

    public ServiceRequest findByBST(String requestId) { return bstIndex.search(requestId); }
    public ServiceRequest findByRedBlackTree(String requestId) { return rbIndex.search(requestId); }
    public ServiceRequest findByBTree(String requestId) { return btreeIndex.search(requestId); }

    public int bstHeight() { return bstIndex.height(); }
    public int redBlackHeight() { return rbIndex.height(); }
    public int bTreeHeight() { return btreeIndex.height(); }
}
