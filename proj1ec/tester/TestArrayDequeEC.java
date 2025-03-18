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

    public Integer addFirstRemoveStudent(int num, StudentArrayDeque<Integer> deque) {

        deque.addFirst(num);
        deque.addFirst(num*2);
        return deque.removeFirst();
    }

    public Integer addFirstRemoveSolution(int num, ArrayDequeSolution<Integer> deque) {

        deque.addFirst(num);
        deque.addFirst(num*2);
        return deque.removeFirst();
    }

    public Integer addLastRemoveStudent(int num, StudentArrayDeque<Integer> deque) {

        deque.addLast(num);
        deque.addLast(num+3);
        deque.addFirst(num+4);
        return deque.removeLast();
    }
    public Integer addLastRemoveSolution(int num, ArrayDequeSolution<Integer> deque) {

        deque.addLast(num);
        deque.addLast(num+3);
        deque.addFirst(num+4);
        return deque.removeLast();
    }

    @Test
    public void addRemoveTest() {
        StudentArrayDeque<Integer> sad1 = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> ads1 = new ArrayDequeSolution<>();
        StringBuilder methodCalls = new StringBuilder();

        for (int i = 0; i < 1000; i++) {
            double uniRandomNum = StdRandom.uniform();
            if (uniRandomNum < 0.5) {
                sad1.addFirst(i);
                sad1.addFirst(i*2);
                methodCalls.append("addFirst(").append(i).append(")").append("\n");
                ads1.addFirst(i);
                ads1.addFirst(i*2);
                methodCalls.append("addFirst(").append(i*2).append(")").append("\n");
                Integer sad1First = sad1.removeFirst();
                Integer ads1First = ads1.removeFirst();
                methodCalls.append("RemoveFirst()").append("\n");
                assertEquals(String.valueOf(methodCalls), ads1First, sad1First);
            } else {
                sad1.addLast(i);
                sad1.addLast(i+1);
                methodCalls.append("addLast(").append(i).append(")").append("\n");
                ads1.addLast(i);
                ads1.addLast(i+1);
                methodCalls.append("addLast(").append(i+1).append(")").append("\n");
                Integer sad1Last = sad1.removeLast();
                Integer ads1Last = ads1.removeLast();
                methodCalls.append("RemoveFirst()").append("\n");
                assertEquals(String.valueOf(methodCalls), sad1Last, ads1Last);
            }
        }

    }
}