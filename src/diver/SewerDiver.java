package diver;

import game.ScramState;
import game.SeekState;

/**
 * The interface a sewer diver must implement in order to be used in solving the game.
 */
public interface SewerDiver {
    /**
     * Find the ring in as few steps as possible. Once you get there, you must return from this
     * function in order to pick it up. If you continue to move after finding the ring rather than
     * returning, it will not count. If you return from this function while not standing on top of
     * the ring,  it will count as a failure.
     * <p>
     * There is no limit to how many steps you can take, but you will receive a score bonus
     * multiplier for finding the ring in fewer steps.
     * <p>
     * At every step, you know only your current tile's ID and the ID of all open neighbor tiles, as
     * well as the distance to the ring at each of these tiles (ignoring walls and obstacles).
     * <p>
     * In order to get information about the current state, use functions currentLocation(),
     * neighbors(), and distanceToRing() in state. You know you are standing on the ring when
     * distanceToRing() is 0.
     * <p>
     * Use function moveTo(long id) in state to move to a neighboring tile by its ID. Doing this
     * will change state to reflect your new position.
     * <p>
     * A suggested first implementation that will always find the ring, but likely won't receive a
     * large bonus multiplier, is a depth-first walk. Some modification is necessary to make the
     * search better, in general.
     */
    void seek(SeekState state);

    /**
     * Scram --get out of the sewer system before the steps are all used, trying to collect as many
     * coins as possible along the way. McDiver must ALWAYS get out before the steps are all used,
     * and this should be prioritized above collecting coins.
     * <p>
     * You now have access to the entire underlying graph, which can be accessed through ScramState.
     * currentNode() and exit() return Node objects of interest, and allNodes() returns a collection
     * of all nodes on the graph.
     * <p>
     * You have to get out of the sewer system in the number of steps given by stepsToGo(); for each
     * move along an edge, this number is decremented by the weight of the edge taken.
     * <p>
     * Use moveTo(n) to move to a node n that is adjacent to the current node. When n is moved-to,
     * coins on node n are automatically picked up.
     * <p>
     * You must return from this function while standing at the exit. Failing to do so before steps
     * run out or returning from the wrong node will be considered a failed run.
     * <p>
     * Initially, there are enough steps to get from the starting point to the exit using the
     * shortest path, although this will not collect many coins. For this reason, a good starting
     * solution is to use the shortest path to the exit.
     */
    void scram(ScramState state);
}
