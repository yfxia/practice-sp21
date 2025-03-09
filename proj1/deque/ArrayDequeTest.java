package deque;


import org.junit.Test;
import static org.junit.Assert.*;


public class ArrayDequeTest {
    @Test
    public void addIsEmptySizeTest() {
        ArrayDeque<String> ad1 = new ArrayDeque<>();
        
        assertTrue("A newly initialized ArrayDeque should be empty", ad1.isEMpty());
        ad1.addLast("a");
        ad1.addLast("b");
        
        assertEquals(2, ad1.size());
        assertFalse("ad1 should contain 2 items", ad1.isEMpty());
        
        ad1.addFirst("c");
        assertEquals(3, ad1.size());

        ad1.printDeque();
        
    }

    @Test
    public void addRemoveTest() {
        ArrayDeque<Integer> ad1 = new ArrayDeque<>();
        ad1.addFirst(10);
        assertFalse("ad1 should contain 1 item", ad1.isEMpty());
        ad1.removeFirst();
        assertTrue("ad1 should be empty after removal", ad1.isEMpty());

        ArrayDeque<Integer> ad2 = new ArrayDeque<>();
        ad1.addLast(10);
        assertFalse("ad1 should contain 1 item", ad1.isEMpty());
        ad1.removeLast();
        assertTrue("ad1 should be empty after removal", ad1.isEMpty());
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
        String errorMsg = "  Bad size returned when removing from empty deque.\n";
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
    public void emptyNullReturnTest() {
        ArrayDeque<Integer> ad1 = new ArrayDeque<>();
        boolean passed1 = false;
        boolean passed2 = false;
        assertNull("Return null when removeFirst is called", ad1.removeFirst());
        assertNull("Return null when removeLast is called", ad1.removeLast());
    }

}