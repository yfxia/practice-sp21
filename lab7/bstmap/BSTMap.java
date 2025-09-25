package bstmap;

import java.util.*;

/**
 * Creates a BSTMap class that implements the Map61B interface using a BST
 * as its core data structure. It contains all the methods given in
 * Map61B except for remove, iterator and keySet.
 */
public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {

    private BSTNode root;            // root of BST
    /**
     * Initializes an empty BST tree map table.
     */
    public BSTMap() {
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
    }

    @Override
    public boolean containsKey(K key) {
        if (key == null) {
            throw new IllegalArgumentException("argument to containsKey() is null");
        }
        return keySet().contains(key);
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
        if (isEmpty()) {
            return Collections.emptySet();
        }
        Set<K> set = new HashSet<>();
        for (K key : keys(min(), max())) {
            set.add(key);
        }
        return set;
    }

    @Override
    public V remove(K key) {
        if (!containsKey(key)) {
            return null;
        }
        V val = get(key);
        root = remove(root, key);
        return val;
    }

    private BSTNode remove(BSTNode node, K key) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = remove(node.left, key);
        } else if (cmp > 0) {
            node.right = remove(node.right, key);
        } else {
            if (node.right == null) {
                return node.left;
            }
            if (node.left == null) {
                return node.right;
            }
            BSTNode temp = node;
            node = min(temp.right);
            node.right = removeMin(temp.right);
            node.left = temp.left;
        }
        node.size =  1 + size(node.left) + size(node.right);
        return node;
    }

    private BSTNode removeMin(BSTNode node) {
        if (node.left == null) {
            return node.right;
        }
        node.left = removeMin(node.left);
        node.size = 1 + size(node.left) + size(node.right);
        return node;
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<K> iterator() {
        return keys().iterator();
    }

    private boolean isEmpty() {
        return size() == 0;
    }

    private K min() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return min(root).key;
    }

    private BSTNode min(BSTNode node) {
        if (node.left == null) {
            return node;
        }
        return min(node.left);
    }

    private K max() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return max(root).key;
    }

    private BSTNode max(BSTNode node) {
        if (node.right == null) {
            return node;
        }
        return max(node.right);
    }

    private Iterable<K> keys() {
        if (isEmpty()) {
            return new ArrayDeque<>();
        }
        return keys(min(), max());
    }

    private Iterable<K> keys(K lo, K hi) {
        if (lo == null) {
            throw new IllegalArgumentException("argument to keys() is null");
        }
        if (hi == null) {
            throw new IllegalArgumentException("argument to keys() is null");
        }
        ArrayDeque<K> queue = new ArrayDeque<>();
        keys(root, queue, lo, hi);
        return queue;
    }

    private void keys(BSTNode node, ArrayDeque<K> queue, K lo, K hi) {
        if (node == null) {
            return;
        }
        int cmplo = lo.compareTo(node.key);
        int cmphi = hi.compareTo(node.key);
        if (cmplo < 0) {
            keys(node.left, queue, lo, hi);
        }
        if (cmplo <= 0 && cmphi >= 0) {
            queue.addFirst(node.key);
        }
        if (cmphi > 0) {
            keys(node.right, queue, lo, hi);
        }
    }

    private void traverseBST(BSTNode node, List<K> sortedKeys) {
        if (node == null) {
            return;
        }
        traverseBST(node.left, sortedKeys);
        sortedKeys.add(node.key);
        traverseBST(node.right, sortedKeys);
    }

    public void printInOrder() {
        List<K> sortedK = new ArrayList<>();
        traverseBST(root, sortedK);
        for (K key : sortedK) {
            System.out.print(key + " ");
        }
    }

}
