package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
    @Test
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> testNoResize = new AListNoResizing<>();
        BuggyAList<Integer> testBuggyAList = new BuggyAList<>();
        /** addLast 4 to both*/
        testNoResize.addLast(4);
        testBuggyAList.addLast(4);
        /** addLast 5 to both*/
        testNoResize.addLast(5);
        testBuggyAList.addLast(5);
        /** addLast 6 to both*/
        testNoResize.addLast(6);
        testBuggyAList.addLast(6);
        /** removeLast from both -- 6*/
        assertEquals(testNoResize.removeLast(), testBuggyAList.removeLast());
        /** removeLast again from both -- 5*/
        assertEquals(testNoResize.removeLast(), testBuggyAList.removeLast());
        /** removeLast again from both -- 4*/
        assertEquals(testNoResize.removeLast(), testBuggyAList.removeLast());
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> B = new BuggyAList<>();
        int N = 500;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 3);
            if (operationNumber == 0) {
                //addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                B.addLast(randVal);
            } else if (operationNumber == 1) {
                // size
                int sizeL = L.size();
                int sizeB = B.size();
                assertEquals(sizeL, sizeB);
            } else if (operationNumber == 2) {
                int sizeL = L.size();
                if (sizeL > 0) {
                    assertEquals(L.removeLast(), B.removeLast());
                }
            }
        }
    }
}
