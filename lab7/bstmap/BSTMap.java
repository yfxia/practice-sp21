package bstmap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Creates a BSTMap class that implements the Map61B interface using a BST
 * as its core data structure. It contains all the methods given in
 * Map61B except for remove, iterator and keySet.
 */
public class BSTMap<K extends Comparable<K>, V extends Comparable<V>> implements Map61B<K, V> {

    private BSTNode root;            // root of BST
    private final Set<K> keys;         // set of keys
    /**
     * Initializes an empty BST tree map table.
     */
    public BSTMap() {
        keys = new HashSet<>();
    }

    /**
     * Maintains a BSTNode binary search tree that stores hashmap keys.
     */
    private class BSTNode {
        private K key;               // sorted by key
        private V value;             // associated data value
        private int size;            // number of nodes in the tree
        private BSTNode left, right; // left and right subtrees

        public BSTNode(K key, V value, int size) {
            this.key = key;
            this.value = value;
            this.size = size;
        }
    }

    @Override
    public void clear() {
        root = null;
        keys.clear();
    }

    @Override
    public boolean containsKey(K key) {
        if (key == null) {
            throw new IllegalArgumentException("argument to containsKey() is null");
        }
        return keys.contains(key);
    }

    @Override
    public V get(K key) {
        return get(root, key);
    }

    private V get(BSTNode node, K key) {
        if (key == null) {
            throw new IllegalArgumentException("argument to get() is null");
        }
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            return get(node.left, key);
        } else if (cmp > 0) {
            return get(node.right, key);
        } else {
            return node.value;
        }
    }

    @Override
    public int size() {
        return size(root);
    }

    private int size(BSTNode node) {
        if (node == null) {
            return 0;
        } else {
            return node.size;
        }
    }

    @Override
    public void put(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("calls put() with a null key");
        }
        root = put(root, key, value);
        keys.add(key);
    }

    private BSTNode put(BSTNode x, K key, V value) {
        if (x == null) {
            return new BSTNode(key, value, 1);
        }
        int cmp = key.compareTo(x.key);
        if (cmp < 0) {
            x.left = put(x.left, key, value);
        } else if (cmp > 0) {
            x.right = put(x.right, key, value);
        } else {
            x.key = key;
        }
        x.size = 1 + size(x.left) + size(x.right);
        return x;
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<K> iterator() {
        throw new UnsupportedOperationException();
    }
}
