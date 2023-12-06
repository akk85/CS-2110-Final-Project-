package diver;

import game.*;
import graph.WeightedDigraph;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.*;
import graph.WeightedDigraph;
import graph.*;



/** This is the place for your implementation of the {@code SewerDiver}.
 */
public class McDiver implements SewerDiver {

    /**
     * See {@code SewerDriver} for specification.
     */
    @Override
    public void seek(SeekState state) {
        // DO NOT WRITE ALL THE CODE HERE. DO NOT MAKE THIS METHOD RECURSIVE.
        // Instead, write your method (it may be recursive) elsewhere, with a
        // good specification, and call it from this one.
        //
        // Working this way provides you with flexibility. For example, write
        // one basic method, which always works. Then, make a method that is a
        // copy of the first one and try to optimize in that second one.
        // If you don't succeed, you can always use the first one.
        //
        // Use this same process on the second method, scram.
        Set<Long> visited = new HashSet<>();
        dfsSeek(state, visited);
    }

    private void dfsSeek(SeekState state, Set<Long> visited) {
        long currentLocation = state.currentLocation();
        visited.add(currentLocation);

        if (state.distanceToRing() == 0) {
            return;
        }
        PriorityQueue<NodeStatus> neighbors = new PriorityQueue<>(
                Comparator.comparingInt(NodeStatus::getDistanceToRing));
        for (NodeStatus n : state.neighbors()) {
            if (!visited.contains(n.getId())) {
                neighbors.add(n);
            }
        }

        while (!neighbors.isEmpty()) {
            NodeStatus next = neighbors.poll();
            if (!visited.contains(next.getId())) {
                state.moveTo(next.getId());
                dfsSeek(state, visited);
                if (state.distanceToRing() == 0) {
                    return;
                }
                state.moveTo(currentLocation);
            }
        }
    }

    /**
     * See {@code SewerDriver} for specification.
     */
    @Override
    public void scram(ScramState state) {
        // DO NOT WRITE ALL THE CODE HERE. Instead, write your method elsewhere,
        // with a good specification, and call it from this one.
        // Create a graph representation of the sewer system.
        // Initialize the graph and shortest paths utility
        // Create the graph representation of the sewer system.
        Maze maze = new Maze(new HashSet<>(state.allNodes()));
        ShortestPaths<Node, Edge> shortestPaths = new ShortestPaths<>(maze);
        shortestPaths.singleSourceDistances(state.currentNode());
        List<Edge> edgesToExit = shortestPaths.bestPath(state.exit());
        List<Node> pathToExit = edgesToNodes(edgesToExit, state.currentNode());

        if (!pathToExit.get(pathToExit.size() - 1).equals(state.exit())) {
            throw new IllegalStateException("The calculated path does not end at the exit.");
        }

        //followPath(state, pathToExit);

        if (pathToExit.size() < state.stepsToGo()) {
            collectCoins(state, pathToExit, shortestPaths);
        } else {
            followPath(state, pathToExit);
        }

        if (!state.currentNode().equals(state.exit())) {
            throw new IllegalStateException("scram() ended, but McDiver is not at the exit.");
        }
    }

    private List<Node> edgesToNodes(List<Edge> edges, Node startNode) {

        List<Node> nodes = new ArrayList<>();
        //Node current = startNode;

        for (Edge edge : edges) {
            Node next = edge.destination();
            if (!next.equals(startNode)) {
                nodes.add(next);
                startNode = next;
            }
        }
        return nodes;
    }

    private void collectCoins(ScramState state, List<Node> pathToExit,
            ShortestPaths<Node, Edge> shortestPaths) {
        Node currentNode = state.currentNode();
        while (state.stepsToGo() > pathToExit.size()) {
            Collection<Node> neighbors = currentNode.getNeighbors();
            Node bestCoinNode = null;
            double bestValue = 0;

            for (Node neighbor : neighbors) {
                double coinValue = neighbor.getTile()
                        .coins();
                double stepsToNeighbor = 1;
                double stepsFromNeighborToExit = shortestPaths.getDistance(
                        neighbor);

                if (coinValue / stepsToNeighbor > bestValue
                        && state.stepsToGo() > stepsFromNeighborToExit + stepsToNeighbor) {
                    bestValue = coinValue / stepsToNeighbor;
                    bestCoinNode = neighbor;
                }
            }

            if (bestCoinNode != null) {
                currentNode = bestCoinNode;
                state.moveTo(currentNode);
                currentNode.getTile()
                        .takeCoins();
            } else {
                break;
            }
        }

        followPath(state, pathToExit);
    }


    private void followPath(ScramState state, List<Node> path) {
        for (int i = 0; i < path.size() - 1; i++) {
            Node currentNode = path.get(i);
            Node nextNode = path.get(i + 1);

            if (!currentNode.getNeighbors().contains(nextNode)) {
                throw new IllegalArgumentException("getEdge: Node must be a neighbor of this Node");
            }

            if (state.stepsToGo() == 0){
                break;
            }

            state.moveTo(nextNode);
        }
    }
}