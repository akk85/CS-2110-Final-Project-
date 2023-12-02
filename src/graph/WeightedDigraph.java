package graph;

/** Constraint interface for a directed graph with edge weights.
 * @param <V> the type of vertices
 * @param <E> the type of edges
 */
public interface WeightedDigraph<V, E> extends DirectedGraph<V, E> {
    /** The weight of an edge */
    double weight(E edge);
}
