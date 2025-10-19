package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.algs4.Edge;

import java.util.*;

import static byow.Core.RandomUtils.*;
import static byow.Core.AStar2D.*;

/**
 * The Room object is used to represent a single room on the board,
 * and can be drawn to the screen using TERenderer class.
 *
 */
public class Room {

    public static final class IntPair {
        private final int x, y;
        public IntPair(int x, int y) { this.x = x; this.y = y; }
        public int x() { return x; }
        public int y() { return y; }
        @Override public String toString() { return "IntPair[x="+x+", y="+y+"]"; }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof IntPair)) return false;
            IntPair p = (IntPair) o;
            return x == p.x && y == p.y;
        }
        @Override public int hashCode() { return 31 * x + y; }
    }

    public static final class DoorPair {
        private final IntPair aDoor, bDoor;
        public DoorPair(IntPair aDoor, IntPair bDoor) { this.aDoor = aDoor; this.bDoor = bDoor; }
        public IntPair aDoor() { return aDoor; }
        public IntPair bDoor() { return bDoor; }
        @Override public String toString() { return "DoorPair[a="+aDoor+", b="+bDoor+"]"; }
    }

    /** MINIMUM SIZE ROOM REQUIREMENT */
    public static final int MIN_WIDTH = 3;
    public static final int MIN_HEIGHT = 3;

    private int WIDTH;
    private int HEIGHT;

    private long SEED;
    private Random RANDOM;

    static final List<Room> rooms = new ArrayList<>();

    TETile[][] world;
    Boolean[][] isInterior;
    private Boolean[][] isWall;
    Boolean[][] passableMask;

    private IntPair loc;
    private IntPair size;

    /** Add this room to the list if not overlapping with others. */
    private boolean overlap = false;

    /** Center of the Room in (x,y) coordinates pair. */
    private IntPair center;

    public Room(int seed, int width, int height) {
        SEED = seed;
        WIDTH = width;
        HEIGHT = height;
        initializeWorld();
    }

    public Room(IntPair location, IntPair size) {
        this.loc = location;
        this.size = size;
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
        int sizeY = (int) Math.max(MIN_WIDTH, Math.min(randomY, HEIGHT - y - 1));
        Room room = new Room(new IntPair(x, y), new IntPair(sizeX, sizeY));
        for (Room other : rooms) {
            if (overlapWithBuffer(room, other, 1)) {
                room.overlap = true;
                break;
            }
        }
        if (!room.overlap) {
            rooms.add(room);
            room.center = new IntPair(x + sizeX / 2, y + sizeY / 2);
            buildRoom(world, x, y, sizeX, sizeY);
            fillInterior(world, x, y, sizeX, sizeY);
        }
        return !room.overlap;
    }

    /** Utility function to fill in shape horizontally x-axis then vertically along y-axis. */
    private void buildRoom(TETile[][] world, int x0, int y0, int sizeX, int sizeY) {
        for (int i = 0; i < sizeX; i++) {
            world[x0 + i][y0] = Tileset.WALL;
            world[x0 + i][y0 + sizeY - 1] = Tileset.WALL;
            isWall[x0 + i][y0] = true;
            isWall[x0 + i][y0 + sizeY - 1] = true;
        }
        for (int j = 0; j < sizeY; j++) {
            world[x0][y0 + j] = Tileset.WALL;
            world[x0 + sizeX - 1][y0 + j] = Tileset.WALL;
            isWall[x0][y0 + j] = true;
            isWall[x0 + sizeX - 1][y0 + j] = true;
        }
    }

    private void fillInterior(TETile[][] world, int x0, int y0, int sizeX, int sizeY) {
        int x1 = x0 + 1; // first tile - interior x
        int y1 = y0 + 1; // first tile - interior y
        int x2 = x0 + sizeX - 2; // last tile - interior x
        int y2 = y0 + sizeY - 2; // last tile -interior y
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                world[x][y] = Tileset.FLOOR;
                isInterior[x][y] = true;
            }
        }
    }

    /** Build a word and prefill with NOTHING to avoid nulls */
    private void initializeWorld() {
        RANDOM = new Random(SEED);
        this.world = new TETile[WIDTH][HEIGHT];
        isWall = new Boolean[WIDTH][HEIGHT];
        isInterior = new Boolean[WIDTH][HEIGHT];
        passableMask = new Boolean[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x++) {
            Arrays.fill(world[x], Tileset.NOTHING);
            Arrays.fill(isWall[x], false);
            Arrays.fill(isInterior[x], false);
            Arrays.fill(passableMask[x], false);
        }

    }

    /** Compute the distance between two rooms using Euclidean Distance. */
    private Double getEuclideanDistance(Room a, Room b) {
        double dx = a.center.x - b.center.x;
        double dy = a.center.y - b.center.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /** Find the Minimum Spanning Tree of the un-directed, incomplete graph. */
    List<Edge> computeMST() {
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

    /** Pick a door on each room in a connected edge.  */
    DoorPair placeDoors(TETile[][] world, Room a, Room b) {
        // Decide horizontal (L/R) vs. vertical (T/B) orientation of the corridor.
        char ori = (Math.abs(a.center.x - b.center.x) >=
                    Math.abs(a.center.y - b.center.y)) ? 'H' : 'V';
        // If doorA opens on the right
        IntPair doorA = pickDoorFacing(a, b, ori);
        world[doorA.x][doorA.y] = Tileset.LOCKED_DOOR;
        // doorB opens on the left
        IntPair doorB = pickDoorFacing(b, a, ori);
        world[doorB.x][doorB.y] = Tileset.LOCKED_DOOR;
        return new DoorPair(doorA, doorB);
    }

    private IntPair pickDoorFacing(Room a, Room b, char ori) {
        int xLeftExt = a.loc.x;
        int yBottomExt = a.loc.y;
        int xRightExt = xLeftExt + a.size.x - 1;
        int yTopExt = yBottomExt + a.size.y - 1;
        int xLeftInt = xLeftExt + 1;
        int yBottomInt = yBottomExt + 1;
        int xRightInt = xRightExt - 1;
        int yTopInt = yTopExt - 1;
        if (ori == 'H') {
            if (b.center.x >= a.center.x) {
                return new IntPair(xRightExt, clamp(b.center.y, yBottomInt, yTopInt));
            } else {
                return new IntPair(xLeftExt, clamp(b.center.y, yBottomInt, yTopInt));
            }
        } else {
            if (b.center.y >= a.center.y) {
                return new IntPair(clamp(b.center.x, xLeftInt, xRightInt), yTopExt); // top
            } else {
                return new IntPair(clamp(b.center.x, xLeftInt, xRightInt), yBottomExt); // bottom
            }
        }
    }

    void buildPassableMask(IntPair doorA, IntPair doorB) {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                boolean pass = (world[x][y] == Tileset.NOTHING);
                if (isInterior[x][y] || isWall[x][y]) pass = false;  // force block
                if ((x == doorA.x() && y == doorA.y()) ||
                        (x == doorB.x() && y == doorB.y())) pass = true; // force allow
                passableMask[x][y] = pass;
            }
        }
    }

    private static int clamp(int value, int lo, int hi) {
        return Math.max(lo, Math.min(value, hi));
    }

    static boolean isDoor(TETile t) {
        return t == Tileset.LOCKED_DOOR /* or whatever door tiles you use */;
    }

    // Treat rectangles inclusive on the boundary; buf=1 gives one NOTHING ring
    private static boolean overlapWithBuffer(Room a, Room b, int buf) {
        int aL = a.loc.x - buf, aR = a.loc.x + a.size.x - 1 + buf;
        int bL = b.loc.x - buf, bR = b.loc.x + b.size.x - 1 + buf;
        int aB = a.loc.y - buf, aT = a.loc.y + a.size.y - 1 + buf;
        int bB = b.loc.y - buf, bT = b.loc.y + b.size.y - 1 + buf;
        return !(aR < bL || bR < aL || aT < bB || bT < aB);
    }

    void buildWall(int x, int y) {
        for (Dir dir : ORDER) {
            int nx = x + dir.dx;
            int ny = y + dir.dy;
            if (inBounds(nx, ny, WIDTH, HEIGHT) && !isDoor(world[nx][ny])
                    && passableMask[nx][ny]) {
                world[nx][ny] = Tileset.WALL;
            }
        }
    }

//    public static void main(String[] args) {
//        TERenderer ter = new TERenderer();
//        ter.initialize(WIDTH, HEIGHT);
//        Room room = new Room();
//        TETile[][] world = room.world;
//        room.drawRandomRooms(8);
//        List<Edge> edges = room.computeMST();
//        for (Edge edge : edges) {
//            int a = edge.either();
//            int b = edge.other(a);
//            DoorPair doors =  room.placeDoors(world, Room.rooms.get(a), Room.rooms.get(b));
//            room.buildPassableMask(doors.aDoor, doors.bDoor);
//            List<IntPair> path = AStar2D.findPath(room.passableMask, room.isInterior,
//                    new IntPair(doors.aDoor.x, doors.aDoor.y),
//                    new IntPair(doors.bDoor.x, doors.bDoor.y),
//                    0, 0);
//            for (IntPair p : path) {
//                if (!isDoor(world[p.x][p.y])) {
//                    world[p.x][p.y] = Tileset.FLOOR;
//                    room.passableMask[p.x][p.y] = false;
//                }
//                room.buildWall(p.x, p.y);
//            }
//
//        }
//        ter.renderFrame(world);
//    }
}
