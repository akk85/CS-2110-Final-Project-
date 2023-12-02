package game;

import java.util.Objects;

/** Status of a graph node */
public final class NodeStatus implements Comparable<NodeStatus> {
	/** This node's id */
	private final long id;

	/** distance to ring */
	private final int distance;

	/** Constructor: an instance with id nodeId and distance dist to the ring */
	/* package */ NodeStatus(long nodeId, int dist) {
		id= nodeId;
		distance= dist;
	}

	/** Return the Id of the Node that corresponds to this NodeStatus. */
	public long getId() {
		return id;
	}

	/** Return the distance to the ring from the Node that corresponds br>to this NodeStatus. */
	public int getDistanceToRing() {
		return distance;
	}

	/** If the distances of this and other are equal, return neg, 0 or pos
	 * depending on whether this id is less than, equal to, or greater than other's id.
	 * Otherwise, return neg, or pos number depending on whether this's
	 * distance is less than or greater than other's distance. */
	@Override
	public int compareTo(NodeStatus other) {
		if (distance == other.distance) return Long.compare(id, other.id);
		return distance - other.distance;
	}

	/** Return true iff ob is a NodeStatus with the same id as this one.
	 * We don't have to be concerned with ob being a subclass of NodeStatus
	 * because this class is declared final.
     */
	@Override
	public boolean equals(Object ob) {
		if (ob == this) return true;
		if (!(ob instanceof NodeStatus)) return false;
		return id == ((NodeStatus) ob).id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
