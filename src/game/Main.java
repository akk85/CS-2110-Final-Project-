package game;

import diver.McDiver;
import java.util.Locale;
import java.util.Random;

/** The main program for the McDiver application. Run with --help to see the various options.
 */
public class Main {
    static long seed = new Random().nextLong();
    static boolean useGUI = true;
    /**
     * The main program. By default, runs seek() and scram() on a random seed, with a
     * graphical user interface.
     */
    public static void main(String[] args) {

        int argi = 0;
        // parse options
        boolean valid = true;
        int runs = 1;
        while (valid && argi < args.length) {
            if (args[argi].charAt(0) != '-') break;
            switch (args[argi++].toLowerCase(Locale.ROOT)) {
                case "-s":
                    try {
                        seed = Long.parseLong(args[argi++]);
                    } catch (NumberFormatException e) {
                        System.err.println("Error, -s must be followed by a numeric seed");
                        return;
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.err.println("Error, -s must be followed by a seed");
                        return;
                    }
                    break;
                case "-n":
                    try {
                        runs = Integer.parseInt(args[argi++]);
                    } catch (NumberFormatException exc) {
                        runs = 1;
                    }
                    break;
                case "--nographics":
                    useGUI = false;
                    break;
                case "--help":
                    usage();
                    return;
            }
        }
        if (argi != args.length) {
            usage();
            return;
        }

        int totalScore = 0;
        for (int i = 0; i < runs; i++) {
            totalScore += GameState.runNewGame(seed, useGUI, new McDiver());
            seed = new Random(seed).nextLong();
            System.out.println();
        }

        if (runs > 1) {
            System.out.println("Average score : " + totalScore / runs);
        }
    }

    /** Effect: Prints a usage message. */
    public static void usage() {
        System.out.println("Usage: Main [--help] [-s <seed>] [-n <runs>] [--nographics]");
    }
}
