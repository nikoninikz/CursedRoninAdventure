package RogueLike;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * A wooden practice dummy. It doesn't move or fight back.
 * It respawns a few seconds after "dying" so you can test combat
 * over and over without restarting the game.
 */
public class TrainingDummy extends Entity implements Damageable {

    private static final int MAX_HEALTH = 100;
    private static final double RESPAWN_TIME = 3.0;

    private int health = MAX_HEALTH;
    private double flashTimer = 0;      // turns red briefly when hit
    private double respawnTimer = 0;    // counts down while "dead"

    public TrainingDummy(double x, double y) {
        super(x, y, 64, 96, 0, AssetManager.loadImage("res/images/dummy.png"));
        hitboxWidth = 40;
        hitboxHeight = 60;
        hitboxOffsetX = (width - hitboxWidth) / 2;
        hitboxOffsetY = 20;
    }

    @Override
    public void update(double dt) {
        if (flashTimer > 0) {
            flashTimer -= dt;
        }
        if (!isAlive()) {
            respawnTimer -= dt;
            if (respawnTimer <= 0) {
                health = MAX_HEALTH;   // back to full health
            }
        }
    }

    @Override
    public void takeDamage(int amount) {
        if (!isAlive()) {
            return;   // can't hurt it while it's "down"
        }
        health -= amount;
        flashTimer = 0.15;
        if (health <= 0) {
            health = 0;
            respawnTimer = RESPAWN_TIME;
        }
    }

    @Override
    public boolean isAlive() {
        return health > 0;
    }

    @Override
    public void render(Graphics2D g2) {
        if (!isAlive()) {
            return;   // don't draw it while "down"
        }

        super.render(g2);

        if (flashTimer > 0) {
            g2.setColor(new Color(255, 60, 60, 120));
            g2.fillRect((int) x, (int) y, width, height);
        }

        // Health bar above its head.
        int barWidth = 50;
        int barX = (int) (x + width / 2.0 - barWidth / 2.0);
        int barY = (int) y - 10;
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(barX, barY, barWidth, 6);
        g2.setColor(Color.RED);
        g2.fillRect(barX, barY, (int) (barWidth * (health / (double) MAX_HEALTH)), 6);
    }
}