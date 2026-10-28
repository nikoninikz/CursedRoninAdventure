package RogueLike;

/**
 * Camera decides which part of the world is visible in the window.
 * x and y are the WORLD position of the window's top-left corner.
 */
public class Camera {

    private double x;
    private double y;
    private double zoom = 1.0;

    private final int viewWidth;    // window size
    private final int viewHeight;
    private final int worldWidth;   // world size
    private final int worldHeight;

    public Camera(int viewWidth, int viewHeight, int worldWidth, int worldHeight) {
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    /**
     * Centers the camera on the target (the player).
     */
    public void follow(Entity target) {
        // Put the target's center in the middle of the window.
        x = target.getX() + target.getWidth() / 2.0 - viewWidth / 2.0;
        y = target.getY() + target.getHeight() / 2.0 - viewHeight / 2.0;

        // Don't show anything outside the world.
        if (x < 0) { x = 0; }
        if (y < 0) { y = 0; }
        if (x > worldWidth - viewWidth)   { x = worldWidth - viewWidth; }
        if (y > worldHeight - viewHeight) { y = worldHeight - viewHeight; }
    }
    
    public void zoomIn() {
        zoom += 0.1;

        if (zoom > 2.0) {
            zoom = 2.0;
        }
    }

    public void zoomOut() {
        zoom -= 0.1;

        if (zoom < 0.5) {
            zoom = 0.5;
        }
    }

    public double getZoom() {
        return zoom;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public int getViewWidth()  { return viewWidth; }
    public int getViewHeight() { return viewHeight; }
}
