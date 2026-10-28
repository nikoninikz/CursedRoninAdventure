package RogueLike;

import java.awt.image.BufferedImage;

/**
 * A chest holding one item. Opens once, then stays open and empty.
 */
public class Chest extends Interactable {

    private BufferedImage[] frames;   // [0] closed, [1] open
    private boolean opened = false;
    private String itemName;

    public Chest(double x, double y, BufferedImage sheet, String itemName) {
        super(x, y, 64, 64, "Open chest");
        this.itemName = itemName;
        frames = AssetManager.sliceRow(sheet, 64, 0, 2);
        sprite = frames[0];
    }

    @Override
    public void update(double dt) {
        // Chests don't need to do anything every frame.
    }

    @Override
    public String interact() {
        opened = true;
        sprite = frames[1];
        return "Found: " + itemName;
    }

    @Override
    public boolean isAvailable() {
        return !opened;   // once opened, it stops showing a prompt
    }
}