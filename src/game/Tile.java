package game;

/** A tile on the game board. */
public class Tile {

    /**
     * An enum representing the different types of Tiles that <br> may appear
     * in a sewer system.
     *
     * @author eperdew
     */
    public enum TileType {
        FLOOR, RING, ENTRANCE, WALL
    }

    /**
     * The row and column position of the GameNode
     */
    private final int row, col;

    /**
     * Value of coins on this Node
     */
    private final int coinValue;

    /**
     * The Type of Tile this Node has
     */
    private TileType tileType;

    /**
     * True iff the coins on this Tile have been piceked up
     */
    private boolean coinsPickedUp;

    /**
     * Constructor: an instance with row r, column c, coin-value cv, and Type t.
     */
    public Tile(int r, int c, int cv, TileType t) {
        row = r;
        col = c;
        coinValue = cv;
        tileType = t;
        coinsPickedUp = false;
    }

    /**
     * Return the value of coins on this Tile.
     */
    public int coins() {
        return coinsPickedUp ? 0 : coinValue;
    }

    /**
     * Return the original amount of coins on this tile.
     */
    public int originalCoinValue() {
        return coinValue;
    }

    /**
     * Return the row of this Tile.
     */
    public int row() {
        return row;
    }

    /**
     * Return the column of this Tile.
     */
    public int column() {
        return col;
    }

    /**
     * Return the TileType of this Tile.
     */
    public TileType type() {
        return tileType;
    }

    /**
     * Set the TileType of this Tile to t.
     */
    void setType(TileType t) {
        tileType = t;
    }

    /**
     * Set the value of coins on this Node to 0 and return the amount "taken"
     */
    public int takeCoins() {
        int result = coins();
        coinsPickedUp = true;
        return result;
    }

    public String toString() {
        return "(row: " + row + ", col:" + col +")";
    }
}
