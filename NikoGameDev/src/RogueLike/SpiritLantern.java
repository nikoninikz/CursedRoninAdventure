package RogueLike;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * A cursed lantern. Interact to light it or put it out.
 * (Later it can become a checkpoint or a Ghost Ronin marker.)
 */
public class SpiritLantern extends Interactable {

    private BufferedImage[] frames;   // [0] = unlit, [1] = lit
    private boolean lit = false;
    private double flickerTimer = 0;

    public SpiritLantern(double x, double y, BufferedImage sheet) {
        super(x, y, 64, 64, "Light The Lantern");
        frames = AssetManager.sliceRow(sheet, 64, 0, 2);
        sprite = frames[0];
    }

    @Override
    public void update(double dt) {
        if (lit) {
            flickerTimer += dt;   // drives the glow pulse
        }
    }

    @Override
    public String interact() {
        lit = !lit;
        sprite = lit ? frames[1] : frames[0];
        if (lit) {
            return "The Spirit Lantern Flares To Life.";
        }
        return "The Flame Sinks Back Into The Dark.";
    }

    @Override
    public void render(Graphics2D g2) {
        if (lit) {
            // A soft glow made of three see-through circles.
            double pulse = Math.sin(flickerTimer * 6);
            int[] sizes = { 260, 190, 120 };
            for (int i = 0; i < sizes.length; i++) {
                int alpha = (int) (28 + i * 12 + pulse * 8);
                g2.setColor(new Color(90, 240, 230, alpha));
                int cx = (int) (x + width / 2.0 - sizes[i] / 2.0);
                int cy = (int) (y + height / 2.0 - sizes[i] / 2.0);
                g2.fillOval(cx, cy, sizes[i], sizes[i]);
            }
        }
        super.render(g2);   // draw the lantern sprite
    }
}