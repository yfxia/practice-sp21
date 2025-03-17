package tester;

import static org.junit.Assert.*;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import student.StudentArrayDeque;

public class TestArrayDequeEC {
    /** Randomly call StudentArrayDeque and ArrayDequeSolution methods
     * until they disagree on an output.
     */
    @Test
    public void addIsEmptySizeTest() {
        StudentArrayDeque<Integer> sad1 = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> ads1 = new ArrayDequeSolution<>();
        assertTrue("A newly initialized ArrayDeque should be Empty", sad1.isEmpty());

        sad1.addFirst(2);
        ads1.addLast(2);
        assertEquals("Size of ArrayDeque should be 1", sad1.size(), ads1.size());
    }

    @Test
    public void addRemoveTest() {
        StudentArrayDeque<Integer> sad1 = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> ads1 = new ArrayDequeSolution<>();
        for (int i = 0; i < 1000; i++) {
            double uniRandomNum = StdRandom.uniform();
            if (uniRandomNum < 0.5) {
                sad1.addFirst(i);
                ads1.addFirst(i);
            } else {
                sad1.addLast(i);
                ads1.addLast(i);
            }
        }
        for (int i = 0; i < 1000; i++) {
            double uniRandomNum = StdRandom.uniform();
            if (uniRandomNum < 0.5) {
                Integer sad1First = sad1.removeFirst();
                Integer ads1First = ads1.removeFirst();
                assertEquals("Removing the first item of two array implementations should give the same value",sad1First, ads1First);
            } else {
                Integer sad1Last = sad1.removeLast();
                Integer ads1Last = ads1.removeLast();
                assertEquals("Removing the last item of two array implementations should give the same value", sad1Last, ads1Last);
            }
        }

    }
}