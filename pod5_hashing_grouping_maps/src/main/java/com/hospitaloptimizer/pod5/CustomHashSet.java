package com.hospitaloptimizer.pod5;

import java.util.List;

/** Pod 5 -- Set on top of the custom hash table (per Section 6: "custom set/map on top of hash table"). */
public class CustomHashSet<T> {

    private final CustomHashMap<T, Boolean> map = new CustomHashMap<>();

    public boolean add(T value) {
        boolean isNew = !map.containsKey(value);
        map.put(value, Boolean.TRUE);
        return isNew;
    }

    public boolean contains(T value) { return map.containsKey(value); }

    public boolean remove(T value) { return map.remove(value) != null; }

    public int size() { return map.size(); }
    public boolean isEmpty() { return map.isEmpty(); }
    public List<T> values() { return map.keys(); }
}
