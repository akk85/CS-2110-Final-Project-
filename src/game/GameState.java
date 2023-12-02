package game;

import cms.util.maybe.Maybe;
import diver.SewerDiver;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import gui.GUI;

import javax.swing.*;

/** The game controller. The diver algorithm calls into this class to move the diver around,
 *  through the SeekState and ScramState interfaces.
 */
public class GameState implements SeekState, ScramState {

    private enum Phase {
        SEEK, SCRAM
    }

    private static class OutOfTimeException extends RuntimeException {

    }

    static boolean shouldPrint = true;

    /**
     * minimum and maximum number of rows
     */
    public static final int MIN_ROWS = 8, MAX_ROWS = 25;

    /**
     * minimum and maximum number of columns
     */
    public static final int MIN_COLS = 12, MAX_COLS = 40;

    /**
     * Minimum and maximum bonuses
     */
    public static final double MIN_BONUS = 1.0, MAX_BONUS = 1.3;

    /**
     * extra time factor. bigger is nicer - addition to total multiplier
     */
    private static final double EXTRA_TIME_FACTOR = 0.3;

    private static final double NO_BONUS_LENGTH = 3;

    /**
     * The seek- and scram- sewers
     */
    private final Sewers seekSewer, scramSewer;

    private final SewerDiver sewerDiver;

    private Maybe<GUI> gui;

    private final long seed;

    private Node position;

    /**
     * steps taken so far, steps left, and coins collected
     */
    private int stepsTaken, stepsToGo, coinsCollected;

    private Phase phase;
    private boolean seekSucceeded = false;
    private boolean scramSucceeded = false;
    private boolean seekErred = false;
    private boolean scramErred = false;


    /**
     * Time-out time for seek and scram phases
     */
    public static final long SEEK_TIMEOUT = 10, SCRAM_TIMEOUT = 15;
    private boolean seekTimedOut = false;
    private boolean scramTimedOut = false;

    private int minSeekDistance;
    private int minScramDistance;

    private int seekStepsLeft = 0;
    private int scramStepsLeft = 0;

    private int minSeekSteps;

    /**
     * = "scram succeeded"
     */
    public boolean scramSucceeded() {
        return scramSucceeded;
    }

    /**
     * Constructor: a new GameState object for sewerDiver sd. This constructor takes a path to files
     * storing serialized sewers and simply loads these sewers.
     */
    GameState(Path seekSewerPath, Path scramSewerPath, SewerDiver sd)
            throws IOException {
        seekSewer = Sewers.deserialize(Files.readAllLines(seekSewerPath));
        minSeekSteps = seekSewer.minPathLengthToRing(seekSewer.entrance());
        scramSewer = Sewers.deserialize(Files.readAllLines(scramSewerPath));

        sewerDiver = sd;

        position = seekSewer.entrance();
        stepsTaken = 0;
        stepsToGo = Integer.MAX_VALUE;
        coinsCollected = 0;

        seed = -1;

        phase = Phase.SEEK;
        gui = Maybe.some(new GUI(seekSewer, position.getTile().row(),
                position.getTile().column(), 0, this));
    }

    /**
     * Creates: a new game instance using seed {@code seed} with or without a GUI, and with
     * {@code SewerDiver} {@code sd} used to solve the game.
     */
    GameState(long seed, boolean useGui, SewerDiver sd) {
        Random rand = new Random(seed);
        int ROWS = rand.nextInt(MAX_ROWS - MIN_ROWS + 1) + MIN_ROWS;
        int COLS = rand.nextInt(MAX_COLS - MIN_COLS + 1) + MIN_COLS;
        seekSewer = Sewers.digExploreSewer(ROWS, COLS, rand);
        minSeekSteps = seekSewer.minPathLengthToRing(seekSewer.entrance());
        Tile ringTile = seekSewer.ring().getTile();
        scramSewer = Sewers.digGetOutSewer(ROWS, COLS, ringTile.row(), ringTile.column(), rand);

        position = seekSewer.entrance();
        stepsTaken = 0;
        stepsToGo = Integer.MAX_VALUE;
        coinsCollected = 0;

        sewerDiver = sd;
        phase = Phase.SEEK;

        this.seed = seed;

        gui = Maybe.none();
        if (useGui) {
            // Set up GUI on the event dispatch thread
            final Object mutex = new Object();
            SwingUtilities.invokeLater(() -> {
                gui = Maybe.some(new GUI(seekSewer, position.getTile().row(),
                        position.getTile().column(), seed, this));
                synchronized (mutex) { mutex.notifyAll(); }
            });
            // Wait for the GUI setup to complete
            synchronized (mutex) {
                try {
                    while (!gui.isPresent()) mutex.wait();
                } catch (InterruptedException exc) {}
            }
        }
    }

    /**
     * Run through the game, one step at a time. Will run scram() only
     * if seek() succeeds. Will fail in case of timeout.
     */
    void runWithTimeLimit() {
        seekWithTimeLimit();
        if (!seekSucceeded) {
            seekStepsLeft = seekSewer.minPathLengthToRing(position);
            scramStepsLeft = scramSewer.minPathLengthToRing(scramSewer.entrance());
        } else {
            scramWithTimeLimit();
            if (!scramSucceeded) {
                scramStepsLeft = scramSewer.minPathLengthToRing(position);
            }
        }
    }

    /**
     * Run through the game, one step at a time. Will run scram() only if seek() succeeds.  Does not
     * use a timeout and will wait as long as necessary.
     */
    void run() {
        seek();
        if (!seekSucceeded) {
            seekStepsLeft = seekSewer.minPathLengthToRing(position);
            scramStepsLeft = scramSewer.minPathLengthToRing(scramSewer.entrance());
        } else {
            scram();
            if (!scramSucceeded) {
                scramStepsLeft = scramSewer.minPathLengthToRing(position);
            }
        }
    }

    /**
     * Run only the seek phase. Uses timeout.
     */
    void runFindWithTimeout() {
        seekWithTimeLimit();
        if (!seekSucceeded) {
            seekStepsLeft = seekSewer.minPathLengthToRing(position);
        }
    }

    /**
     * Run only the scram phase. Uses timeout.
     */
    void runScramWithTimeout() {
        scramWithTimeLimit();
        if (!scramSucceeded) {
            scramStepsLeft = scramSewer.minPathLengthToRing(position);
        }
    }

    @SuppressWarnings("deprecation")
    /** Wrap a call to seek() with the timeout functionality. */
    private void seekWithTimeLimit() {
        FutureTask<Void> ft = new FutureTask<>(new Callable<Void>() {
            @Override
            public Void call() {
                seek();
                return null;
            }
        });

        Thread t = new Thread(ft);
        t.start();
        try {
            ft.get(SEEK_TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            t.stop();
            seekTimedOut = true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("ERROR");
            // Shouldn't happen
        }
    }

    /** If the GUI is active, perform some action to the gui object,
     *  on the event dispatch thread.
     */
    void onGUI(Consumer<GUI> code) {
        SwingUtilities.invokeLater(() -> gui.thenDo(code));
    }


    /**
     * Run the sewerDiver's seek() function.
     */
    void seek() {
        phase = Phase.SEEK;
        stepsTaken = 0;
        seekSucceeded = false;
        position = seekSewer.entrance();
        minSeekDistance = seekSewer.minPathLengthToRing(position);
        GUIControl.startAnimation(gui);
        onGUI(g -> {
            g.setLighting(false);
            g.updateSewer(seekSewer, 0);
            g.moveTo(position);
        });
        GUIControl.waitForAnimation(gui);

        try {
            sewerDiver.seek(this);
            // Verify that we returned at the correct location
            if (position.equals(seekSewer.ring())) {
                seekSucceeded = true;
            } else {
                errPrintln("seek(...) returned at the wrong location.");
                onGUI(g -> g.displayError(
                        "seek(f..) returned at the wrong location."));
            }
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                return;
            }
            errPrintln("seek(...) threw an exception.");
            errPrintln("Here is the output.");
            t.printStackTrace();
            onGUI(g -> g.displayError(
                    "seek(...) threw an exception. See the console output."));
            seekErred = true;
        }
    }

    @SuppressWarnings("deprecation")
    /** Wrap a call to scram() with the timeout functionality. */
    private void scramWithTimeLimit() {
        FutureTask<Void> ft = new FutureTask<>(new Callable<Void>() {
            @Override
            public Void call() {
                scram();
                return null;
            }
        });

        Thread t = new Thread(ft);
        t.start();
        try {
            ft.get(SCRAM_TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            t.stop();
            scramTimedOut = true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("ERROR"); // Shouldn't happen
        }
    }

    /**
     * Handle the logic for running the sewerDiver's scram() procedure with no timeout.
     */
    void scram() {
        phase = Phase.SCRAM;
        Tile ringTile = seekSewer.ring().getTile();
        position = scramSewer.nodeAt(ringTile.row(), ringTile.column());
        minScramDistance = scramSewer.minPathLengthToRing(position);
        stepsToGo = computeStepsToScram();
        onGUI(g -> {
            g.getOptionsPanel().changePhaseLabel("Scram phase");
            g.setLighting(true);
            g.updateSewer(scramSewer, stepsToGo);
        });

        // Pick up coins on start phase (if any)
        Node cn = currentNode();
        int coins = cn.getTile().coins();
        if (coins > 0) {
            grabCoins();
        }

        try {
            sewerDiver.scram(this);
            // Verify that the diver returned at the correct location
            if (!position.equals(scramSewer.ring())) {
                errPrintln("scram(..) returned at the wrong location.");
                onGUI(g -> g.displayError("scram(...) returned at the wrong location."));
                return;
            }

            scramSucceeded = true;
            onGUI(g -> g.getOptionsPanel().changePhaseLabel("Scram done!"));
            System.out.println("Scram Succeeded!");
            // Since the exit has been reached, turn off painting the
            onGUI(g -> g.getMazePanel().repaint());

        } catch (OutOfTimeException e) {
            errPrintln("scram(...) ran out of steps before returning!");
            onGUI(g -> g.displayError(
                    "scram(...) ran out of steps before returning!"));
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                return;
            }
            errPrintln("scram(...) threw an exception:");
            t.printStackTrace();
            onGUI(g -> g.displayError(
                    "scram(...) threw an exception. See the console output."));
            scramErred = true;
        }

        outPrintln("Coins collected   : " + getCoinsCollected());
        DecimalFormat df = new DecimalFormat("#.##");
        outPrintln("Bonus multiplier : " + df.format(computeBonusFactor()));
        outPrintln("Score            : " + getScore());
    }

    /**
     * Making sure the sewerDiver always has the minimum steps needed to get out, add a factor of
     * extra steps proportional to the size of the sewer.
     */
    private int computeStepsToScram() {
        int minStepsToScram = scramSewer.minPathLengthToRing(position);
        return (int) (minStepsToScram + EXTRA_TIME_FACTOR *
                (Sewers.MAX_EDGE_WEIGHT + 1) * scramSewer.numOpenTiles() / 2);
    }

    /**
     * Compare the sewerDiver's performance on the scram() phase to the theoretical minimum, compute
     * their bonus factor on a call from MIN_BONUS to MAX_BONUS. Bonus should be minimum if take
     * longer than NO_BONUS_LENGTH times optimal.
     */
    private double computeBonusFactor() {
        double seekDiff = (stepsTaken - minSeekSteps) / (double) minSeekSteps;
        if (seekDiff <= 0) {
            return MAX_BONUS;
        }
        double multDiff = MAX_BONUS - MIN_BONUS;
        return Math.max(MIN_BONUS, MAX_BONUS - seekDiff / NO_BONUS_LENGTH * multDiff);
    }

    /**
     * See moveTo(Node&lt;TileData&gt; n)
     *
     * @param id The Id of the neighboring Node to move to
     */
    @Override
    public void moveTo(long id) {
        if (phase != Phase.SEEK) {
            throw new IllegalStateException(
                    "moveTo(ID) can only be called while fleeing!");
        }

        for (Node n : position.getNeighbors()) {
            if (n.getId() == id) {
                position = n;
                stepsTaken++;
                GUIControl.startAnimation(gui);
                onGUI(g -> {
                    g.updateBonus(computeBonusFactor());
                    g.moveTo(n);
                });
                GUIControl.waitForAnimation(gui);
                return;
            }
        }
        throw new IllegalArgumentException(
                "moveTo: Node must be adjacent to position");
    }

    /**
     * Return the unique id of the current location.
     */
    @Override
    public long currentLocation() {
        if (phase != Phase.SEEK) {
            throw new IllegalStateException(
                    "getLocation() can be called only while fleeing!");
        }

        return position.getId();
    }

    /**
     * Return a collection of NodeStatus objects that contain the unique ID of the node and the
     * distance from that node to the ring.
     */
    @Override
    public Collection<NodeStatus> neighbors() {
        if (phase != Phase.SEEK) {
            throw new IllegalStateException(
                    "getNeighbors() can be called only while fleeing!");
        }

        Collection<NodeStatus> options = new ArrayList<>();
        for (Node n : position.getNeighbors()) {
            int distance = computeDistanceToRing(n.getTile().row(),
                    n.getTile().column());
            options.add(new NodeStatus(n.getId(), distance));
        }
        return options;
    }

    /**
     * Return the Manhattan distance from (row, col) to the ring
     */
    private int computeDistanceToRing(int row, int col) {
        return Math.abs(row - seekSewer.ring().getTile().row()) +
                Math.abs(col - seekSewer.ring().getTile().column());
    }

    /**
     * Return the Manhattan distance from the current location to the ring location on the map.
     */
    @Override
    public int distanceToRing() {
        if (phase != Phase.SEEK) {
            throw new IllegalStateException(
                    "distanceToRing() can be called only while fleeing!");
        }

        return computeDistanceToRing(position.getTile().row(),
                position.getTile().column());
    }

    @Override
    public Node currentNode() {
        if (phase != Phase.SCRAM) {
            throw new IllegalStateException("getCurrentNode: Error, " +
                    "current Node may not be accessed unless fleeing");
        }
        return position;
    }

    @Override
    public Node exit() {
        if (phase != Phase.SCRAM) {
            throw new IllegalStateException("getEntrance: Error, " +
                    "current Node may not be accessed unless fleeing");
        }
        return scramSewer.ring();
    }

    @Override
    public Collection<Node> allNodes() {
        if (phase != Phase.SCRAM) {
            throw new IllegalStateException("getVertices: Error, " +
                    "Vertices may not be accessed unless fleeing");
        }
        return Collections.unmodifiableSet(scramSewer.graph());
    }

    /**
     * Attempt to move the sewerDiver from the current position to the {@code Node n}. Throw {@code
     * IllegalArgumentException} if {@code n} is not neighboring. Increment the steps taken if
     * successful.
     */
    @Override
    public void moveTo(Node n) {
        if (phase != Phase.SCRAM) {
            throw new IllegalStateException("Call moveTo(Node) only when fleeing!");
        }
        int distance = position.getEdge(n).length;
        if (stepsToGo - distance < 0) {
            throw new OutOfTimeException();
        }

        if (!position.getNeighbors().contains(n)) {
            throw new IllegalArgumentException(
                    "moveTo: Node must be adjacent to position");
        }
        position = n;
        stepsToGo -= distance;
        GUIControl.startAnimation(gui);
        onGUI(g-> {
            g.updateStepsToGo(stepsToGo);
            g.moveTo(n);
        });
        GUIControl.waitForAnimation(gui);
        grabCoins();
    }

    /**
     * Pick up coins. Coins on a {@code Node n} are picked up automatically when the scram phase
     * starts and when a call {@code moveTo(n)} is executed.
     */
    void grabCoins() {
        if (phase != Phase.SCRAM) {
            throw new IllegalStateException("Call grabCoins() only when fleeing!");
        }
        coinsCollected += position.getTile().takeCoins();
        onGUI(g -> g.updateCoins(coinsCollected, getScore()));
    }

    @Override
    /** Return the number of steps remaining to scram. */
    public int stepsToGo() {
        if (phase != Phase.SCRAM) {
            throw new IllegalStateException(
                    "stepsToGo() can be called only while fleeing!");
        }
        return stepsToGo;
    }

    int getCoinsCollected() {
        return coinsCollected;
    }

    /**
     * Return the player's current score.
     */
    int getScore() {
        return (int) (computeBonusFactor() * coinsCollected);
    }

    boolean getSeekSucceeded() {
        return seekSucceeded;
    }

    boolean getScramSucceeded() {
        return scramSucceeded;
    }

    boolean getSeekErrored() {
        return seekErred;
    }

    boolean getScramErrored() {
        return scramErred;
    }

    boolean getSeekTimeout() {
        return seekTimedOut;
    }
    boolean getScramTimeout() { return scramTimedOut; }
    /**
     * Given seed, whether to use the GUI, and an instance of a solution, run the
     * game using that solution.
     */
    public static int runNewGame(long seed, boolean useGui, SewerDiver solution) {
        GameState state;
        state = new GameState(seed, useGui, solution);
        outPrintln("Seed : " + state.seed);
        state.run();
        return state.getScore();
    }

    static void outPrintln(String s) {
        if (shouldPrint) {
            System.out.println(s);
        }
    }

    static void errPrintln(String s) {
        if (shouldPrint) {
            System.err.println(s);
        }
    }
}
