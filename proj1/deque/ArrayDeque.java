package deque;

import java.util.Iterator;

/** Array based deque.
 * @author Sophia Xia
 */

/*
 addLast: The next item we add from the back, will go into position nextLast
 addFirst: The next item we add from the front, will go into position nextFirst
 size: The number of items in the core array in the deque should be size

 */
public class ArrayDeque<T> {
    private T[] items;
    private int size;
    private int nextFirst;
    private int nextLast;

    public ArrayDeque() {
        nextFirst = 4;
        nextLast = 5;
        items = (T[]) new Object[8];
        size = 0;
    }

    private void resize(int capacity) {
        T[] a = (T[]) new Object[capacity];
        items = a;
    }

    /** Adds an item of type T to the front of the deque.*/
    public void addFirst(T item) {
        if (size == items.length) {
            resize(size * 2);
        }
        items[nextFirst] = item;
        nextFirst = (nextFirst < 1) ? items.length-1 : nextFirst-1;
        size++;
    }

    public void addLast(T item) {
        if (size == items.length) {
            resize(size * 2);
        }
        items[nextLast] = item;
        nextLast = (nextLast+1 >= items.length) ? 0 : nextLast+1;
        size++;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        for (int i = nextFirst+1; i < nextLast; i++) {
            System.out.println(get(i) + " ");
        }
        System.out.println();
    }

    public T removeFirst() {
        if (size < 1) {
            return null;
        }
        nextFirst = (nextFirst+1 >= items.length) ? 0 : nextFirst+1;
        size--;
        return items[nextFirst];
    }

    public T removeLast() {
        if (size < 1) {
            return null;
        }
        nextLast = (nextLast < 1) ? items.length-1 : nextLast-1;
        size--;
        return items[nextLast];
    }

    public T get(int index) {
        int innerIndex = nextFirst+index+1;
        innerIndex = (innerIndex < items.length) ? innerIndex : innerIndex % items.length;
        return items[innerIndex];
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int index = 0;
            @Override
            public boolean hasNext() {
                return index < size;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    return null;
                }
                return get(index+1);
            }
        };
    }

    public boolean equals(Object o) {
        if (o instanceof ArrayDeque && ((ArrayDeque<?>) o).size() == size) {
            for (int i = 0; i < size; i++) {
                if (get(i) != ((ArrayDeque<?>) o).get(i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}