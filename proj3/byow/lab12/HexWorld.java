package byow.lab12;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Arrays;
import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {

    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;
    private TETile[][] world;
    private int sideLen;
    private TETile tile;
    private int height;
    private int width;
    private static final long SEED = 1234;
    private static final Random RANDOM = new Random(SEED);

    public HexWorld(int size) {
        this.world = new TETile[WIDTH][HEIGHT];
        this.sideLen = size;
        fillWithNullTiles();
        this.height = size * 2; // height of the hexagon
        this.width = size + (size - 1) * 2; // width of the nesagon
    }

    /** Build a word and prefill with NOTHING to avoid nulls */
    private void fillWithNullTiles() {
        for (int x = 0; x < WIDTH; x++) {
            Arrays.fill(world[x], Tileset.NOTHING);
        }
    }

    /** Drawing a tesselation of hexagon based off of (x0, y0) */
    public void drawHexWorld(TETile[][] world, int x0, int y0) {
        for (int j = 0; j < 3; j ++) {
            fillCol(world, j, x0, y0);
            switch(j) {
                case 0:
                    fillCol(world, 0, (sideLen * 2 - 1) * 4, 0);
                    break;
                case 1:
                    fillCol(world, 1, (sideLen * 2 - 1) * 2, 0);
                    break;
            }
        }
    }

    /** Utility function to fill in each single hexagon by column in the world */
    private void fillCol(TETile[][] world, int col, int x0, int y0) {
        int start = sideLen * (2 - col);  // top offset for the column
        int count = 3 + col; // 3,4,5 for col 0,1,2
        int x = x0 + (sideLen * 2 - 1) * col; // col-to-col x step
        for (int k = 0; k < count; k ++) {
            int y = y0 + start + k * height;
            addHexagon(world, x, y, getRandomTile());
        }
    }


    /**
     * Adds a hexagon of side length s to a given position in the world.
     */
    public void addHexagon(TETile[][] world, int x0, int y0, TETile bgColor) {
        for (int r = sideLen - 1; r >= 0; r--) {
            int start = sideLen - r - 1;
            int end = width - (sideLen - r);
            fillRow(world, x0, y0, start, end, r, bgColor);
        }
    }

    /**
     * Utility function to fill in world by rows, from position [s,e]
     * inclusive with offset values (x0, y0) starting from bottom left corner.
     */
    private void fillRow(TETile[][] world, int x0, int y0, int s, int e, int r,
                                TETile tile) {
        int mirror = height - (r + 1);
        for (int j = 0; j < width; j++) {
            if (j >= s && j <= e) {
                world[x0 + j][y0 + r] = tile;
                world[x0 + j][y0 + mirror] = tile;
            }

        }
    }

    private TETile getRandomTile() {
        int tileNum = RANDOM.nextInt(10);
        return switch (tileNum) {
            case 0 -> Tileset.GRASS;
            case 1 -> Tileset.TREE;
            case 2 -> Tileset.MOUNTAIN;
            case 3 -> Tileset.WATER;
            case 4 -> Tileset.UNLOCKED_DOOR;
            case 5 -> Tileset.LOCKED_DOOR;
            case 6 -> Tileset.FLOOR;
            case 7 -> Tileset.AVATAR;
            case 8 -> Tileset.SAND;
            case 9 -> Tileset.WALL;
            default -> Tileset.FLOWER;
        };
    }

    public static void main(String[] args) {
        HexWorld hex = new HexWorld(4);
        TETile[][] world = hex.world;
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        hex.drawHexWorld(world, 0, 0);
        ter.renderFrame(world);
    }


}
