package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private class ListNode {
        private T item;
        private ListNode prev;
        private ListNode next;

        private ListNode(T i, ListNode p, ListNode n) {
            item = i;
            prev = p;
            next = n;
        }
    }
    /* The first item (if it exists) is at sentinel.next */
    private ListNode sentinel;
    private int size;

    /** Creates an empty LinkedListDeque*/
    public LinkedListDeque() {
        sentinel = new ListNode(null, null, null);
        size = 0;
    }

    /* Adds an item of type T to the front of the deque.*/
    @Override
    public void addFirst(T item) {
        ListNode newNode = new ListNode(item, null, null);
        ListNode firstNode = (size > 0) ? sentinel.next : newNode;
        ListNode lastNode = (size > 0) ? sentinel.next.prev : newNode;
        sentinel.next = newNode;
        firstNode.prev = newNode;
        lastNode.next = newNode;
        newNode.prev = lastNode;
        newNode.next = firstNode;
        size++;
    }

    /** Adds an item of type T to the back of the deque */
    @Override
    public void addLast(T item) {
        ListNode newNode = new ListNode(item, null, null);
        ListNode firstNode = (size > 0) ? sentinel.next : newNode;
        ListNode lastNode = (size > 0) ? sentinel.next.prev : newNode;
        if (isEmpty()) {
            sentinel.next = newNode;
        } else {
            sentinel.next.prev = newNode;
        }
        firstNode.prev = newNode;
        lastNode.next = newNode;
        newNode.prev = lastNode;
        newNode.next = firstNode;
        size++;
    }

    /** Returns the number of items in the deque.*/
    @Override
    public int size() {
        return size;
    }

    /**
     * Prints the items in the deque from first to last, separated by a space.
     * Once all the items have been printed, print out a new line.
     */
    @Override
    public void printDeque() {
        ListNode p = sentinel;
        for (int i = 0; i < size; i++) {
            System.out.print(p.item + " ");
            p = p.next;
        }
        System.out.println();
    }

    /** Removes and returns the item at the front of the deque.*/
    @Override
    public T removeFirst() {
        if (size < 1) {
            return null;
        } else if (size == 1) {
            ListNode firstNode = sentinel.next;
            sentinel.next = new ListNode(null, null, null);
            size--;
            return firstNode.item;
        }
        ListNode firstNode = sentinel.next;
        ListNode secondNode = firstNode.next;
        ListNode lastNode = firstNode.prev;
        sentinel.next = secondNode;
        lastNode.next = secondNode;
        secondNode.prev = lastNode;
        firstNode.next = null;
        size--;
        return firstNode.item;
    }

    /** Removes and returns the item at the back of the deque.*/
    @Override
    public T removeLast() {
        if (size < 1) {
            return null;
        } else if (size == 1) {
            ListNode lastNode = sentinel.next;
            sentinel.next = new ListNode(null, null, null);
            size--;
            return lastNode.item;
        }
        ListNode lastNode = sentinel.next.prev;
        ListNode secondToLastNode = lastNode.prev;
        ListNode firstNode = sentinel.next;
        secondToLastNode.next = firstNode;
        firstNode.prev = secondToLastNode;
        lastNode.prev = null;
        size--;
        return lastNode.item;
    }

    /** Gets the item at the given index. If no such item exists, return null*/
    @Override
    public T get(int index) {
        ListNode p = sentinel;
        for (int i = 0; i <= index; i++) {
            p = p.next;
        }
        return p.item;
    }

    private T getRecursive(int index, ListNode p) {
        if (index >= 0) {
            return getRecursive(index - 1, p.next);
        }
        return p.item;
    }
    public T getRecursive(int index) {
        if (index >= size) {
            return null;
        }
        ListNode p = sentinel;
        return getRecursive(index, p);
    }

    /** Use this method to return an iterator */
    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    private class LinkedListDequeIterator implements Iterator<T> {
        private int wizPos;
        public LinkedListDequeIterator() {
            wizPos = 0;
        }
        public boolean hasNext() {
            return wizPos < size;
        }

        public T next() {
            T returnItem = get(wizPos);
            wizPos += 1;
            return returnItem;
        }
    }

    /** Returns whether the parameter o is equal to the Deque*/
    public boolean equals(Object o) {
        if (o instanceof LinkedListDeque && ((LinkedListDeque<?>) o).size() == size) {
            ListNode p = sentinel.next;
            for (int i = 0; i < size; i++) {
                if (!p.item.equals(((LinkedListDeque<?>) o).get(i))) {
                    return false;
                }
                p = p.next;
            }
            return true;
        }
        return false;
    }
}
