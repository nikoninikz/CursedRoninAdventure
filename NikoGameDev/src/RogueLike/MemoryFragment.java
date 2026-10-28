package RogueLike;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * A glowing shard holding one piece of the Ronin's lost memory.
 * Picking it up permanently unlocks its text in the MemoryJournal.
 */
public class MemoryFragment extends Interactable {

    private int fragmentId;
    private MemoryJournal journal;
    private double pulseTimer = 0;
    private boolean takenThisInstance = false;

    public MemoryFragment(double x, double y, BufferedImage sprite,
                          int fragmentId, MemoryJournal journal) {
        super(x, y, 32, 32, "Recall memory");
        this.fragmentId = fragmentId;
        this.journal = journal;
        this.sprite = sprite;
        this.interactRange = 70;
    }

    @Override
    public void update(double dt) {
        pulseTimer += dt;
    }

    @Override
    public String interact() {
        takenThisInstance = true;
        return "Memory recovered:\n" + journal.collect(fragmentId);
    }

    @Override
    public boolean isAvailable() {
        return !takenThisInstance;
    }

    @Override
    public void render(Graphics2D g2) {
        if (takenThisInstance) {
            return;   // NEW: gone once collected
        }

        double bob = Math.sin(pulseTimer * 3) * 4;
        int drawY = (int) (y + bob);

        double glowSize = 30 + Math.sin(pulseTimer * 4) * 6;
        g2.setColor(new Color(170, 120, 255, 70));
        g2.fillOval((int) (x + width / 2.0 - glowSize / 2.0),
                    (int) (drawY + height / 2.0 - glowSize / 2.0),
                    (int) glowSize, (int) glowSize);

        if (sprite != null) {
            g2.drawImage(sprite, (int) x, drawY, width, height, null);
        }
    }
}