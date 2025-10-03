package byow.lab12;
import org.junit.Test;
import static org.junit.Assert.*;

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
    public TETile[][] world;

    public HexWorld() {
        // Build a word and prefill with NOTHING to avoid nulls
        this.world = new TETile[WIDTH][HEIGHT];
        fillWithNullTiles();
    }

    private static final long SEED = 1234;
    private static final Random RANDOM = new Random(SEED);

    private void fillWithNullTiles() {
        for (int x = 0; x < WIDTH; x++) {
            Arrays.fill(world[x], Tileset.NOTHING);
        }
    }

    public TETile[][] getHexWorld() {
        return world;
    }

    /**
     * Create a Hexagon class to create a Hexagon object given
     * the tile type and size of the hexagon.
     */
    public static class Hexagon {
        private final TETile tile;
        private final int size;
        private final int height;
        private final int width;

        public Hexagon(TETile tile, int sideLength) {
            this.tile = tile;
            this.size = sideLength;
            this.height = size * 2; // number of rectangular rows
            this.width = size + (size-1) * 2; // number of rec cols
        }

        /**
         * Adds a hexagon of side length s to a given position in the world.
         * @return a world of hexagon tiles.
         */
        public TETile[][] addHexagon(TETile[][] world, int x0, int y0, TETile bgColor) {
            for (int r = size - 1; r >= 0; r--) {
                int start = size - r - 1;
                int end = width - (size - r);
                fillRow(world, x0, y0, start, end, r, bgColor);
            }
            return world;
        }

        private void fillRow(TETile[][] world, int x0, int y0, int s, int e, int r, TETile bgColor) {
            int mirror = height - (r + 1);
            for (int j = 0; j < width; j++) {
                TETile t = (j >= s && j <= e) ? tile : bgColor;
                world[x0 + j][y0 + r] = t;
                world[x0 + j][y0 + mirror] = t;
            }
        }

        public TETile getHexagonTile() {
            return tile;
        }

        public int getHexagonSize() {
            return size;
        }

    }

    public static void main(String[] args) {
        TETile[][] world = new HexWorld().getHexWorld();
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        Hexagon hex = new Hexagon(Tileset.FLOWER, 2);
        ter.renderFrame(hex.addHexagon(world, 10, 0, Tileset.NOTHING));
    }


}
