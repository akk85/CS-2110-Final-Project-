package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import game.Node;
import game.Sewers;
import game.Tile;

/**
 * An instance is responsible for drawing the underlying maze on the screen.<br> The MazePanel
 * should contain only static images that don't need to change <br> unless the screen is redrawn.
 */
public class MazePanel extends JPanel {

    private static final long serialVersionUID = 1L;
    /**
     * The gui on which this panel resides
     */
    private GUI gui;

    /**
     * Paths to ring image and the image representing a path
     */
    private static final String RING_PATH = "res/ring2.png", PATH_PATH = "res/path.png";

    /**
     * Paths to wall image and coin image
     */
    private static final String WALL_PATH = "res/wall.png", COIN_PATH = "res/coins.png";

    /**
     * Paths to manhole and exit
     */
    private static final String ENTRANCE_PATH = "res/manhole.png",
            Final_EXIT_PATH = "res/coinpersonSmall.png";
    private static final String TASTY_PATH = "res/pizza.png";
    private static final String BACKGROUND_PATH = "res/info_texture.png";

    /**
     * The width and height (in pixels) of a tile on the grid
     */
    public static int TILE_WIDTH, TILE_HEIGHT;

    /**
     * Images representing an area the diver can walk on and a wall
     */
    private final BufferedImage path, wall;

    /**
     * Image representing the ring
     */
    private final BufferedImage ring;

    /**
     * Images representing entrance at beginning and a man emerging from hole
     */
    protected final BufferedImage entrance, exitEnd;

    private final BufferedImage tasty;

    /**
     * Image representing a coin spritesheet
     */
    private final Sprite coinSheet;

    /**
     * The background
     */
    private BufferedImage background;

    private boolean diverSpriteExitPainted = false;

    /**
     * Representation of the graph/level
     */
    private Sewers sewers;

    /**
     * Contains the nodes already visited
     */
    private boolean[][] visited;

    /**
     * Darkness of path. Lower values means darker
     */
    private static final float DARK_FACTOR = 0.3f;

    /**
     * Color to place over unvisited paths
     */
    private Color darkness;
    private static final int COIN_SPRITES_PER_ROW = 7;
    private static final int COIN_SPRITES_PER_COL = 2;

    /**
     * Create a new MazePanel of a given size.<br>
     *
     * @param sew          The Sewer to display
     * @param screenWidth  The width of the panel, in pixels
     * @param screenHeight The height of the panel, in pixels
     * @param gui          The GUI that owns this MazePanel
     */
    public MazePanel(Sewers sew, int screenWidth, int screenHeight, GUI gui) {
        this.gui = gui;
        sewers = sew;
        visited = new boolean[sewers.rowCount()][sewers.columnCount()];

        // Compute the dimensions of an individual tile
        TILE_WIDTH = (int) (screenWidth * 1.0 / sewers.columnCount());
        TILE_HEIGHT = (int) (screenHeight * 0.95 / sewers.rowCount());
        // Force tiles to be square
        TILE_WIDTH = Math.min(TILE_WIDTH, TILE_HEIGHT);
        TILE_HEIGHT = Math.min(TILE_WIDTH, TILE_HEIGHT);

        // Load content
        try {
            path = ImageIO.read(new File(PATH_PATH));
            wall = ImageIO.read(new File(WALL_PATH));
            ring = ImageIO.read(new File(RING_PATH));
            coinSheet = new Sprite(COIN_PATH, 32, 32, 1);
            entrance = ImageIO.read(new File(ENTRANCE_PATH));
            exitEnd = ImageIO.read(new File(Final_EXIT_PATH));
            tasty = ImageIO.read(new File(TASTY_PATH));
            background = ImageIO.read(new File(BACKGROUND_PATH));
        } catch (IOException e) {
            throw new IllegalArgumentException("Can't find input file : " + e.toString());
        }

        // Create the dark path
        darkness = new Color(0, 0, 0, (int) (256 - 256 * DARK_FACTOR));

        // Add listener for clicking tiles
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = e.getY() / TILE_HEIGHT;
                int col = e.getX() / TILE_WIDTH;
                if (row < sewers.rowCount() && col < sewers.columnCount()) {
                    gui.selectNode(sewers.nodeAt(row, col));
                }
            }
        });
    }

    /**
     * Set the sewers to c
     */
    void setSewer(Sewers c) {
        sewers = c;
        repaint();
    }

    /**
     * The screen size has changed. <br> Adjust the maze panel to (width, height).
     */
    void updateScreenSize(int width, int height) {
        TILE_WIDTH = (int) (width * 1.0 / sewers.columnCount());
        TILE_HEIGHT = (int) (height * 0.95 / sewers.rowCount());
        // Force tiles to be square
        TILE_WIDTH = Math.min(TILE_WIDTH, TILE_HEIGHT);
        TILE_HEIGHT = Math.min(TILE_WIDTH, TILE_HEIGHT);
        repaint();
    }

    /**
     * Set the sewers to be all light (light = true) or all dark.
     */
    public void setLighting(boolean light) {
        for (int i = 0; i < sewers.rowCount(); i++) {
            for (int j = 0; j < sewers.columnCount(); j++) {
                visited[i][j] = light;
            }
        }
        repaint();
    }

    /**
     * Update the GUI to inform it that tile (row, col) was visited.
     */
    public void setVisited(int row, int col) {
        visited[row][col] = true;
    }

    /**
     * Return an image representing tile type type.
     */
    public BufferedImage getIcon(Tile.TileType tileType) {
        switch (tileType) {
            case FLOOR:
                return path;
            case RING:
                return ring;
            case ENTRANCE: {
		    if (gui.gameState.scramSucceeded()) {
			    return exitEnd;
		    }
                return entrance;
            }
            case WALL:
                return wall;
            default:
                return path;
        }
    }

    /**
     * Return an icon for the coins on tile n, or null if no coins.
     */
    public BufferedImage getCoinsIcon(Node n) {
        double cns = n.getTile().coins();
	    if (cns == Sewers.TASTY_VALUE) {
		    return tasty;
	    }
        cns *= (double) COIN_SPRITES_PER_ROW * COIN_SPRITES_PER_COL / Sewers.MAX_COIN_VALUE;
        int spriteIndex = (int) cns;
        int rowIndex = spriteIndex / COIN_SPRITES_PER_ROW;
        int colIndex = spriteIndex % COIN_SPRITES_PER_ROW;
        return coinSheet.getSprite(rowIndex, colIndex);
    }

    /**
     * Draw the maze on the screen.
     */
    @Override
    public void paintComponent(Graphics page) {
        super.paintComponent(page);
        for (int i = 0; i < getWidth(); i += 100) {
            page.drawImage(background, i, 0, 100, getHeight(), null);
        }
        page.setColor(darkness);

        // Draw the maze tiles
        for (int row = 0; row < sewers.rowCount(); row++) {
            for (int col = 0; col < sewers.columnCount(); col++) {
                if (sewers.tileAt(row, col).type() == Tile.TileType.WALL) {
                    page.drawImage(wall, TILE_WIDTH * col, TILE_HEIGHT * row,
                            TILE_WIDTH, TILE_HEIGHT, null);
                    continue;
                }

                // Draw the path image to the background
                page.drawImage(path, TILE_WIDTH * col, TILE_HEIGHT * row,
                        TILE_WIDTH, TILE_HEIGHT, null);
                // Darken this tile if the diver has not visited it yet
                if (!visited[row][col]) {
                    page.fillRect(TILE_WIDTH * col, TILE_HEIGHT * row,
                            TILE_WIDTH, TILE_HEIGHT);
                }
                // If this is the ring-tile, draw the ring
                if (sewers.tileAt(row, col).type() == Tile.TileType.RING) {
                    page.drawImage(ring, TILE_WIDTH * col, TILE_HEIGHT * row,
                            TILE_WIDTH, TILE_HEIGHT, null);
                }
                // If there is a coin here, draw it
                if (sewers.tileAt(row, col).coins() > 0) {
                    page.drawImage(getCoinsIcon(sewers.nodeAt(row, col)),
                            TILE_WIDTH * col, TILE_HEIGHT * row,
                            TILE_WIDTH, TILE_HEIGHT, null);
                }
                paintEntranceExit(page, row, col);
            }
        }
    }

    /**
     * If tile (row, col) is the entrance/exit, draw the graphic for it.
     */
    public void paintEntranceExit(Graphics page, int row, int col) {

	    if (sewers.tileAt(row, col).type() != Tile.TileType.ENTRANCE) {
		    return;
	    }
        if (gui.gameState.scramSucceeded()) {
            page.drawImage(path, TILE_WIDTH * col, TILE_HEIGHT * row,
                    TILE_WIDTH, TILE_HEIGHT, null);
            if (!diverSpriteExitPainted) {
                gui.diver.repaint();
                diverSpriteExitPainted = true;
            }
            page.drawImage(exitEnd, TILE_WIDTH * col, TILE_HEIGHT * row,
                    TILE_WIDTH, TILE_HEIGHT, null);
            // System.out.println("paintEntranceExit. drew exitEnd");
            return;
        }
        page.drawImage(entrance, TILE_WIDTH * col, TILE_HEIGHT * row,
                TILE_WIDTH, TILE_HEIGHT, null);
    }

}
