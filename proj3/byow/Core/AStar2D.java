package byow.Core;

import java.util.*;
import static byow.Core.Room.IntPair;

public class AStar2D {

    enum Dir {
        // Instances of Dir.
        E(1, 0), W(-1, 0), N(0, 1), S(0, -1);
        final int dx, dy;
        Dir(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }
    }

    static final Dir[] ORDER = {Dir.E, Dir.W, Dir.N, Dir.S};

    static class Node {
        int id;
        double f;
        double g;
        Dir dir;
        Node(int id, double f, double g, Dir dir) {
            this.id = id;
            this.f = f;
            this.g = g;
            this.dir = dir;
        }
    }

    /** Compute Heuristic function: Manhattan distance -- 4-way */
    public static int h(IntPair a, IntPair b) {
        return Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y());
    }

    /** Using A* to get doorA --> doorB path. Empty if None found. */
    public static List<IntPair> findPath(Boolean[][] passable, Boolean[][] isInterior,
                                         IntPair start, IntPair goal,
                                         double turnCost, double nearCost) {
        int W = passable.length;
        int H = passable[0].length;
        int N = W * H;
        if (!inBounds(start, W, H) || !inBounds(goal, W, H)
                || !passable[start.x()][start.y()] || !passable[goal.x()][goal.y()]) {
            return Collections.emptyList();
        }

        double[] g = new double[N]; // cost from start to this tile so far
        Arrays.fill(g, Double.POSITIVE_INFINITY);
        int[] parent = new int[N];
        Arrays.fill(parent, -1);
        Dir[] cameDir  = new Dir[N];
        boolean[] visited = new boolean[N]; // Marks tile if fully processed.
        // f = g + h (priority key)
        PriorityQueue<Node> open = new PriorityQueue<>(
                Comparator.<Node>comparingDouble(n -> n.f).
                        thenComparing((a, b) -> Double.compare(b.g, a.g)).
                        thenComparingInt(n -> n.id)
        );
        int s = toId(start.x(), start.y(), W);
        int t = toId(goal.x(), goal.y(), W);

        g[s] = 0.0;
        open.add(new Node(s, h(start, goal), 0.0, null));

        while (!open.isEmpty()) {
            Node current = open.poll();
            if (visited[current.id]) {
                continue;
            }
            visited[current.id] = true;
            if (current.id == t) {
                break; // found the destination.
            }

            IntPair p = toIntPair(current.id, W);
            for (Dir dir : ORDER) {
                int nx = p.x() + dir.dx;
                int ny = p.y() + dir.dy;
                if (!inBounds(nx, ny, W, H) || !passable[nx][ny]) {
                    continue;
                }
                int vid =  toId(nx, ny, W);
                if (visited[vid]) {
                    continue;
                }

                double step = 1.0; // base cost
                if (current.dir != null && dir != current.dir) {
                    step += turnCost; // turn penalty, favors L/straight
                }
                if (touchesInterior(nx, ny, isInterior)) {
                    step += nearCost; // keep halls centered
                }

                double tentative = g[current.id] + step;
                if (tentative < g[vid]) {
                    g[vid] = tentative;
                    parent[vid] = current.id;
                    cameDir[vid] = dir;
                    open.add(new Node(vid,
                            tentative + h(new IntPair(nx, ny), goal), tentative, dir));
                }
            }
        }
        if (parent[t] == -1 && s != t) {
            return Collections.emptyList();
        }
        ArrayDeque<IntPair> stack = new ArrayDeque<>();
        for (int id = t; id != -1; id = parent[id]) {
            stack.push(toIntPair(id, W));
        }
        return new ArrayList<>(stack);

    }

    private static int toId(int x, int y, int W) {
        return y * W + x;
    }

    private static IntPair toIntPair(int id, int W) {
        return new IntPair(id % W, id / W);
    }

    private static boolean touchesInterior(int x, int y, Boolean[][] isInterior) {
        int W = isInterior.length;
        int H = isInterior[0].length;
        return (x > 0 && isInterior[x - 1][y]
                || x + 1 < W && isInterior[x + 1][y]
                || y > 0 && isInterior[x][y - 1]
                || y + 1 < H && isInterior[x][y + 1]
                );
    }

    public static boolean inBounds(IntPair p, int W, int H) {
        return inBounds(p.x(), p.y(), W, H);
    }

    public static boolean inBounds(int x, int y, int W, int H) {
        return x >= 0 && x < W && y >= 0 && y < H;
    }


}
