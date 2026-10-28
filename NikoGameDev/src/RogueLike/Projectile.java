package RogueLike;

import java.awt.Color;
import java.awt.Graphics2D;

public class Projectile extends Entity {

    private double vx;
    private double vy;
    private int damage;
    private double lifeTimer = 3.0;   // vanishes on its own after 3 seconds
    private boolean expired = false;

    public Projectile(double centerX, double centerY, double vx, double vy, int damage) {
        super(centerX - 6, centerY - 6, 12, 12, 0, null);
        this.vx = vx;
        this.vy = vy;
        this.damage = damage;
        hitboxWidth = 12;
        hitboxHeight = 12;
    }

    @Override
    public void update(double dt) {
        x += vx * dt;
        y += vy * dt;
        lifeTimer -= dt;
        if (lifeTimer <= 0) {
            expired = true;
        }
    }

    public int getDamage()      { return damage; }
    public boolean isExpired()  { return expired; }
    public void expire()        { expired = true; }

    @Override
    public void render(Graphics2D g2) {
        g2.setColor(new Color(140, 255, 120, 90));
        g2.fillOval((int) x - 4, (int) y - 4, width + 8, height + 8);   // soft glow
        g2.setColor(new Color(210, 255, 190, 255));
        g2.fillOval((int) x, (int) y, width, height);                  // bright core
    }
}