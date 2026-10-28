package RogueLike;

/**
 * One cell in the dungeon's ROOM grid (not the tile grid).
 * A Room knows which tiles belong to it, based on its grid position.
 */
public class Room {

    public final int gridCol;
    public final int gridRow;

    public Room(int gridCol, int gridRow) {
        this.gridCol = gridCol;
        this.gridRow = gridRow;
    }

    public int getTileColStart() { return gridCol * World.ROOM_W; }
    public int getTileRowStart() { return gridRow * World.ROOM_H; }
    public int getTileColEnd()   { return getTileColStart() + World.ROOM_W - 1; }
    public int getTileRowEnd()   { return getTileRowStart() + World.ROOM_H - 1; }

    // The middle of the room, in world PIXELS. Handy for spawning things.
    public double getCenterX() {
        return (getTileColStart() + World.ROOM_W / 2.0) * World.TILE_SIZE;
    }
    public double getCenterY() {
        return (getTileRowStart() + World.ROOM_H / 2.0) * World.TILE_SIZE;
    }

    // So we can check "is this the same room?" (used to skip the start room).
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Room)) { return false; }
        Room other = (Room) o;
        return other.gridCol == gridCol && other.gridRow == gridRow;
    }

    @Override
    public int hashCode() {
        return gridCol * 31 + gridRow;
    }
}