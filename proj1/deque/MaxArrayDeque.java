package deque;

import java.util.Comparator;

/** Inherits all methods that an ArrayDeque has, with additional methods on comparator*/
public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private T[] items;
    private int size;
    private int nextFirst;
    private int nextLast;
    private Comparator<T> comp;
    public MaxArrayDeque(Comparator<T> c) {
        nextFirst = 4;
        nextLast = 5;
        items = (T[]) new Object[8];
        size = 0;
        comp = c;
    }

    /** Returns the maximum element in the deque as governed by Comparator*/
    public T max() {
        if (isEmpty()) {
            return null;
        }
        T greatest = get(0);
        for (int i = 0; i < size(); i++) {
            T curVal = get(i);
            if (comp.compare(curVal, greatest) > 0) {
                greatest = curVal;
            }
        }
        return greatest;
    }

    /** Returns the max element in the deque governed by c*/
    public T max(Comparator<T> c) {
        if (isEmpty()) {
            return null;
        }
        T greatest = get(0);
        for (int i = 0; i < size(); i++) {
            T curVal = get(i);
            if (c.compare(curVal, greatest) > 0) {
                greatest = curVal;
            }
        }
        return greatest;
    }
}
