package tester;

import static org.junit.Assert.*;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import student.StudentArrayDeque;

import java.util.Deque;
import java.util.Optional;

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
        StringBuilder methodCalls = new StringBuilder();
        Integer sad1Remove;
        Integer ads1Remove;

        for (int i = 0; i < 100; i++) {
            double uniRandomNum = StdRandom.uniform();
            if (uniRandomNum < 0.5) {
                sad1.addFirst(i-1);
                sad1.addFirst(i*2);
                ads1.addFirst(i-1);
                ads1.addFirst(i*2);
                methodCalls.append("addFirst(").append(i-1).append(")").append("\n");
                methodCalls.append("addFirst(").append(i*2).append(")").append("\n");
                sad1Remove = sad1.removeFirst();
                ads1Remove = ads1.removeFirst();
                methodCalls.append("removeFirst()").append("\n");
                assertEquals(String.valueOf(methodCalls), ads1Remove, sad1Remove);
            } else {
                sad1.addLast(i-10);
                sad1.addLast(i+1);
                ads1.addLast(i-10);
                ads1.addLast(i+1);
                methodCalls.append("addLast(").append(i-10).append(")").append("\n");
                methodCalls.append("addLast(").append(i+1).append(")").append("\n");
                ads1.addFirst(i+4);
                sad1.addFirst(i+4);
                methodCalls.append("addFirst(").append(i+4).append(")").append("\n");
                sad1Remove = sad1.removeLast();
                ads1Remove = ads1.removeLast();
                methodCalls.append("removeLast()").append("\n");
                assertEquals(String.valueOf(methodCalls), ads1Remove, sad1Remove);
            }
        }

    }

    @Test
    public void testFailureSequence() {
        ArrayDequeSolution<Integer> ads = new ArrayDequeSolution<>();
//        ads.addFirst(0);
//        ads.RemoveFirst();
//        ads.addFirst(0);
//        ads.addFirst(2);
//        ads.RemoveFirst();
//        ads.addLast(-8);
//        ads.addLast(3);
//        ads.addFirst(6);
//        ads.RemoveLast();
//        ads.addFirst(2);
//        ads.addFirst(6);
//        ads.RemoveFirst();
//        ads.addFirst(3);
//        ads.addFirst(8);
//        ads.RemoveFirst();;
//        ads.addLast(-5);
//        ads.addLast(6);
//        ads.addFirst(9);
//        ads.RemoveLast();
//        ads.addFirst(5);
//        ads.addFirst(12);
//        ads.RemoveFirst();
//        ads.addFirst(6);
//        ads.addFirst(14);
//        ads.RemoveFirst();
//        ads.addLast(-2);
//        ads.addLast(9);
//        ads.addFirst(12);
//        ads.RemoveLast();
    }
}