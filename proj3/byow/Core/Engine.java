package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.algs4.Edge;

import java.util.List;

import static byow.Core.Room.isDoor;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {

        System.out.print(input);
        int seed = 1;
        int n = input.length();
        if (Character.toLowerCase(input.charAt(0)) == 'n' &&
                Character.toLowerCase(input.charAt(n-1)) == 's') {
            seed = Integer.parseInt(input.substring(1, n-1));
        }

        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);
        Room room = new Room(seed, WIDTH, HEIGHT);
        TETile[][] world = room.world;
        room.drawRandomRooms(8);
        List<Edge> edges = room.computeMST();
        for (Edge edge : edges) {
            int a = edge.either();
            int b = edge.other(a);
            Room.DoorPair doors =  room.placeDoors(world, Room.rooms.get(a), Room.rooms.get(b));
            room.buildPassableMask(doors.aDoor(), doors.bDoor());
            List<Room.IntPair> path = AStar2D.findPath(room.passableMask, room.isInterior,
                    new Room.IntPair(doors.aDoor().x(), doors.aDoor().y()),
                    new Room.IntPair(doors.bDoor().x(), doors.bDoor().y()),
                    0, 0);
            for (Room.IntPair p : path) {
                if (!isDoor(world[p.x()][p.y()])) {
                    world[p.x()][p.y()] = Tileset.FLOOR;
                    room.passableMask[p.x()][p.y()] = false;
                }
                room.buildWall(p.x(), p.y());
            }

        }
        ter.renderFrame(world);
        return world;
    }
}
