package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.algs4.Edge;

import java.util.*;

import static byow.Core.RandomUtils.*;

/**
 * The Room object is used to represent a single room on the board,
 * and can be drawn to the screen using TERenderer class.
 *
 */
public class Room {

    public record IntPair(int x, int y) {}

    /** MINIMUM SIZE ROOM REQUIREMENT */
    public static final int MIN_WIDTH = 3;
    public static final int MIN_HEIGHT = 3;

    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;

    private static final long SEED = 1234;
    private static final Random RANDOM = new Random(SEED);

    private static final List<Room> rooms = new ArrayList<>();

    private TETile[][] world;

    private IntPair loc;
    private IntPair size;

    /** Add this room to the list if not overlapping with others. */
    private boolean overlap = false;

    private IntPair center;

    public Room() {
        initializeWorld();
    }

    public Room(IntPair location, IntPair size) {
        this.loc = location;
        this.size = size;
    }

    public boolean intersects(Room b) {
        return !((loc.x >= b.loc.x + b.size.x) ||
                (loc.x + size.x <= b.loc.x) ||
                (loc.y >= b.loc.y + b.size.y) ||
                (loc.y + size.y <= b.loc.y));
    }

    public void drawRandomRooms(int numberOfRooms) {
        while (numberOfRooms > 0) {
            if (addARoom(world)) {
                numberOfRooms--;
            }
        }
    }

    private Boolean addARoom(TETile[][] world) {
        /* Randomly select origin of the room to be (x,y) */
        int x = uniform(RANDOM, WIDTH - MIN_WIDTH - 1);
        int y = uniform(RANDOM, HEIGHT - MIN_HEIGHT - 1);
        double randomX = gaussian(RANDOM, WIDTH / 10.0, MIN_WIDTH);
        double randomY = gaussian(RANDOM, HEIGHT / 10.0, MIN_HEIGHT);
        int sizeX = (int) Math.max(MIN_WIDTH, Math.min(randomX, WIDTH - x - 1));
        int sizeY = (int) Math.max(MIN_WIDTH, Math.min(randomY, WIDTH - y - 1));
        Room room = new Room(new IntPair(x, y), new IntPair(sizeX, sizeY));
        for (Room other : rooms) {
            if (room.intersects(other)) {
                room.overlap = true;
            }
        }
        if (!room.overlap) {
            rooms.add(room);
            room.center = new IntPair(x + sizeX / 2, y + sizeY / 2);
            buildRoom(world, x, y, sizeX, sizeY);
        }
        return !room.overlap;
    }

    /** Utility function to fill in shape horizontally x-axis then vertically along y-axis. */
    private void buildRoom(TETile[][] world, int x0, int y0, int sizeX, int sizeY) {
        for (int i = 0; i < sizeX; i++) {
            world[x0 + i][y0] = Tileset.WALL;
            world[x0 + i][y0 + sizeY - 1] = Tileset.WALL;
        }
        for (int j = 0; j < sizeY; j++) {
            world[x0][y0 + j] = Tileset.WALL;
            world[x0 + sizeX - 1][y0 + j] = Tileset.WALL;
        }
    }

    /** Build a word and prefill with NOTHING to avoid nulls */
    private void initializeWorld() {
        this.world = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x++) {
            Arrays.fill(world[x], Tileset.NOTHING);
        }
    }

    /** Compute the distance between two rooms using Euclidean Distance. */
    private Double getEuclideanDistance(Room a, Room b) {
        double dx = a.center.x - b.center.x;
        double dy = a.center.y - b.center.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /** Find the Minimum Spanning Tree of the un-directed, incomplete graph. */
    private List<Edge> computeMST() {
        int n = rooms.size();
        double[] distTo = new double[n];
        int[] parent = new int[n];
        boolean[] inMST = new boolean[n];
        Arrays.fill(distTo, Double.POSITIVE_INFINITY);
        Arrays.fill(parent, -1);
        int start = 0;
        distTo[start] = 0.0;
        for (int iter = 0; iter < n; iter++) {
            int v = -1;
            double best = Double.POSITIVE_INFINITY;
            // Pick the next vertex in Prim's Algorithm
            for (int i = 0; i < n; i++) {
                if (!inMST[i] && distTo[i] < best) {
                    v = i;
                    best = distTo[i];
                } // find argmin over all vertices not yet in the tree
            }
            if (v == -1) break; // disconnected
            inMST[v] = true; // add v to tree
            // relax edges (v,w) to every vertex w not yet in MST
            for (int w = 0; w < n; w++) {
                if (inMST[w] || w == v) continue;
                double d = getEuclideanDistance(rooms.get(v), rooms.get(w));
                if (d < distTo[w]) {
                    distTo[w] = d;
                    parent[w] = v;
                }
            }
        }
        return buildMST(distTo, parent);
    }

    /** Build the edges for the Minimum Spanning Tree to connect all the rooms. */
    private List<Edge> buildMST(double[] distTo, int[] parent) {
        List<Edge> mst = new ArrayList<>();
        for (int v = 0; v < rooms.size(); v++) {
            int u = parent[v];
            if (u != -1) {
                mst.add(new Edge(u, v, distTo[v]));
            }
        }
        return mst;
    }

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);
        Room room = new Room();
        TETile[][] world = room.world;
        room.drawRandomRooms(3);
        List<Edge> edges = room.computeMST();
        ter.renderFrame(world);
    }
}
