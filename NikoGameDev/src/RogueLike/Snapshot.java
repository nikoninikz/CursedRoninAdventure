package RogueLike;

import java.awt.Rectangle;

/**
 * One frame's worth of recorded player state. A whole run is just a
 * list of these, played back in order to create a Ghost Ronin.
 */
public class Snapshot {
    public double x;
    public double y;
    public double facingX;
    public double facingY;
    public PlayerState state;
    public Rectangle attackHitbox;   // null on every frame except when an attack fires
}