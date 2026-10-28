package RogueLike;

/**
 * CollisionHandler moves entities while respecting solid tiles.
 * Enemies will use the same handler later.
 *
 * Note: it can read Entity's "protected" fields because both classes
 * are in the same package (RogueLike).
 */
public class CollisionHandler {

    private World world;

    public CollisionHandler(World world) {
        this.world = world;
    }

    /**
     * Moves the entity by (moveX, moveY), one axis at a time,
     * so it slides along walls instead of sticking to them.
     */
    public void moveEntity(Entity e, double moveX, double moveY) {
        e.x = resolveX(e, moveX);
        e.y = resolveY(e, moveY);
    }

    private double resolveX(Entity e, double moveX) {
        if (moveX == 0) {
            return e.x;
        }

        double newX = e.x + moveX;
        double hitX = newX + e.hitboxOffsetX;           // hitbox left edge
        double hitY = e.y + e.hitboxOffsetY;            // hitbox top edge

        if (!collidesWithWalls(hitX, hitY, e.hitboxWidth, e.hitboxHeight)) {
            return newX;    // the way is clear
        }

        // Blocked: snap the hitbox flush against the wall tile.
        if (moveX > 0) {
            int wallCol = (int) Math.floor((hitX + e.hitboxWidth - 1) / World.TILE_SIZE);
            return wallCol * World.TILE_SIZE - e.hitboxOffsetX - e.hitboxWidth;
        } else {
            int wallCol = (int) Math.floor(hitX / World.TILE_SIZE);
            return (wallCol + 1) * World.TILE_SIZE - e.hitboxOffsetX;
        }
    }

    private double resolveY(Entity e, double moveY) {
        if (moveY == 0) {
            return e.y;
        }

        double newY = e.y + moveY;
        double hitX = e.x + e.hitboxOffsetX;
        double hitY = newY + e.hitboxOffsetY;

        if (!collidesWithWalls(hitX, hitY, e.hitboxWidth, e.hitboxHeight)) {
            return newY;
        }

        if (moveY > 0) {
            int wallRow = (int) Math.floor((hitY + e.hitboxHeight - 1) / World.TILE_SIZE);
            return wallRow * World.TILE_SIZE - e.hitboxOffsetY - e.hitboxHeight;
        } else {
            int wallRow = (int) Math.floor(hitY / World.TILE_SIZE);
            return (wallRow + 1) * World.TILE_SIZE - e.hitboxOffsetY;
        }
    }

    /**
     * Does this rectangle (in world pixels) overlap any solid tile?
     */
    public boolean collidesWithWalls(double x, double y, int w, int h) {
        // Which tiles does the rectangle touch?
        int leftCol   = (int) Math.floor(x / World.TILE_SIZE);
        int rightCol  = (int) Math.floor((x + w - 1) / World.TILE_SIZE);
        int topRow    = (int) Math.floor(y / World.TILE_SIZE);
        int bottomRow = (int) Math.floor((y + h - 1) / World.TILE_SIZE);

        for (int row = topRow; row <= bottomRow; row++) {
            for (int col = leftCol; col <= rightCol; col++) {
                if (world.isSolid(col, row)) {
                    return true;
                }
            }
        }
        return false;
    }
}