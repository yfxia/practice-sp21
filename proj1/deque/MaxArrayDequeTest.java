package deque;

import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.*;


public class MaxArrayDequeTest {

    class SortByString implements Comparator<String> {
        @Override
        public int compare(String a, String b) {
            return a.compareTo(b);
        }
    }

    class SortByNumeric implements Comparator<Integer> {
        @Override
        public int compare(Integer a, Integer b) {
            return a.compareTo(b);
        }
    }

    class SortByChar implements Comparator<Character> {
        @Override
        public int compare(Character a, Character b) {
            return a.compareTo(b);
        }
    }

    class SortByCharReversed implements Comparator<Character> {
        @Override
        public int compare(Character a, Character b) {
            int comp = a.compareTo(b);
             if (comp > 0) {
                 return -1;
             } else if (comp < 0) {
                 return 1;
             }
             return 0;
        }
    }

    @Test
    public void addRemoveIsEmptySizeTest() {
        MaxArrayDeque<String> ma1 = new MaxArrayDeque<>(new SortByString());

        assertTrue("A newly initialized MaxArrayDeque should be Empty", ma1.isEmpty());
        ma1.addLast("a");
        ma1.addLast("b");

        assertEquals(2, ma1.size());
        ma1.removeLast();
        ma1.removeLast();
        assertTrue("Remove twice MaxArrayDeque should be empty", ma1.isEmpty());
    }

    @Test
    public void findMaxElementTest() {
        MaxArrayDeque<Integer> ma1 = new MaxArrayDeque<>(new SortByNumeric());
        ma1.addFirst(100);
        ma1.addLast(99);
        ma1.addFirst(10001);
        assertEquals("Maximum item using constructor comparator should 10001", 10001, (int) ma1.max());

        MaxArrayDeque<Integer> ma2 = new MaxArrayDeque<>(new SortByNumeric());
        for (int i = 0; i < 1000001; i++) {
            ma2.addLast(i);
        }
        assertEquals("Maximum item using constructor comparator should 1000000", 1000000, (int) ma2.max());
    }

    @Test
    public void findMaxElementCustomComparatorTest(){
        MaxArrayDeque<Character> ma1 = new MaxArrayDeque<>(new SortByChar());
        ma1.addFirst('z');
        for (int i = 0; i < 26; i++) {
            ma1.addLast((char) ('a' + i));
        }
        assertEquals("Maximum item using custom reversed comparator should be a", 'a', (int) ma1.max(new SortByCharReversed()));
    }
}