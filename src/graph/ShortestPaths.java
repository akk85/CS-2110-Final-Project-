package graph;

import datastructures.PQueue;
import datastructures.SlowPQueue;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This object computes and remembers shortest paths through a weighted, directed graph with
 * nonnegative weights. Once shortest paths are computed from a specified source vertex, it allows
 * querying the distance to arbitrary vertices and the best paths to arbitrary destination
 * vertices.
 * <p>
 * Types Vertex and Edge are parameters, so their operations are supplied by a model object supplied
 * to the constructor.
 */
public class ShortestPaths<Vertex, Edge> {

    /**
     * The model for treating types Vertex and Edge as forming a weighted directed graph.
     */
    private final WeightedDigraph<Vertex, Edge> graph;

    /**
     * The distance to each vertex from the source.
     */
    private Map<Vertex, Double> distances;

    /**
     * The incoming edge for the best path to each vertex from the source vertex.
     */
    private Map<Vertex, Edge> bestEdges;

    /**
     * Creates: a single-source shortest-path finder for a weighted graph.
     *
     * @param graph The model that supplies all graph operations.
     */
    public ShortestPaths(WeightedDigraph<Vertex, Edge> graph) {
        this.graph = graph;
    }

    /**
     * Effect: Computes the best paths from a given source vertex, which can then be queried using
     * bestPath().
     */
    public void singleSourceDistances(Vertex source) {
        // Implementation constraint: use Dijkstra's single-source shortest paths algorithm.
        PQueue<Vertex> frontier = new SlowPQueue<>();
        Set<Vertex> inQueue = new HashSet<>();
        distances = new HashMap<>();
        bestEdges = new HashMap<>();
        distances.put(source, 0.0);
        frontier.add(source, 0.0);
        inQueue.add(source);

        while (!frontier.isEmpty()) {
            Vertex current = frontier.extractMin();
            inQueue.remove(current);

            for (Edge e : graph.outgoingEdges(current)) {
                Vertex neighbor = graph.dest(e);
                double edgeWeight = graph.weight(e);
                double newDistance = distances.getOrDefault(current, Double.POSITIVE_INFINITY) + edgeWeight;

                if (newDistance < distances.getOrDefault(neighbor, Double.POSITIVE_INFINITY)) {
                    distances.put(neighbor, newDistance);
                    bestEdges.put(neighbor, e);

                    if (inQueue.contains(neighbor)) {
                        frontier.changePriority(neighbor, newDistance);
                    } else {
                        frontier.add(neighbor, newDistance);
                        inQueue.add(neighbor);
                    }
                }
            }
        }
    }

    /**
     * Returns: the distance from the source vertex to the given vertex. Requires: distances have
     * been computed from a source vertex, and vertex v is reachable from that vertex.
     */
    public double getDistance(Vertex v) {
        assert !distances.isEmpty() : "Must run singleSourceDistances() first";
        Double d = distances.get(v);
        assert d != null : "v not reachable from source";
        return d;
    }

    /**
     * Returns: the best path from the source vertex to a given target vertex. The path is
     * represented as a list of edges. Requires: singleSourceDistances() has already been used to
     * compute best paths, and vertex target is reachable from that source.
     */
    public List<Edge> bestPath(Vertex target) {
        assert !bestEdges.isEmpty() : "Must run singleSourceDistances() first";
        LinkedList<Edge> path = new LinkedList<>();
        Vertex v = target;
        while (true) {
            Edge e = bestEdges.get(v);
            if (e == null) {
                break; // must be the source vertex (assuming target is reachable)
            }
            path.addFirst(e);
            v = graph.source(e);
        }
        return path;
    }
}
