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

        for (int i = 0; i < 10; i++) {
            double uniRandomNum = StdRandom.uniform();
            if (uniRandomNum < 0.5) {
                sad1.addFirst(i-1);
                sad1.addFirst(i*2);
                ads1.addFirst(i-1);
                ads1.addFirst(i*2);
                methodCalls.append("addFirst(").append(i-1).append(")").append("\n");
                methodCalls.append("addFirst(").append(i*2).append(")").append("\n");
                sad1.addLast(i+3);
                ads1.addLast(i+3);
                methodCalls.append("addLast(").append(i+3).append(")").append("\n");
                Integer sad1First = sad1.removeFirst();
                Integer ads1First = ads1.removeFirst();
                methodCalls.append("RemoveFirst()").append("\n");
                assertEquals(String.valueOf(methodCalls), ads1First, sad1First);
                Integer sad1Last = sad1.removeLast();
                Integer ads1Last = ads1.removeLast();
                assertEquals(String.valueOf(methodCalls), ads1Last, sad1Last);
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
                Integer sad1Last = sad1.removeLast();
                Integer ads1Last = ads1.removeLast();
                methodCalls.append("RemoveLast()").append("\n");
                assertEquals(String.valueOf(methodCalls), ads1Last, sad1Last);
            }
        }

    }
}