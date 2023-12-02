package gui;

import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;

import game.GameState;

/**
 * An instance is a JPanel with info for the user, <br> including a slider to speed up McDiver's
 * movements.
 */
public class OptionsPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;

    /**
     * Minimum and maximum speed for the game (seconds per move)
     */
    private static double MIN_SPEED = 0.05, MAX_SPEED = 0.85;

    private JSlider speedSelect;
    private JProgressBar stepsLeft;
    private JButton showSeed;

    /**
     * Description for speed slider and current phase
     */
    private JLabel speedLabel = new JLabel("Speed:"),
            phaseLabel = new JLabel("Find phase");

    /**
     * bonus multiplier
     */
    private JLabel bonusLabel = new JLabel("Bonus: " + GameState.MAX_BONUS);

    /**
     * number of coins picked up multiplied by bonus factor
     */
    private JLabel coinsLabel = new JLabel("Coins: 0"),
            scoreLabel = new JLabel("Score: 0");

    /**
     * Number of steps left
     */
    private JLabel stepsLeftLabel = new JLabel("Steps left: 0");

    /**
     * Background for options pane
     */
    private BufferedImage background;

    /**
     * Location of background image
     */
    private String BACKGROUND_PATH = "res/info_texture.png";

    /**
     * seed used to generate the graph
     */
    long seed;

    // private Box box; // contains all the other things

    /**
     * Constructor: an instance is a JPanel with top-left corner (x, y), width width,<br> height
     * height, and random number seed seed --which was used t generate the graph.
     */
    public OptionsPanel(int x, int y, int width, int height, long seed) {
        /** The slider is used to provide a value, in seconds per move, <br>
         * for the speed at which the character moves. <br>
         * The min and max values are defined as MIN_SPEED and MAX_SPEED, respectively.<br>
         * In order to even out the scaling of speed, the actual speed s is <br>
         * defined relative to the slider value v as follows: s = 10^(-v/1000). */
        int lowVal = (int) (Math.log10(MAX_SPEED) * -1000);
        int highVal = (int) (Math.log10(MIN_SPEED) * -1000);
        int startVal = (int) (-1000 *
                Math.log10((double) GUI.FRAMES_PER_MOVE / GUI.FRAMES_PER_SECOND));
        speedSelect = new JSlider(JSlider.HORIZONTAL, lowVal, highVal, startVal);
        speedSelect.addChangeListener((e) -> GUI.FRAMES_PER_MOVE = (int) (GUI.FRAMES_PER_SECOND *
                Math.pow(10, -(double) speedSelect.getValue() / 1000.0)));

        stepsLeft = new JProgressBar(0, 100);
        this.seed = seed;

        setLayout(new GridLayout(7, 1));

        // JPanel sliderPanel= new JPanel();
        Box sliderBox = new Box(BoxLayout.Y_AXIS);
        sliderBox.add(speedLabel);
        sliderBox.add(speedSelect);
        sliderBox.setOpaque(false);

        Box stepsLeftBox = new Box(BoxLayout.Y_AXIS);
        stepsLeftBox.add(stepsLeftLabel);
        Box stepsWithStruts = new Box(BoxLayout.X_AXIS);
        stepsWithStruts.add(Box.createHorizontalStrut(14));
        stepsWithStruts.add(stepsLeft);
        stepsWithStruts.add(Box.createHorizontalStrut(14));
        stepsLeftBox.add(stepsWithStruts);
        // stepsLeftBox.add(stepsLeft);
        stepsLeftBox.setOpaque(false);

        JPanel showSeedPanel = new JPanel();
        showSeed = new JButton("Print seed");
        showSeed.addActionListener(this);
        showSeedPanel.setOpaque(false);
        showSeedPanel.add(showSeed);

        bonusLabel.setHorizontalAlignment(JLabel.CENTER);
        phaseLabel.setHorizontalAlignment(JLabel.CENTER);
        coinsLabel.setHorizontalAlignment(JLabel.CENTER);
        stepsLeftLabel.setHorizontalAlignment(JLabel.CENTER);
        speedLabel.setHorizontalAlignment(JLabel.CENTER);
        scoreLabel.setHorizontalAlignment(JLabel.CENTER);
        phaseLabel.setAlignmentX(Box.CENTER_ALIGNMENT);
        coinsLabel.setAlignmentX(Box.CENTER_ALIGNMENT);
        bonusLabel.setAlignmentX(Box.CENTER_ALIGNMENT);
        scoreLabel.setAlignmentX(Box.CENTER_ALIGNMENT);

        add(sliderBox);
        add(stepsLeftBox);

        add(phaseLabel);
        add(bonusLabel);
        add(coinsLabel);
        add(scoreLabel);
        // add(labels);
        add(showSeedPanel);

        setBounds(x, y, width, height);

        // Load content
        try {
            background = ImageIO.read(new File(BACKGROUND_PATH));
        } catch (IOException e) {
            throw new IllegalArgumentException("Can't find input file: " + e.toString());
        }
    }

    /**
     * Update bonus multiplier b as displayed by the GUI
     */
    public void updateBonus(double b) {
        DecimalFormat df = new DecimalFormat("#.##");
        bonusLabel.setText("Bonus: " + df.format(b));
    }

    /**
     * Update the number of coins c picked up as displayed on the GUI.<br> Score is the current
     * player's score.
     */
    public void updateCoins(int c, int score) {
        coinsLabel.setText("Coins: " + c);
        scoreLabel.setText("Score: " + score);
    }

    /**
     * Change phase label to s.
     */
    public void changePhaseLabel(String s) {
        phaseLabel.setText(s);
    }

    /**
     * Update the steps t left as displayed on the GUI.
     */
    public void updateStepsLeft(int t) {
        stepsLeftLabel.setText("Steps left: " + t);
        stepsLeft.setValue(t);
    }

    /**
     * Update the maximum number of steps left, m, for this stage.
     */
    public void updateMaxStepsLeft(int m) {
        stepsLeft.setMaximum(m);
    }

    /**
     * Paint the component
     */
    @Override
    public void paintComponent(Graphics page) {
        super.paintComponent(page);
        page.drawImage(background, 0, 0, getWidth(), getHeight(), null);
    }

    /**
     * When showSeed button clicked, print the seed in the console.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == showSeed) {
            System.out.println("Seed: " + seed);
        }
    }
}
