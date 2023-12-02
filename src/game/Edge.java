package game;

import java.util.Map;

/**
 * An Edge represents an immutable directed, weighted edge.
 *
 * @author eperdew
 */
public class Edge {

    /**
     * The Nodes this edge is coming from and going to
     */
    private final Node src, dest;

    /**
     * The length of this edge
     */
    public final int length;

    /**
     * Constructor: an edge from src to dest with length length.
     */
    public Edge(Node src, Node dest, int length) {
        this.src = src;
        this.dest = dest;
        this.length = length;
    }

    /**
     * Constructor: an edge that is isomorphic to iso.
     */
    public Edge(Edge e, Map<Node, Node> iso) {
        src = iso.get(e.src);
        dest = iso.get(e.dest);
        length = e.length;
    }

    /**
     * Return the Node on this Edge that is not equal to n. Throw an
     * IllegalArgumentException if n is not in this Edge.
     */
    public Node getOther(Node n) {
	    if (src == n) {
		    return dest;
	    }
	    if (dest == n) {
		    return src;
	    }
        throw new IllegalArgumentException("getOther: Edge must contain provided node");

    }

    /**
     * Return the length of this {@code Edge}
     */
    public int length() {
        return length;
    }

    /**
     * Return the source of this edge.
     */
    public Node source() {
        return src;
    }

    /**
     * Return destination of edge
     */
    public Node destination() {
        return dest;
    }
}
