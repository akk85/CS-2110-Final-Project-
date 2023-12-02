package graph;

/**
 * This is a constraint interface for a directed graph of type G, with
 * vertices of type V and edges of type E.
 * For use in implementing generic graph algorithms.
 * @param <V> the type of vertices
 * @param <E> the type of edges
 */
public interface DirectedGraph<V, E> {
    /** All outgoing edges from a vertex in the graph */
    Iterable<E> outgoingEdges(V vertex);
    /** The source vertex for an edge in the graph. */
    V source(E edge);
    /** The destination vertex for an edge in the graph. */
    V dest(E edge);
}
