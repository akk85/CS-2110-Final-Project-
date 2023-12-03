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
        // TODO : Look for the ring and return.
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
            // Found the ring, return successfully
            return;
        }
        PriorityQueue<NodeStatus> neighbors = new PriorityQueue<>(
                Comparator.comparingInt(NodeStatus::getDistanceToRing));
        for (NodeStatus n : state.neighbors()) {
            // Add only unvisited neighbors
            if (!visited.contains(n.getId())) {
                neighbors.add(n);
            }
        }

        while (!neighbors.isEmpty()) {
            NodeStatus next = neighbors.poll();
            if (!visited.contains(next.getId())) {
                state.moveTo(next.getId());
                dfsSeek(state, visited);
                // After returning from the recursive call, check if the ring was found
                if (state.distanceToRing() == 0) {
                    // Ring found, return successfully
                    return;
                }
                // Backtrack to previous location if ring not found
                state.moveTo(currentLocation);
            }
        }
    }

    /**
     * See {@code SewerDriver} for specification.
     */
    @Override
    public void scram(ScramState state) {
        // TODO: Get out of the sewer system before the steps are used up.
        // DO NOT WRITE ALL THE CODE HERE. Instead, write your method elsewhere,
        // with a good specification, and call it from this one.
        // Create a graph representation of the sewer system.
        // Initialize the graph and shortest paths utility
        // Create the graph representation of the sewer system.
        Maze maze = new Maze(new HashSet<>(state.allNodes()));

        // Create an instance of ShortestPaths with the maze as the argument.
        ShortestPaths<Node, Edge> shortestPaths = new ShortestPaths<>(maze);

        // Compute the shortest paths from the current node to all nodes.
        shortestPaths.singleSourceDistances(state.currentNode());

        // Retrieve the best path to the exit as a list of edges.
        List<Edge> edgesToExit = shortestPaths.bestPath(state.exit());

        // Debugging: print out edges and nodes to check continuity.
        System.out.println("Path edges and nodes:");
        Node prevDestination = null;
        for (Edge edge : edgesToExit) {
            System.out.println(edge.source().getId() + " -> " + edge.destination().getId());
            if (prevDestination != null && !prevDestination.equals(edge.source())) {
                System.out.println("Discontinuity found between edges ending at " +
                        prevDestination.getId() + " and starting at " +
                        edge.source().getId());
                // You may want to throw an exception or handle this situation appropriately.
            }
            prevDestination = edge.destination();
        }

        // Convert the list of edges to a list of nodes.
        List<Node> pathToExit = edgesToNodes(edgesToExit, state.currentNode());

        // If you have time (steps) left after the shortest path, collect coins.
        if (pathToExit.size() < state.stepsToGo()) {
            collectCoins(state, pathToExit, shortestPaths);
        } else {
            // If you do not have extra steps, just follow the shortest path.
            followPath(state, pathToExit);
        }
    }

    private List<Node> edgesToNodes(List<Edge> edges, Node startNode) {
        List<Node> path = new ArrayList<>();
        if (edges.isEmpty()) {
            // If there are no edges, add the start node and return.
            path.add(startNode);
            return path;
        }

        // Start from the destination of the last edge in the list, which is the actual start node of the path
        Node current = edges.get(edges.size() - 1).destination();
        path.add(current);

        // Iterate through the edges in reverse to build the path correctly
        for (int i = edges.size() - 1; i >= 0; i--) {
            Edge e = edges.get(i);
            // The next node in the path should be the source of the current edge
            current = e.source();
            path.add(current);
        }

        // Since the path is constructed in reverse, we need to reverse it to get the correct order
        Collections.reverse(path);

        return path;
    }

    private void collectCoins(ScramState state, List<Node> pathToExit,
            ShortestPaths<Node, Edge> shortestPaths) {
        // Placeholder for a method that collects coins while ensuring McDiver can reach the exit in time.
        // You would use the number of steps left and the distances to the exit to guide the coin collection.
        Node currentNode = state.currentNode();
        while (state.stepsToGo()
                > pathToExit.size()) { // Ensure there's enough steps left to reach the exit
            Collection<Node> neighbors = currentNode.getNeighbors();
            Node bestCoinNode = null;
            double bestValue = 0;

            // Find the neighboring node with the best coin-to-step ratio
            for (Node neighbor : neighbors) {
                double coinValue = neighbor.getTile()
                        .coins(); // Assume getCoins() method returns the coin value of a tile
                double stepsToNeighbor = 1; // Assuming each move takes one step
                double stepsFromNeighborToExit = shortestPaths.getDistance(
                        neighbor); // Assume getDistance() method returns the shortest distance from a node to the exit

                if (coinValue / stepsToNeighbor > bestValue
                        && state.stepsToGo() > stepsFromNeighborToExit + stepsToNeighbor) {
                    bestValue = coinValue / stepsToNeighbor;
                    bestCoinNode = neighbor;
                }
            }

            // If a node with coins was found, move to it and collect the coins
            if (bestCoinNode != null) {
                currentNode = bestCoinNode;
                state.moveTo(currentNode);
                currentNode.getTile()
                        .takeCoins(); // Assume collectCoins() method collects the coins and updates the tile's coin value
            } else {
                // If no coins are nearby, start heading towards the exit
                break;
            }
        }

        // After collecting coins, follow the path to the exit
        List<Edge> edgesToExit = shortestPaths.bestPath(state.exit());
        List<Node> pathToExit1 = edgesToNodes(edgesToExit, state.currentNode());
        followPath(state, pathToExit1);
    }


    private void followPath(ScramState state, List<Node> path) {
//        // Traverse the path to the exit, ensuring that McDiver exits in time.
//        for (Node node : path) {
//            if (state.stepsToGo() > 0) {
//                state.moveTo(node);
//            }
//        }
//    }
        for (int i = 0; i < path.size() - 1; i++) {
            Node current = path.get(i);
            Node next = path.get(i + 1);

            // Check if 'next' is a direct neighbor of 'current'.
            if (!current.getNeighbors().contains(next)) {
                throw new IllegalArgumentException("Node " + next.getId() +
                        " is not a neighbor of " + current.getId());
            }

            // Attempt the move.
            state.moveTo(next);
        }
    }
}




