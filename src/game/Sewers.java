package game;

import graph.ShortestPaths;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A sewer through which a sewer diver can move: a grid of Tile objects
 * with a weighted graph of all non-floor tiles.
 * <p>
 * There is an entrance to the sewer system and a ring location
 * (which may also be the entrance).
 */
public class Sewers {

    public static final int MAX_EDGE_WEIGHT = 15;
    private static final double DENSITY = 0.6;
    private static final double COIN_PROBABILITY = 0.33;
    public static final int MAX_COIN_VALUE = 1000;
    public static final int TASTY_VALUE = 5000;

    /**
     * An enum representing a grid direction.
     */
    public enum Direction {
        NORTH(-1, 0), EAST(0, 1), SOUTH(1, 0), WEST(0, -1);

        private final Point dir;

        /**
         * Constructor: an instance with direction (row, col).
         */
        Direction(int row, int col) {
            dir = new Point(row, col);
        }

        /**
         * Return the direction of this instance
         */
        public Point direction() {
            return dir;
        }
    }

    /**
     * An instance represents a point on the grid.
     */
    private static final class Point {

        /**
         * The row and column of the point
         */
        private final int row, col;

        /**
         * Constructor: an instance for (r, c).
         */
        private Point(int r, int c) {
            row = r;
            col = c;
        }

        /**
         * Return a new point that is this point with p added to it.
         */
        public Point add(Point p) {
            return new Point(row + p.row, col + p.col);
        }

        /**
         * Return true iff ob is a Point and with same row and col of this one.
         */
        @Override
        public boolean equals(Object ob) {
		if (!(ob instanceof Point)) {
			return false;
		}
            Point p = (Point) ob;
            return p.row == row && p.col == col;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }
    }

    /**
     * Number of rows and columns
     */
    private final int rows, cols;

    /**
     * The nodes of the graph
     */
    private final Set<Node> graph;

    /**
     * The entrance and the node with the ring
     */
    private final Node entrance, ring;
    private final Maze maze;

    /**
     * Grid of tiles
     */
    private final Node[][] tiles;

    /**
     * Return a new random sewer system with r rows, c columns, and no
     * coins, all edges have weight 1, and there is a ring a reasonable
     * distance from the exit. rand is the source of randomness for the
     * sewer-system generation.
     */
    public static Sewers digExploreSewer(int r, int c, Random rand) {
        int minRingDist = minRingDistance(r, c);

        Sewers sewers = new Sewers(r, c, rand, () -> 1, () -> 0, Tile.TileType.RING);
        while (sewers.minPathLengthToRing(sewers.entrance()) < minRingDist) {
            sewers = new Sewers(r, c, rand, () -> 1, () -> 0, Tile.TileType.RING);
        }
        return sewers;
    }

    /**
     * Return the minimum allowable path distance from the entrance to
     * the ring. The graph has r rows and c columns.
     */
    private static int minRingDistance(int r, int c) {
        return (r + c) / 2;
    }

    /**
     * Return a new random sewer system with r rows, c columns, and
     * random coins and edge weights. It is guaranteed that
     * (currentRow, {currentCol) will be an open floor cell. rand is
     * the source of randomness to use for the sewer-system generation.
     */
    public static Sewers digGetOutSewer(int r, int c, int currentRow, int currentCol,
            Random rand) {
        Supplier<Integer> edgeWeightGen = () -> rand.nextInt(MAX_EDGE_WEIGHT) + 1;
        Supplier<Integer> coinGen = () -> Sewers.randomCoinValue(rand);
        Sewers potentialCavern = new Sewers(r, c, rand, edgeWeightGen, coinGen,
                Tile.TileType.ENTRANCE);
        while (potentialCavern.tileAt(currentRow, currentCol).type() != Tile.TileType.FLOOR) {
            potentialCavern = new Sewers(r, c, rand, edgeWeightGen, coinGen,
                    Tile.TileType.ENTRANCE);
        }
        return potentialCavern;
    }

    /**
     * Return a randomly determined gold value (to place on a tile).
     * Use rand as the source of randomness.
     */
    private static int randomCoinValue(Random rand) {
        if (rand.nextDouble() > COIN_PROBABILITY) {
            return 0;
        }

        int val = rand.nextInt(MAX_COIN_VALUE) + 1;
        if (val == MAX_COIN_VALUE) {
            val = TASTY_VALUE;
        }
        return val;
    }

    /**
     * Constructor: a new sewer system of size (rws, cls).
     * Randomness rand is used to determine which grid tiles are
     * open. This uses edgeWeightGenerator and coinGenerator
     * to generate edge weights and coin values.
     * Requires: targetType is either Tile.TileType.RING or
     * Tile.TileType.ENTRANCE.
     */
    private Sewers(int rws, int cls, Random rand,
            Supplier<Integer> edgeWeightGenerator,
            Supplier<Integer> coinGenerator,
            Tile.TileType targetType) {
        rows = rws;
        cols = cls;

        graph = generateGraph(rand, targetType, coinGenerator);
        maze = new Maze(graph);
        entrance = graph.stream().filter((n) -> n.getTile().type() == Tile.TileType.ENTRANCE)
                .findAny().get();
        ring = graph.stream().filter((n) -> n.getTile().type() == targetType).findAny().get();

        // Set tiles for the floor and then add walls wherever floor is missing.
        tiles = new Node[rows][cols];
        for (Node node : graph) {
            Tile t = node.getTile();
            tiles[t.row()][t.column()] = node;
        }
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (tiles[i][j] == null) {
                    tiles[i][j] = new Node(new Tile(i, j, 0, Tile.TileType.WALL), cols);
                }
            }
        }
        createEdges(tiles, edgeWeightGenerator);

    }

    /**
     * Constructor; a graph constructed from givenGraph and tiles
     * givenTiles, with the ring placed at trgt.
     * Requires:
     *  1. givenGraph and givenTiles represent the same graph (i.e.
     *     givenGraph contains all non-floor nodes in givenTiles and
     *     edges are along the grid).
     *  2. trgt is a node in givenGraph.
     */
    private Sewers(Set<Node> givenGraph, Node[][] givenTiles, Node trgt) {
        tiles = givenTiles;
        rows = tiles.length;
        cols = tiles[0].length;

        graph = Collections.unmodifiableSet(givenGraph);
        maze = new Maze(graph);
        entrance = graph.stream().filter((n) -> n.getTile().type() == Tile.TileType.ENTRANCE)
                .findAny().get();
        ring = trgt;
    }

    /**
     * Given tiles without edges and a supplier to return edge weights,
     * add edges to the nodes between adjacent non-wall tiles.
     * Requires: all elements of tiles are non-null.
     */
    private void createEdges(Node[][] tiles, Supplier<Integer> edgeWeightGenerator) {
        for (int i = 0; i < tiles.length - 1; i++) {
            for (int j = 0; j < tiles[i].length - 1; j++) {
                Node node = tiles[i][j];
                if (node.getTile().type() == Tile.TileType.WALL) {
                    continue;
                }

                final Point p = new Point(i, j);
                Stream.of(Direction.SOUTH, Direction.EAST)
                        .map(Direction::direction)
                        .map(p::add)
                        .map((q) -> tiles[q.row][q.col])
                        .filter((m) -> m.getTile().type() != Tile.TileType.WALL)
                        .forEach((m) -> {
                            int weight = edgeWeightGenerator.get();
                            node.addEdge(new Edge(node, m, weight));
                            m.addEdge(new Edge(m, node, weight));
                        });
            }
        }
    }

    /**
     * Return true iff p is on the grid.
     */
    private boolean isValid(Point p) {
        return 0 < p.row && p.row < rows - 1 &&
                0 < p.col && p.col < cols - 1;
    }

    /**
     * Generate a new random graph that fits within the grid and return
     * the set of nodes.
     */
    private Set<Node> generateGraph(Random rand,
            Tile.TileType targetType,
            Supplier<Integer> coinGenerator) {
        List<Node> nodes = new ArrayList<>();

        Set<Point> pointsSeen = new HashSet<>();
        Set<Point> openPoints = new HashSet<>();
        Queue<Node> frontier = new ArrayDeque<>();

        Point entrancePoint = getEntrancePoint(rand);
        Node entrance = new Node(new Tile(entrancePoint.row,
                entrancePoint.col, 0, Tile.TileType.ENTRANCE), cols);
        nodes.add(entrance);

        pointsSeen.add(entrancePoint);
        openPoints.add(entrancePoint);
        frontier.add(entrance);
        while (!frontier.isEmpty()) {
            Node node = frontier.remove();
            Point p = new Point(node.getTile().row(), node.getTile().column());

            // We want to make sure there's a way out if we can get one.
            // This will prevent stupid degenerate graphs.
            int existingExits = 0;
            List<Point> newExits = new ArrayList<>();
            for (Direction dir : Direction.values()) {
                Point newPt = dir.direction().add(p);
                if (isValid(newPt)) {
                    if (openPoints.contains(newPt)) {
                        existingExits++;
                    } else if (pointsSeen.add(newPt)) {
                        newExits.add(newPt);
                    }
                }
            }

            int nExits = newExits.size();
            if (nExits > 0) {
                double modifiedDensity;
                Point forcedExit;
                // Modify the density function so that the expected
                // number of open exits is the same even though we're
                // forcing something to be open.
                if (existingExits < 2) {
                    modifiedDensity = nExits == 1 ? 0.0 : (nExits * DENSITY - 1) / (nExits - 1);
                    forcedExit = newExits.get(rand.nextInt(newExits.size()));
                } else {
                    modifiedDensity = DENSITY;
                    forcedExit = null;
                }
                newExits.stream()
                        .filter((q) -> q.equals(forcedExit) || rand.nextDouble() < modifiedDensity)
                        .peek(openPoints::add)
                        .map((q) -> new Node(
                                new Tile(q.row, q.col, coinGenerator.get(), Tile.TileType.FLOOR),
                                cols))
                        .peek(frontier::add)
                        .forEach(nodes::add);
            }
        }

        if (targetType != Tile.TileType.ENTRANCE) {
            // Grab a random tile that's not the entrance and make it the ring.
            int targetIdx = rand.nextInt(nodes.size() - 1) + 1;
            nodes.get(targetIdx).getTile().setType(targetType);
        }

        return Collections.unmodifiableSet(new HashSet<>(nodes));
    }

    /**
     * Return a randomly chosen entrance to the sewer system (the only
     * non-wall tile along an edge of the grid), using rand.
     */
    private Point getEntrancePoint(Random rand) {
        switch (rand.nextInt(4)) {
            case 0: // North wall
                return new Point(rand.nextInt(rows - 2) + 1, 0);
            case 1: // South wall
                return new Point(rand.nextInt(rows - 2) + 1, cols - 1);
            case 2: // West wall
                return new Point(0, rand.nextInt(cols - 2) + 1);
            case 3: // East wall
                return new Point(rows - 1, rand.nextInt(cols - 2) + 1);
            default:
                throw new IllegalStateException("Unexpected random value!");
        }
    }

    /**
     * Return the number of open floor tiles in this sewer system (this
     * is the size of the graph).
     */
    public int numOpenTiles() {
        return graph.size();
    }

    /**
     * Return the number of rows in the grid.
     */
    public int rowCount() {
        return rows;
    }

    /**
     * Return the number of columns in the grid.
     */
    public int columnCount() {
        return cols;
    }

    /**
     * Return the set of all nodes in the graph. This is an umodifiable view of the graph.
     */
    public Set<Node> graph() {
        return graph;
    }

    /**
     * Return the node corresponding to the entrance to the sewer system.
     */
    public Node entrance() {
        return entrance;
    }

    /**
     * Return the ring node in this sewer system.
     */
    public Node ring() {
        return ring;
    }

    /**
     * Return the Tile information for tile (r, c).
     * Requires: (r, c) must be in the grid.
     */
    public Tile tileAt(int r, int c) {
        return tiles[r][c].getTile();
    }

    /**
     * Return the node at the given (r, c).
     * Requires: (r, c) must be in the grid.
     */
    public Node nodeAt(int r, int c) {
        return tiles[r][c];
    }

    // NOTE: if you are having trouble getting Dijkstra's algorithm to work well
    // enough to let the game run, you can change USE_MANHATTAN_DISTANCE to true
    // to allow progress on other tasks. However, it should be changed back to false
    // eventually for you to be successful.
    private static final boolean USE_MANHATTAN_DISTANCE = false;

    /**
     * Returns: the shortest distance from node start to ring node -- unless
     * MANHATTAN_DISTANCE is true, in which case it just returns the Manhattan
     * distance between the nodes.
     * Requires: start must be a node of the graph.
     */
    int minPathLengthToRing(Node start) {
        if (USE_MANHATTAN_DISTANCE) {
            return (int) manhattanDistanceToRing(start);
        }
        ShortestPaths<Node, Edge> dijkstra = new ShortestPaths<>(maze);
        dijkstra.singleSourceDistances(start);
        return (int) dijkstra.getDistance(ring);
    }

    /** The Manhattan distance from start to the ring. */
    int manhattanDistanceToRing(Node start) {
        return Math.abs(start.getTile().row() - ring.getTile().row())
             + Math.abs(start.getTile().column() - ring.getTile().column());
    }

    /**
     * Serialize this sewer system to a list of strings that can be
     * written out to a file. The list of strings can be converted
     * back into a Sewers using deserialize().
     */
    public List<String> serialize() {
        List<String> nodes = new ArrayList<>();
        nodes.add(rows + ":" + cols + ",trgt:" + ring.getId());
        for (Node n : graph) {
            Tile t = n.getTile();
            String nodeStr = n.getId() + "," + t.row() + "," + t.column() +
                    "," + t.coins() + "," + t.type().name();

            String edges = n.getExits().stream()
                    .map((e) -> e.getOther(n).getId() + "-" + e.length())
                    .collect(Collectors.joining(","));
            nodes.add(nodeStr + "=" + edges);
        }
        return nodes;
    }

    /**
     * Convert nodeStrList, which was output by serialize(), back into
     * a Sewers.
     * Requires: The list of strings is of the format output by
     * serialize().
     */
    public static Sewers deserialize(List<String> nodeStrList) {
        String extraInfo = nodeStrList.get(0);
        String[] infoParts = extraInfo.split(",");
        String[] dimensions = infoParts[0].split(":");
        int rows = Integer.parseInt(dimensions[0]);
        int cols = Integer.parseInt(dimensions[1]);
        long targetId = Long.parseLong(infoParts[1].split(":")[1]);

        Map<Long, Node> idToNode = new HashMap<>();
        for (String nodeStr : nodeStrList) {
            if (nodeStr.equals(extraInfo)) {
                continue;
            }

            String nodeInfo = nodeStr.substring(0, nodeStr.indexOf("="));
            String[] splitInfo = nodeInfo.split(",");

            long nodeId = Long.parseLong(splitInfo[0]);
            Node n = new Node(nodeId,
                    new Tile(Integer.parseInt(splitInfo[1]),
                            Integer.parseInt(splitInfo[2]),
                            Integer.parseInt(splitInfo[3]),
                            Tile.TileType.valueOf(splitInfo[4])));
            idToNode.put(nodeId, n);
        }

        Node[][] tiles = new Node[rows][cols];
        for (String nodeStr : nodeStrList) {
            // The first line is not a node, it's metadata, so skip it.
            if (nodeStr.equals(extraInfo)) {
                continue;
            }

            String[] nodeAndEdgeInfo = nodeStr.split("=");
            long nodeId = Long.parseLong(nodeAndEdgeInfo[0].split(",")[0]);

            Node n = idToNode.get(nodeId);
            tiles[n.getTile().row()][n.getTile().column()] = n;
            for (String edgeStr : nodeAndEdgeInfo[1].split(",")) {
                String[] idAndWeight = edgeStr.split("-");
                long otherId = Long.parseLong(idAndWeight[0]);
                int weight = Integer.parseInt(idAndWeight[1]);
                n.addEdge(new Edge(n, idToNode.get(otherId), weight));
            }
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (tiles[i][j] == null) {
                    tiles[i][j] = new Node(new Tile(i, j, 0, Tile.TileType.WALL), cols);
                }
            }
        }
        return new Sewers(new HashSet<>(idToNode.values()), tiles, idToNode.get(targetId));
    }
}
