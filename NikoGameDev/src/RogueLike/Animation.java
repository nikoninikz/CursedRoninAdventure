package RogueLike;

import java.awt.image.BufferedImage;

/**
 * Animation plays a list of frames in a loop.
 * It doesn't know anything about players or enemies, so every
 * character can reuse it.
 */
public class Animation {

    private BufferedImage[] frames;
    private double frameTime;     // seconds each frame is shown
    private double timer = 0;     // time spent on the current frame
    private int index = 0;        // which frame we're showing

    public Animation(BufferedImage[] frames, double frameTime) {
        this.frames = frames;
        this.frameTime = frameTime;
    }

    public void update(double dt) {
        timer += dt;
        // If enough time passed, go to the next frame (and loop around).
        while (timer >= frameTime) {
            timer -= frameTime;
            index = (index + 1) % frames.length;   // % wraps 3 -> 0
        }
    }

    public BufferedImage getCurrentFrame() {
        return frames[index];
    }

    // Start again from the first frame.
    public void reset() {
        index = 0;
        timer = 0;
    }

    public void setFrameTime(double frameTime) {
        this.frameTime = frameTime;
    }
}
