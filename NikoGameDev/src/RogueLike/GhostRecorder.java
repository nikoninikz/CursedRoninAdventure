package RogueLike;

import java.awt.Rectangle;
import java.util.ArrayList;

/**
 * Records one Snapshot of the player every frame, for the whole
 * current run. When the player dies, this becomes the next
 * Ghost Ronin's replay data.
 */
public class GhostRecorder {

    private ArrayList<Snapshot> frames = new ArrayList<Snapshot>();

    public void record(Player player, Rectangle attackHitbox) {
        Snapshot snap = new Snapshot();
        snap.x = player.getX();
        snap.y = player.getY();
        snap.facingX = player.getFacingX();
        snap.facingY = player.getFacingY();
        snap.state = player.getState();
        snap.attackHitbox = attackHitbox;
        frames.add(snap);
    }

    public ArrayList<Snapshot> getFrames() {
        return frames;
    }
}