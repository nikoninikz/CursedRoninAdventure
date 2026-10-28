package RogueLike;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Entity is the base class for everything that exists in the game world.
 */
public abstract class Entity {

    protected double x;
    protected double y;
    protected int width;
    protected int height;
    protected double speed;          // pixels per second
    protected BufferedImage sprite;

    // How many pixels the sprite is drawn ABOVE its real position (jumping).
    protected double drawOffsetY = 0;

    // NEW: the hitbox is the part of the entity that "counts" for collisions.
    // The offsets are measured from the sprite's top-left corner.
    protected int hitboxOffsetX;
    protected int hitboxOffsetY;
    protected int hitboxWidth;
    protected int hitboxHeight;

    public Entity(double x, double y, int width, int height,
                  double speed, BufferedImage sprite) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.sprite = sprite;

        // By default the hitbox covers the whole sprite.
        // Subclasses (like Player) can make it smaller.
        this.hitboxOffsetX = 0;
        this.hitboxOffsetY = 0;
        this.hitboxWidth = width;
        this.hitboxHeight = height;
    }

    public abstract void update(double dt);

    public void render(Graphics2D g2) {
        int drawY = (int) (y - drawOffsetY);
        if (sprite != null) {
            g2.drawImage(sprite, (int) x, drawY, width, height, null);
        } else {
            g2.setColor(Color.MAGENTA);
            g2.fillRect((int) x, drawY, width, height);
        }
    }

    // NEW: the hitbox as a rectangle in world coordinates.
    public Rectangle getHitbox() {
        return new Rectangle((int) (x + hitboxOffsetX), (int) (y + hitboxOffsetY),
                             hitboxWidth, hitboxHeight);
    }

    public double getHitboxCenterX() { return x + hitboxOffsetX + hitboxWidth / 2.0; }
    public double getHitboxCenterY() { return y + hitboxOffsetY + hitboxHeight / 2.0; }

    public double getX()   { return x; }
    public double getY()   { return y; }
    public int getWidth()  { return width; }
    public int getHeight() { return height; }
}