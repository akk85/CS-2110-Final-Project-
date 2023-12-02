package game;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * An instance is a node of the graph
 */
public class Node {

    /**
     * The unique numerical identifier of this Node
     */
    private final long id;

    /**
     * The edges leaving from this Node
     */
    private final Set<Edge> edges;

    /**
     * The neighbors of this Node
     */
    private final Set<Node> neighbors;

    private final Set<Edge> unmodifiableEdges;
    private final Set<Node> unmodifiableNeighbors;

    /**
     * Extra state that belongs to this node
     */
    private final Tile tile;

    /**
     * Constructor: a Node for tile t using t's row
     */
    Node(Tile t, int numCols) {
        this(t.row() * numCols + t.column(), t);
    }

    /**
     * Constructor: a node for tile t with id givenId.
     */
    Node(long givenId, Tile t) {
        id = givenId;
        edges = new LinkedHashSet<>();
        neighbors = new LinkedHashSet<>();

        unmodifiableEdges = Collections.unmodifiableSet(edges);
        unmodifiableNeighbors = Collections.unmodifiableSet(neighbors);

        tile = t;
    }

    /**
     * Add edge e to this node.
     */
    void addEdge(Edge e) {
        edges.add(e);
        neighbors.add(e.getOther(this));
    }

    /**
     * Return the unique Identifier of this Node.
     */
    public long getId() {
        return id;
    }

    /**
     * Return the Edge of this Node that connects to Node q. Throw an IllegalArgumentException
     * if edge doesn't exist
     */
    public Edge getEdge(Node q) {
        for (Edge e : edges) {
            if (e.destination().equals(q)) {
                return e;
            }
        }
        throw new IllegalArgumentException("getEdge: Node must be a neighbor of this Node");
    }

    /**
     * Return an unmodifiable view of the Edges leaving this Node.
     */
    public Set<Edge> getExits() {
        return unmodifiableEdges;
    }

    /**
     * Return an unmodifiable view of the Nodes neighboring this Node.
     */
    public Set<Node> getNeighbors() {
        return unmodifiableNeighbors;
    }

    /**
     * Return the Tile corresponding to this Node.
     */
    public Tile getTile() {
        return tile;
    }

    /**
     * Return true iff ob is a Node with the same id as this one.
     */
    @Override
    public boolean equals(Object ob) {
	    if (ob == this) {
		    return true;
	    }
	    if (!(ob instanceof Node)) {
		    return false;
	    }
        return id == ((Node) ob).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
