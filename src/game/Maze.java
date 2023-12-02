package game;

import graph.WeightedDigraph;
import java.util.Set;

/** A Maze is a weighted directed graph of game.Node and game.Edge objects */
public class Maze implements WeightedDigraph<Node, Edge> {
    Set<Node> graph;

    /** Creates: a maze from a set of Nodes. */
    public Maze(Set<Node> graph) { this.graph = graph; }

    // The following are all standard graph operations specified
    // in WeightedDigraph.

    public Iterable<Edge> outgoingEdges(Node vertex) { return vertex.getExits(); }
    public Node source(Edge edge) { return edge.source(); }
    public Node dest(Edge edge) { return edge.destination(); }
    public double weight(Edge edge) { return edge.length(); }
}