package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author Sophia Xia
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    /** An array/table of Collection<Node> objects. Each collection
     * of Nodes represents a single bucket in the hash table.
     * */
    private Collection<Node>[] buckets;

    /** Number of items currently stored*/
    private int size;

    /** set loadFactor = 0.75
     * (as Java’s built-in HashMap does)*/
    private double loadFactor;

    /** defaults initialSize = 16 and */
    private static final int defaultSize = 16;

    /** Constructors */
    public MyHashMap() {
        this (defaultSize, 0.75);
    }

    public MyHashMap(int initialSize) {
        this (initialSize, 0.75);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        if (initialSize <= 0 || maxLoad <= 0) {
            throw new IllegalArgumentException();
        }
        buckets = createTable(initialSize);
        size = 0;
        loadFactor = maxLoad;
    }

    /**
     * Increase the number of buckets if exceeds max load.
     */
    private void grow() {
        MyHashMap<K, V> newMap = new MyHashMap<>(buckets.length * 2, loadFactor);
        for (K key : keySet()) {
            newMap.put(key, get(key));
        }
        this.buckets = newMap.buckets;
        this.size = newMap.size;
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new ArrayList<Node>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        buckets = new Collection[tableSize];
        for  (int i = 0; i < tableSize; i++) {
            buckets[i] = createBucket();
        }
        return buckets;
    }

    /**
     * The constant 0x7FFFFFFF is a 32-bit integer in hexadecimal with all
     * but the highest bit set (the highest bit).
     * When you use % on negative value, you get a negative value.
     * There are no negative buckets so to avoid this, remove the sign bit and
     * one way of doing this is to use a mask
     * e.g. x & 0x7FFFFFFF which keeps all the bits except the top one.
     * Another way to do this is to shift the output x >>> 1 however this is slower.
     * @param key: find hash for the specified key and avoid integer overflow.
     * @return: the hash of the key object.
     */
    private int hash(K key) {
        return (key == null) ? 0 : (0x7fffffff & key.hashCode()) % buckets.length;
    }

    @Override
    public Iterator<K> iterator() {
        return keySet().iterator();
    }

    /** Remove all mappings from the map. */
    @Override
    public void clear() {
        this.size = 0;
        this.buckets = createTable(defaultSize);
    }

    /** Return True if thi map contains a mapping for the specified key.*/
    @Override
    public boolean containsKey(K key) {
        return keySet().contains(key);
    }

    /** Returns the number of key-value mappings in this map.*/
    @Override
    public int size() { return size; }

    /**
     * Associates the value with the given key in this map.
     * If map previously contained a mapping, old value is replaced.
     * @param key: given key.
     * @param value: mapped value.
     */
    @Override
    public void put(K key, V value) {
        if ( (double) size() / buckets.length > loadFactor) {
            grow();
        }
        int hash = hash(key);
        Node node = createNode(key, value);
        if (!containsKey(key)) {
            size++;
        } else {
            setValue(key, value, buckets[hash]);
        }
        buckets[hash].add(node);
    }

    private void setValue(K key, V val, Collection<Node> bucket) {
        for (Node node : bucket) {
            if (node.key.equals(key)) {
                node.value = val;
            }
        }
    }

    /**
     * Returns the value to which the given key is mapped, or null if
     * there's no mapping found.
     * @param key: specified key
     * @return value of the key maps to
     */
    @Override
    public V get(K key) {
        Collection<Node> bucket = buckets[hash(key)];
        if (bucket != null) {
            for (Node node : bucket) {
                if (node.key.equals(key)) {
                    return node.value;
                }
            }
        }
        return null;
    }

    /** Returns a Set view of the keys contained in the map.*/
    @Override
    public Set<K> keySet() {
        Set<K> keys = new HashSet<>();
        for (Collection<Node> bucket : buckets) {
            for (Node node : bucket) {
                keys.add(node.key);
            }
        }
        return keys;
    }

    /** Removes the mapping for the key.*/
    @Override
    public V remove(K key) {
        if (!containsKey(key)) {
            return null;
        }
        V oldValue = get(key);
        Collection<Node> bucket = buckets[hash(key)];
        if (bucket != null) {
            for (Node node : bucket) {
                if (node.key.equals(key)) {
                    buckets[hash(key)].remove(node);
                    break;
                }
            }
        }
        return oldValue;
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

}
