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
        return deque.removeLast();
    }
    public Integer addLastRemoveSolution(int num, ArrayDequeSolution<Integer> deque) {

        deque.addLast(num);
        deque.addLast(num+3);
        return deque.removeLast();
    }

    @Test
    public void addRemoveTest() {
        StudentArrayDeque<Integer> sad1 = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> ads1 = new ArrayDequeSolution<>();

        for (int i = 0; i < 100; i++) {
            double uniRandomNum = StdRandom.uniform();
            if (uniRandomNum < 0.5) {
                assertEquals(addFirstRemoveStudent(i, sad1), addFirstRemoveSolution(i, ads1));
            } else {
                assertEquals(addLastRemoveStudent(i, sad1), addLastRemoveSolution(i, ads1));
            }
        }

    }
}