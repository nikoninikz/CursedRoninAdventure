package RogueLike;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * InputHandler listens to the keyboard and remembers which
 * keys are currently held down.
 */
public class InputHandler implements KeyListener {

    private volatile boolean up;
    private volatile boolean down;
    private volatile boolean left;
    private volatile boolean right;
    private volatile boolean jump;    // Space
    private volatile boolean sneak;   // Shift
    private volatile boolean zoom;
    private volatile boolean interactPressed;
    private volatile boolean debug;
    private boolean eHeld;
    private boolean f3Held;
    private volatile boolean testDamagePressed;   // NEW: temporary, for testing HP
    private boolean hHeld;
    private volatile boolean dashPressed;
    private boolean qHeld;
    private volatile boolean journalOpen;
    private boolean jHeld;

    public boolean isUp()    { return up; }
    public boolean isDown()  { return down; }
    public boolean isLeft()  { return left; }
    public boolean isRight() { return right; }
    public boolean isJump()  { return jump; }
    public boolean isSneak() { return sneak; }
    public boolean isDebug() { return debug; }
    public boolean isZoom() { return zoom; }
    public boolean isJournalOpen() { return journalOpen; }
    
    // NEW: returns true ONCE per press of E, then resets itself.
    public boolean consumeInteract() {
        boolean wasPressed = interactPressed;
        interactPressed = false;
        return wasPressed;
    }
    
    // NEW: temporary test key, only meant for checking the health bar
    // before real enemies exist. We'll remove this in Milestone 6.
    public boolean consumeTestDamage() {
        boolean wasPressed = testDamagePressed;
        testDamagePressed = false;
        return wasPressed;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        setKey(e.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        setKey(e.getKeyCode(), false);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not needed, but KeyListener requires this method to exist.
    }
    
    // NEW: one-shot, like consumeInteract().
    public boolean consumeDash() {
        boolean wasPressed = dashPressed;
        dashPressed = false;
        return wasPressed;
    }

    private void setKey(int keyCode, boolean isDown) {
        switch (keyCode) {
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                up = isDown;
                break;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                down = isDown;
                break;
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                left = isDown;
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                right = isDown;
                break;
            case KeyEvent.VK_SPACE:
                jump = isDown;
                break;
            case KeyEvent.VK_SHIFT:
                sneak = isDown;
                break;
            case KeyEvent.VK_E:
                // Only count the moment the key goes down (not auto-repeat).
                if (isDown && !eHeld) {
                    interactPressed = true;
                }
                eHeld = isDown;
                break;
            case KeyEvent.VK_F3:
                if (isDown && !f3Held) {
                    debug = !debug;      // toggle debug view on/off
                }
            case KeyEvent.VK_H:
                if (isDown && !hHeld) {
                    testDamagePressed = true;
                }
                hHeld = isDown;
                break;
            case KeyEvent.VK_Q:
                if (isDown && !qHeld) {
                    dashPressed = true;
                }
                qHeld = isDown;
                break;
            case KeyEvent.VK_J:
                if (isDown && !jHeld) {
                    journalOpen = !journalOpen;
                }
                jHeld = isDown;
                break;
		default:
                break;
        }
    }
}