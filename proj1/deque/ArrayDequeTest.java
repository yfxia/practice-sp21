package deque;


import org.junit.Test;

import java.lang.reflect.Array;

import static org.junit.Assert.*;


public class ArrayDequeTest {
    @Test
    public void addIsEmptySizeTest() {
        ArrayDeque<String> ad1 = new ArrayDeque<>();
        
        assertTrue("A newly initialized ArrayDeque should be Empty", ad1.isEmpty());
        ad1.addLast("a");
        ad1.addLast("b");
        
        assertEquals(2, ad1.size());
        assertFalse("ad1 should contain 2 items", ad1.isEmpty());
        
        ad1.addFirst("c");
        assertEquals(3, ad1.size());

        ad1.printDeque();
        
    }

    @Test
    public void addRemoveTest() {
        ArrayDeque<Integer> ad1 = new ArrayDeque<>();
        ad1.addFirst(10);
        assertFalse("ad1 should contain 1 item", ad1.isEmpty());
        ad1.removeFirst();
        assertTrue("ad1 should be Empty after removal", ad1.isEmpty());

        ArrayDeque<Integer> ad2 = new ArrayDeque<>();
        ad1.addLast(10);
        assertFalse("ad1 should contain 1 item", ad1.isEmpty());
        ad1.removeLast();
        assertTrue("ad1 should be Empty after removal", ad1.isEmpty());
    }

    @Test
    public void removeEmptyTest() {
        ArrayDeque<Integer> ad1 = new ArrayDeque<>();
        ad1.addFirst(3);
        ad1.removeLast();
        ad1.removeFirst();
        ad1.removeLast();
        ad1.removeFirst();

        int size = ad1.size();
        String errorMsg = "  Bad size returned when removing from Empty deque.\n";
        errorMsg += "  student size() returned " + size + "\n";
        errorMsg += "  actual size() returned 0\n";

        assertEquals(errorMsg, 0, size);
        
    }

    @Test
    public void multipleParamTest() {
        ArrayDeque<String> ad1 = new ArrayDeque<>();
        ArrayDeque<Double> ad2 = new ArrayDeque<>();
        ArrayDeque<Boolean> ad3 = new ArrayDeque<>();

        ad1.addLast("a");
        ad1.addLast("b");
        ad1.addFirst("c");
        ad1.addLast("d");
        ad1.addLast("e");

        ad2.addLast(3.1);

        ad3.addFirst(true);
        ad3.addLast(false);

        String s = ad1.removeFirst();
        double d = ad2.removeLast();
        boolean b = ad3.removeFirst();

        assertEquals("Remove first test from ad1", "c", s);
        assertEquals("remove last from ad2", 3.1, d, 0.0);
        assertTrue("Remove first from ad3", b);
    }

    @Test
    public void EmptyNullReturnTest() {
        ArrayDeque<Integer> ad1 = new ArrayDeque<>();
        boolean passed1 = false;
        boolean passed2 = false;
        assertNull("Return null when removeFirst is called", ad1.removeFirst());
        assertNull("Return null when removeLast is called", ad1.removeLast());
    }

    @Test
    public void midSizeArrayDequeTest() {
        ArrayDeque<String> ad1 = new ArrayDeque<>();
        ad1.addFirst("a");
        ad1.addFirst("c");
        ad1.addFirst("f");
        ad1.addLast("b");
        ad1.addLast("d");
        ad1.addLast("e");
        assertEquals("First item before resizing should be f", ad1.get(0), "f");
//        ad1.printDeque();
        ad1.addLast("g");
        ad1.addLast("h");
        ad1.addLast("z");
//        ad1.printDeque();
        assertEquals("First item after resizing should be f", ad1.get(0), "f");
    }

    @Test
    public void bigSizeArrayDequeTest() {
        ArrayDeque<Integer> ad1 = new ArrayDeque<>();
        for (int i = 0; i < 1000000; i++) {
                ad1.addLast(i);
        }
        for (double i = 0; i < 500000; i++) {
            assertEquals("Should have the same value", i, (double) ad1.removeFirst(), 0.0);
        }

        for (double i = 999999; i > 500000; i--) {
            assertEquals("Should have the same value", i, (double) ad1.removeLast(), 0.0);
        }
    }

}