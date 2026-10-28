package RogueLike;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

public class BloodParticle {

    private double x;
    private double y;

    private double vx;
    private double vy;

    private double life;
    private double maxLife;

    private int size;

    private static final Random random = new Random();

    public BloodParticle(double x, double y) {

        this.x = x;
        this.y = y;

        // Random burst direction
        double angle = random.nextDouble() * Math.PI * 2;
        double speed = 40 + random.nextDouble() * 100;

        vx = Math.cos(angle) * speed;
        vy = Math.sin(angle) * speed;

        maxLife = 0.25 + random.nextDouble() * 0.20;
        life = maxLife;

        size = 2 + random.nextInt(3);
    }

    public void update(double dt) {

        x += vx * dt;
        y += vy * dt;

        // Slow down
        vx *= 0.92;
        vy *= 0.92;

        life -= dt;
    }

    public boolean isDead() {
        return life <= 0;
    }

    public void render(Graphics2D g2) {

        float alpha = (float) Math.max(0, life / maxLife);

        g2.setColor(
            new Color(
                180,
                20,
                20,
                (int) (255 * alpha)
            )
        );

        g2.fillRect(
            (int) x,
            (int) y,
            size,
            size
        );
    }
}