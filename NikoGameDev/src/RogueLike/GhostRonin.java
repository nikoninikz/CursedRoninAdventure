package RogueLike;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * A translucent echo of a previous run. It replays its recorded
 * position, facing, and attacks in a loop. It is NOT controlled by
 * the player and cannot be damaged yet — a simple first version, per
 * the roadmap's "start simple" instruction for this milestone.
 */
public class GhostRonin extends Entity {

    private static final int FRAME_SIZE = 64;
    private static final int ATTACK_DAMAGE = 15;

    private ArrayList<Snapshot> frames;
    private int frameIndex = 0;

    private Animation idleAnim;
    private Animation walkAnim;
    private Animation jumpAnim;
    private Animation currentAnim;

    private Rectangle pendingHitbox;

    public GhostRonin(ArrayList<Snapshot> frames, BufferedImage sheet) {
        super(frames.get(0).x, frames.get(0).y, FRAME_SIZE, FRAME_SIZE, 0, null);
        this.frames = frames;

        idleAnim = new Animation(AssetManager.sliceRow(sheet, FRAME_SIZE, 0, 2), 0.6);
        walkAnim = new Animation(AssetManager.sliceRow(sheet, FRAME_SIZE, 1, 4), 0.12);
        jumpAnim = new Animation(AssetManager.sliceRow(sheet, FRAME_SIZE, 2, 1), 1.0);
        currentAnim = idleAnim;
        sprite = currentAnim.getCurrentFrame();
    }

    @Override
    public void update(double dt) {
        Snapshot snap = frames.get(frameIndex);
        x = snap.x;
        y = snap.y;

        Animation next;
        if (snap.state == PlayerState.JUMPING) {
            next = jumpAnim;
        } else if (snap.state == PlayerState.WALKING || snap.state == PlayerState.DASHING
                   || snap.state == PlayerState.SNEAKING) {
            next = walkAnim;
        } else {
            next = idleAnim;
        }

        if (next != currentAnim) {
            currentAnim = next;
            currentAnim.reset();
        }
        currentAnim.update(dt);
        sprite = currentAnim.getCurrentFrame();

        pendingHitbox = snap.attackHitbox;

        frameIndex++;
        if (frameIndex >= frames.size()) {
            frameIndex = 0;   // loop the recording forever
        }
    }

    public Rectangle consumeAttackHitbox() {
        Rectangle result = pendingHitbox;
        pendingHitbox = null;
        return result;
    }

    public static int getAttackDamage() {
        return ATTACK_DAMAGE;
    }

    @Override
    public void render(Graphics2D g2) {
        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        super.render(g2);
        g2.setComposite(old);
    }
}