package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 * Ns (size of the data structure), time(s) gives the time required to complete
 * all operations. #ops (gives the number of calls to addLast made during experiment).
 * microsec/op gives the number of microseconds it took on average to complete each call
 * to addLast.
 */
public class TimeAList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeAListConstruction();
    }

    public static void timeAListConstruction() {
        AList<Integer> Ns = new AList<>();
        AList<Double> times = new AList<>();
        AList<Integer> opCounts = new AList<>();
        for (int n = 1000; n <= 128000; n = n * 2) {
            int opCount = 0;
            Ns.addLast(n);
            AList<String> tempList = new AList<>();
            Stopwatch sw = new Stopwatch();
            for (int i = 0; i < n; i++) {
                tempList.addLast("test message inserted");
                opCount = opCount + 1;
            }
            double timeInSeconds = sw.elapsedTime();
            opCounts.addLast(opCount);
            times.addLast(timeInSeconds);
        }
        printTimingTable(Ns, times, opCounts);
    }
}
