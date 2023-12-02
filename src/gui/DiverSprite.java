package gui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.*;

import game.Node;
import game.Sewers;
import game.Sewers.Direction;

/**
 * Responsible for managing the sewer diver and drawing it on the
 * screen. Handles functions to update the sewer diver and update
 * its drawing as well.
 */
public class DiverSprite extends JPanel {

    /**
     * The GUI that created this DiverSprite
     */
    private GUI gui;

    /**
     * Sprite class to handle animating the diver
     */
    private Sprite sprite;

    /**
     * Width and height (in pixels) of a single diver image on the spritesheet
     */
    private final static int SPRITE_WIDTH = 29, SPRITE_HEIGHT = 36;

    /** Length of an animation frame in ms */
    private final static int FRAME_LENGTH_MS = 1000;

    /**
     * SewerDiver's row and column indexes (updated only once move completes)
     */
    private int row, col;

    /**
     * x- and y- coordinates (pixels)
     */
    private int posX, posY;

    /**
     * Direction the diver is currently facing?
     */
    private Sewers.Direction dir = Direction.NORTH;

    /**
     * Location of the spritesheet image
     */
    private String spriteSheet = "res/explorer_sprites.png";

    /**
     * Constructor: an instance starting at (startRow, startCol), created for gui.
     */
    public DiverSprite(int startRow, int startCol, GUI gui) {
        this.gui = gui;
        // Initialize fields
        sprite = new Sprite(spriteSheet, SPRITE_WIDTH, SPRITE_HEIGHT, 3);
        if (sprite == null) {
            throw new Error("Could not load sprite from " + spriteSheet);
        }
        // Initialize the starting location
        row = startRow;
        col = startCol;
        posX = row * MazePanel.TILE_WIDTH;
        posY = col * MazePanel.TILE_HEIGHT;
    }

    /**
     * Return the image representing the current state of the diver.
     */
    public BufferedImage sprite() {
        /* Use the direction to determine which offset into the
         * spritesheet to use. The Sprite class handles animation. */
        if (gui.gameState.scramSucceeded()) {
            System.out.println("In DiverSprite.sprite. painting exitEnd");
            return gui.mazePanel.exitEnd;
        }
        switch (dir) {
            case NORTH:
                return sprite.getSprite(0, 0);
            case SOUTH:
                return sprite.getSprite(0, 3);
            case WEST:
                return sprite.getSprite(1, 0);
            case EAST:
                return sprite.getSprite(1, 3);
            default:
                return sprite.getSprite(0, 0);
        }
    }

    /**
     * Return the diver's row on the grid. Will remain the diver's
     * old position until the diver has completely arrived at the new
     * one.
     */
    public int row() {
        return row;
    }

    /**
     * Return the diver's column on the grid. Will remain the diver's
     * old position until the diver has completely arrived at the
     * new one.
     */
    public int col() {
        return col;
    }

    /**
     * Tell the diver to move from its current location to node dst.
     * After making move, calling thread will block until the move
     * completes on GUI.
     * Requires: dst must be adjacent to the
     * current location and not currently moving.
     */
    public void moveTo(Node dst) {
        dir = getDirection(row, col, dst.getTile().row(), dst.getTile().column());
        // Determine sequence of moves to add to queue to get to goal
        int dx = (dst.getTile().column() - col) * MazePanel.TILE_WIDTH;
        int dy = (dst.getTile().row() - row) * MazePanel.TILE_HEIGHT;
        animateMove(dx, dy, () -> {
            row = dst.getTile().row();
            col = dst.getTile().column();
        });
    }

    /** Start animation of the diver to relative position (dx, dy), and
     *  perform the specified action when the animation is complete.
     */
    void animateMove(int dx, int dy, Runnable action) {
        int frames = GUI.FRAMES_PER_MOVE;
        long t0 = System.currentTimeMillis(); // Get the next move to make
        Timer timer = new Timer(1000 / GUI.FRAMES_PER_SECOND, event -> {
            sprite.tick();
            long dt = System.currentTimeMillis() - t0; // Get the next move to make
            int frame = (int) (dt * GUI.FRAMES_PER_SECOND / 1000);
            if (frame <= frames) {
                update(frames, frame, dx, dy);
            } else {
                update(frames, frames, dx, dy);
                ((Timer) event.getSource()).stop();
                action.run();
                gui.finishAnimating();
            }
        });
        timer.start();
    }

    /**
     * Draw the diver on its own panel.
     */
    @Override
    public void paintComponent(Graphics page) {
        super.paintComponent(page);
        if (gui.gameState.scramSucceeded()) {
            page.drawImage(gui.mazePanel.exitEnd, MazePanel.TILE_WIDTH * col,
                    MazePanel.TILE_HEIGHT * row,
                    MazePanel.TILE_WIDTH, MazePanel.TILE_HEIGHT, null);
            return;
        }
        page.drawImage(sprite(), posX, posY, MazePanel.TILE_WIDTH, MazePanel.TILE_HEIGHT, null);
    }

    /**
     * Update the location of the diver as necessary.
     */
    private void update(int framesPerMove, int framesIntoMove, int dx, int dy) {
        // Make the move toward our destination
        posX = MazePanel.TILE_WIDTH * col() + framesIntoMove * dx / framesPerMove;
        posY = MazePanel.TILE_HEIGHT * row() + framesIntoMove * dy / framesPerMove;
        repaint();
    }

    /**
     * Return the direction from current location (row, col) to
     * (goalRow, goalCol). If already there, return the current
     * direction.
     */
    private Direction getDirection(int row, int col, int goalRow, int goalCol) {
        if (goalRow < row) {
            return Direction.NORTH;
        }
        if (goalRow > row) {
            return Direction.SOUTH;
        }
        if (goalCol < col) {
            return Direction.WEST;
        }
        if (goalCol > col) {
            return Direction.EAST;
        }
        return dir;
    }
}
