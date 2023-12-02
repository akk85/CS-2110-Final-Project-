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

    /** See {@code SewerDriver} for specification. */
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
            //ring found, end the search
            return;
        }
        int currentDistanceToRing = state.distanceToRing();
        for (NodeStatus neighbor : state.neighbors()) {
            if (!visited.contains(neighbor.getId())) {
                state.moveTo(neighbor.getId());
                if (state.distanceToRing() <= currentDistanceToRing) {
                    dfsSeek(state, visited);
                }
                state.moveTo(currentLocation);
            }
        }
    }

    /** See {@code SewerDriver} for specification. */
    @Override
    public void scram(ScramState state) {
        // TODO: Get out of the sewer system before the steps are used up.
        // DO NOT WRITE ALL THE CODE HERE. Instead, write your method elsewhere,
        // with a good specification, and call it from this one.
        WeightedDigraph<Node, Edge> graph = createGraph(state);
        ShortestPaths<Node, Edge> shortestPaths = new ShortestPaths<>(graph);

        List<Node> pathToExit = getPathToExit(shortestPaths, state.exit());
        followPath(state, pathToExit);

    }

    private WeightedDigraph<Node, Edge> createGraph(ScramState state) {
        Map<Node, Set<Edge>> vertices = new HashMap<>();
        Set<Edge> edges = new HashSet<>();
        for (Node node : state.allNodes()) {
            vertices.putIfAbsent(node, new HashSet<>());
            for (Node neighbor : getNeighborsOfNode(node, state)) {
                Edge edge = new SimpleEdge(node, neighbor);
                vertices.get(node).add(edge);
                edges.add(edge);
            }
        }
        return new SimpleWeightedDiagraph(vertices, edges);
    }

    private List<Node> getPathToExit(ShortestPaths<Node, Edge> shortestPaths, Node exit) {
        List<Node> path = new LinkedList<>();
        Node current = exit;


    }

    private void followPath(ScramState state, List<Node> path) {
        for (Node node : path) {
            if (state.stepsToGo() == 0) {
                break;
            }
            state.moveTo(node);
        }
    }

    private Collection<Node> getNeighborsOfNode(Node node, ScramState state) {
        Collection<Node> neighbors = new HashSet<>();
        for (Node potentialNeighbor : state.allNodes()) {
            if (isNeighbor(node, potentialNeighbor)) {
                neighbors.add(potentialNeighbor);
            }
        }
        return neighbors;
    }

    private boolean isNeighbor(Node node1, Node node2) {
        for (Node neighbor : node1.getNeighbors()) {
            if (neighbor.equals(node2)) {
                return true;
            }
        }
        for (Node neighbor : node2.getNeighbors()) {
            if (neighbor.equals(node1)) {
                return true;
            }
        }
        return false;
    }

    class SimpleEdge implements Edge {

        private final Node source;
        private final Node destination;

        public SimpleEdge(Node source, Node destination) {
            this.source = source;
            this.destination = destination;
        }
    }

    class SimpleWeightedDiagraph implements WeightedDigraph<Node, Edge> {

        private final Map<Node, Set<Edge>> vertices;
        private final Set<Edge> edges;

        public SimpleWeightedDiagraph(Map<Node, Set<Edge>> vertices, Set<Edge> edges) {
            this.vertices = vertices;
            this.edges = edges;
        }

        @Override
        public Iterable<Edge> outgoingEdges(Node vertex) {
            return null;
        }

        @Override
        public Node source(Edge edge) {
            return null;
        }

        @Override
        public Node dest(Edge edge) {
            return null;
        }

        @Override
        public double weight(Edge edge) {
            return 0;
        }
    }

//    private List<Node> findShortestPathToExit(ScramState state){
//        return path;
//    }
//
//    private void followPath(ScramState state, List<Node> path){
//        for (Node node: path){
//            if (state.stepsToGo() == 0){
//                break;
//            }
//            state.moveTo(node);
//        }
//    }

}
