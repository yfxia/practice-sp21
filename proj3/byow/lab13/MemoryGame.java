package byow.lab13;

import byow.Core.RandomUtils;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.util.Random;

public class MemoryGame {
    /** The width of the window of this game. */
    private int width;
    /** The height of the window of this game. */
    private int height;
    /** The current round the user is on. */
    private int round;
    /** The Random object used to randomly generate Strings. */
    private Random rand;
    /** Whether or not the game is over. */
    private boolean gameOver;
    /** Whether or not it is the player's turn. Used in the last section of the
     * spec, 'Helpful UI'. */
    private boolean playerTurn;
    /** The characters we generate random Strings from. */
    private static final char[] CHARACTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    /** Encouraging phrases. Used in the last section of the spec, 'Helpful UI'. */
    private static final String[] ENCOURAGEMENT = {"You can do this!", "I believe in you!",
                                                   "You got this!", "You're a star!", "Go Bears!",
                                                   "Too easy for you!", "Wow, so impressive!"};

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please enter a seed");
            return;
        }

        long seed = Long.parseLong(args[0]);
        MemoryGame game = new MemoryGame(40, 40, seed);
        game.startGame();
    }

    public MemoryGame(int width, int height, long seed) {
        /* Sets up StdDraw so that it has a width by height grid of 16 by 16 squares as its canvas
         * Also sets up the scale so the top left is (0,0) and the bottom right is (width, height)
         */
        this.width = width;
        this.height = height;
        StdDraw.setCanvasSize(this.width * 16, this.height * 16);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, this.width);
        StdDraw.setYscale(0, this.height);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();

        //Initialize random number generator
        rand = new Random(seed);
    }

    /**
     * Randomly generate a string of a specified length.
     * @param n: input length of the string.
     * @return: created string in lower case letter.
     */
    public String generateRandomString(int n) {
        //Generate random string of letters of length n
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < n; i++) {
            char ch = CHARACTERS[RandomUtils.uniform(rand, CHARACTERS.length)];
            s.append(ch);
        }
        return s.toString();
    }

    /**
     * Display the given string on the screen one letter at a time.
     * If game is not over, display relevant game information at the top of the screen.
     * @param s: input given string.
     */
    public void drawFrame(String s) {
        //Take the string and display it in the center of the screen
        StdDraw.clear(Color.BLACK);
        Font font = new Font("Monaco", Font.BOLD, 30);
        Font banner = new Font("Monaco", Font.PLAIN, 20);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.rectangle((double) width / 2, height - 1, this.width, 1);
        StdDraw.setFont(banner);
        StdDraw.text(4, height - 1, "Round: " + round);
        StdDraw.text((double) width / 2, height - 1, "Watch!");
        StdDraw.text(34, height - 1, ENCOURAGEMENT[RandomUtils.uniform(rand, ENCOURAGEMENT.length)]);
        StdDraw.setFont(font);
        StdDraw.text((double) width / 2, (double) height / 2, s); // draws the input string centered on the canvas
        StdDraw.show();
    }

    /**
     * Takes in the input string and displays one character at a time centered
     * on the screen. Each character is visible for 1 second with 0.5-second break.
     * @param letters: given input string.
     */
    public void flashSequence(String letters) {
        // Display each character in letters, making sure to blank the screen between letters
        for (int i = 0; i < letters.length(); i++) {
            drawFrame(letters.substring(i, i + 1));
            StdDraw.pause(1000); // show letter
            drawFrame(""); // blank
            StdDraw.pause(500); // gap
        }
    }

    /**
     * Interacts with a queue StdDraw uses to store all the keys the user
     * pressed and released.
     * @param n: reads in n keystrokes.
     * @return: the string corresponding to those n keystrokes.
     */
    public String solicitNCharsInput(int n) {
        StringBuilder s = new StringBuilder();
        //Read n letters of player input
        while (s.length() < n) {
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                s.append(key);
                drawFrame(s.toString());
            } else {
                StdDraw.pause(15);
            }
        }
        return s.toString();
    }

    /**
     * Launch our game and begin the loop of gameplay until the player fails
     * to type in the target string.
     */
    public void startGame() {
        //Set any relevant variables before the game starts
        round = 1;
        while (!gameOver) {
            drawFrame("Round " + round);
            String s = generateRandomString(round);
            flashSequence(s);
            playerTurn = true;
            drawFrame("Type!");
            String playerS = solicitNCharsInput(round);
            if (playerS.equals(s)) {
                round++;
                continue;
            } else {
                gameOver = true;
                drawFrame("Game Over! You made it to round: " + round);
            }
        }
    }

}
