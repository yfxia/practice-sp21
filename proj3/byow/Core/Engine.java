package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import static byow.Core.KeyboardUtils.*;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 50;
    public static final int HEIGHT = 60;

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        KeyboardUtils keyboard = new KeyboardUtils(WIDTH, HEIGHT);
        keyboard.startGame();
        String userInput = keyboard.solicitUserInput(19);
        interactWithInputString(userInput);
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

        long seed = 1;
        String moves = "";
        int n = input.length();
        if (Character.toLowerCase(input.charAt(0)) == 'n') {
            char endSeed = 's';
            int index = input.indexOf(endSeed);
            seed = Long.parseLong(input.substring(1, index));
            if (index + 1 < n) {
                moves = input.substring(index + 1);
            }

        }

        Room room = new Room(seed, WIDTH, HEIGHT);
        return room.generateWord(moves);
    }
}
