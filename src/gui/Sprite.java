package gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * An instance maintains information about a sprite for the GUI. A sprite is defined by a
 * spritesheet, the information to interpret it (size of each image, etc.), and information
 * necessary to animate the sprite.
 */
public class Sprite {

    /**
     * The entire spritesheet
     */
    private BufferedImage spriteSheet;

    /**
     * Width and height of a single image on the spritesheet
     */
    private int tileWidth, tileHeight;

    /**
     * Number of images in a single animation
     */
    private int cycleSize;

    /**
     * The current image in the animation (in [0, cycleSize - 1])
     */
    private int cycle = 0;

    /**
     * Constructor: an instance with image at imageLoc, of size (width, height), and number of
     * frames in the animation cycleSize.
     */
    public Sprite(String imageLoc, int width, int height, int cycleSize) {
        tileWidth = width;
        tileHeight = height;
        this.cycleSize = cycleSize;
        try {
            spriteSheet = ImageIO.read(new File(imageLoc));
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Creating sprite failed. " + imageLoc + " not found.");
        }
    }

    /**
     * Update the spritesheet's animation by a single frame.
     */
    public void tick() {
        cycle = (cycle + 1) % cycleSize;
    }

    /**
     * Return offset (dRow, dCol) into the spritesheet. Assume that (dRow, dCol) is the base
     * offset, and subsequent animations are at (dRow, dCol + 1), (dRow, dCol + 2) ... (dRow,
     * dCol + cycleSize - 1)
     *
     * @param dRow the number of rows to offset into the spritesheet for the first animation
     * @param dCol the number of columns to offset into the spritesheet for the first animation
     */
    public BufferedImage getSprite(int dRow, int dCol) {
        if (spriteSheet == null) {
            throw new IllegalArgumentException(
                    "Can't get sprite until you've initialized sprite sheet.");
        }
        return spriteSheet.getSubimage((dCol + cycle) * tileWidth, dRow * tileHeight, tileWidth,
                tileHeight);
    }
}
