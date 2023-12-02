package gui;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;

import game.Node;

/**
 * An instance is a panel that displays information about a currently selected Tile.
 */
public class TileSelectPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    /**
     * Background for options pane
     */
    private BufferedImage background;
    private String BACKGROUND_PATH = "res/info_texture.png";

    /**
     * Tells user how to use this panel
     */
    private JLabel instructions;

    /**
     * Information about the larger GUI
     */
    private GUI gui;

    /**
     * Font size to use for the information?
     */
    private static final int FONT_SIZE = 16;

    /**
     * Location of the rectangle to display, in proportions of the total panel
     */
    private static final double RECT_X = 0.1, RECT_Y = 0.1, RECT_WIDTH = 0.75, RECT_HEIGHT = 0.65;

    /**
     * Height proportion at which to display tile type
     */
    private static final double TYPE_HEIGHT = 0.17;

    /**
     * Location at which to draw the image
     */
    private static final double IMAGE_X = 0.28, IMAGE_Y = 0.20, IMAGE_WIDTH = 0.38, IMAGE_HEIGHT = 0.25;

    /**
     * Location at which to draw the location info
     */
    private static final double ROW_X = 0.15;
    private static final double COL_X = 0.47;
    private static final double ROW_WIDTH = 0.32;
    private static final double COL_WIDTH = 0.33;
    private static final double ROW_COL_Y = 0.60;

    /**
     * Location at which to draw the coin/id info
     */
    private static final double COIN_X = 0.15;
    private static final double ID_X = 0.47;
    private static final double COIN_WIDTH = 0.32;
    private static final double ID_WIDTH = 0.33;
    private static final double COIN_ID_Y = 0.70;

    /**
     * The currently selected node
     */
    private Node selectedNode;

    /**
     * Constructor: an instance at (x, y) with size (width, height) on Gui gui.
     */
    public TileSelectPanel(int x, int y, int width, int height, GUI gui) {
        this.gui = gui;

        instructions = new JLabel("Select a tile for more info");
        add(instructions);
        setBounds(x, y, width, height);

        // Load content
        try {
            background = ImageIO.read(new File(BACKGROUND_PATH));
        } catch (IOException e) {
            throw new IllegalArgumentException("Can't find input file: " + e.toString());
        }
    }

    /**
     * Update the location to (x, y) with size (width, height) <br> of this element (for instance,
     * on screen resize).
     */
    public void updateLoc(int x, int y, int width, int height) {
        setBounds(x, y, width, height);
        repaint();
    }

    /**
     * Select node n on the GUI. <br> This displays information on n's panel on the screen to the
     * right.
     */
    public void selectNode(Node n) {
        selectedNode = n;
        repaint();
    }

    /**
     * Return the minimum x-coordinate to draw s in order to center it.<br> minX is the minimum x to
     * center over,<br> width is the width of the area to center over
     */
    private int getTextXForCenter(Graphics2D graphics, String s, int minX, int width) {
        FontMetrics fm = graphics.getFontMetrics();
        Rectangle2D r = fm.getStringBounds(s, graphics);
        return minX + (width - (int) r.getWidth()) / 2;
    }

    /**
     * Paint this component using page.
     */
    @Override
    public void paintComponent(Graphics page) {
        super.paintComponent(page);
        page.drawImage(background, 0, 0, getWidth(), getHeight(), null);

        // Draw the rectangle outline the information panel
        page.drawRect((int) (RECT_X * getWidth()), (int) (RECT_Y * getHeight()),
                (int) (RECT_WIDTH * getWidth()), (int) (RECT_HEIGHT * getHeight()));

        // Display information about a node if one is present
        if (selectedNode != null) {
            // Draw the Tile type
            String text = selectedNode.getTile().type().toString();
            page.setFont(new Font("default", Font.BOLD, FONT_SIZE));
            int x = getTextXForCenter((Graphics2D) page, text, (int) (RECT_X * getWidth()),
                    (int) (RECT_WIDTH * getWidth()));
            page.drawString(text, x, (int) (TYPE_HEIGHT * getHeight()));

            // Draw the image
            BufferedImage pic = gui.getIcon(selectedNode.getTile().type());
            page.drawImage(pic, (int) (IMAGE_X * getWidth()), (int) (IMAGE_Y * getHeight()),
                    (int) (IMAGE_WIDTH * getWidth()), (int) (IMAGE_HEIGHT * getHeight()), null);
            // Draw the coin image
            if (selectedNode.getTile().coins() > 0) {
                BufferedImage coinPic = gui.getCoinIcon(selectedNode);
                page.drawImage(coinPic, (int) (IMAGE_X * getWidth()), (int) (IMAGE_Y * getHeight()),
                        (int) (IMAGE_WIDTH * getWidth()), (int) (IMAGE_HEIGHT * getHeight()), null);
            }

            // Draw the coordinates
            text = "Row: " + selectedNode.getTile().row();
            page.setFont(new Font("default", Font.ROMAN_BASELINE, FONT_SIZE));
            x = getTextXForCenter((Graphics2D) page, text, (int) (ROW_X * getWidth()),
                    (int) (ROW_WIDTH * getWidth()));
            page.drawString(text, x, (int) (ROW_COL_Y * getHeight()));
            text = "Col: " + selectedNode.getTile().column();
            x = getTextXForCenter((Graphics2D) page, text, (int) (COL_X * getWidth()),
                    (int) (COL_WIDTH * getWidth()));
            page.drawString(text, x, (int) (ROW_COL_Y * getHeight()));

            // Draw the value of the coins and the ID
            text = "value: " + selectedNode.getTile().coins();
            x = getTextXForCenter((Graphics2D) page, text, (int) (COIN_X * getWidth()),
                    (int) (COIN_WIDTH * getWidth()));
            page.drawString(text, x, (int) (COIN_ID_Y * getHeight()));
            text = "ID: " + selectedNode.getId();
            x = getTextXForCenter((Graphics2D) page, text, (int) (ID_X * getWidth()),
                    (int) (ID_WIDTH * getWidth()));
            page.drawString(text, x, (int) (COIN_ID_Y * getHeight()));
        }
    }
}
