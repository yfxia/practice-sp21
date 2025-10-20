package byow.Core;

import edu.princeton.cs.algs4.StdDraw;

import java.awt.*;
import java.util.Random;

public class KeyboardUtils {

    private int width;
    private int height;
    Font title;
    Font prompt;
    double halfWidth;
    double halfHeight;

    /**
     * Set up StdDraw so that it has a width x height 2D grid for
     * drawing.
     */
    public KeyboardUtils(int width, int height) {
        this.width = width;
        this.height = height;
        StdDraw.setCanvasSize(this.width * 16, this.height * 16);
        this.title = new Font("Monaco", Font.BOLD, 30);
        this.prompt = new Font("Monaco", Font.PLAIN, 20);
        this.halfWidth = (double) this.width / 2;
        this.halfHeight = (double) this.height / 2;
        StdDraw.setFont(title);
        StdDraw.setXscale(0, width);
        StdDraw.setYscale(0, height);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();
    }

    /**
     * Display the given string on the screen one letter at a time.
     * If game is not over, display relevant game information at the top of the screen.
     */
    public void drawFrame(String s, Font font, double w, double h, boolean clear) {
        if (clear) {
            StdDraw.clear(Color.BLACK);
        }
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.setFont(font);
        StdDraw.text(w, h, s);
        StdDraw.show();
    }

    public String solicitUserInput(int n) {
        boolean terminate = false;
        StringBuilder s = new StringBuilder();
        while (!terminate && s.length() < n) {
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                if (Character.toLowerCase(key) == 'n') {
                    drawFrame("Enter a seed value", title, halfWidth, halfHeight, true);
                } else if (Character.toLowerCase(key) == 's') {
                    drawFrame("Seed value: " + s, title, halfWidth, halfHeight, true);
                    terminate = true;
                } else {
                    s.append(key);
                    drawFrame(s.toString(), title, halfWidth, halfHeight, true);
                }
            }
        }
        return s.toString();
    }

    public void startGame() {
        StdDraw.setPenColor(StdDraw.WHITE);
        drawFrame("CS61B: THE GAME", title, halfWidth, 38, false);
        drawFrame("New Game (N)", prompt, halfWidth, halfHeight, false);
        drawFrame("Load Game (L)", prompt, halfWidth, halfHeight - 1.5, false);
        drawFrame("Quit (Q)", prompt, halfWidth, halfHeight - 3, false);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please enter a seed");
            return;
        }
        KeyboardUtils keyboard = new KeyboardUtils(50, 50);
        keyboard.startGame();
    }
}
