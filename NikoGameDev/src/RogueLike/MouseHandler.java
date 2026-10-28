package RogueLike;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * MouseHandler tracks the mouse position (in screen/window pixels)
 * and whether the left button was just clicked.
 */
public class MouseHandler implements MouseListener, MouseMotionListener {

    private volatile int mouseX;
    private volatile int mouseY;
    private volatile boolean leftClickPressed;
    private volatile boolean rightHeld;   // NEW: block is held, not clicked
    private boolean leftHeld;

    public int getMouseX() { return mouseX; }
    public int getMouseY() { return mouseY; }

    // Same "consume once" idea as InputHandler.consumeInteract().
    public boolean consumeLeftClick() {
        boolean wasPressed = leftClickPressed;
        leftClickPressed = false;
        return wasPressed;
    }
    
    public boolean isRightHeld() { return rightHeld; }   // NEW

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && !leftHeld) {
            leftClickPressed = true;
        }
        if (e.getButton() == MouseEvent.BUTTON1) {
            leftHeld = true;
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            rightHeld = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            leftHeld = false;
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            rightHeld = false;
        }
    }

    // Required by the interfaces, but we don't need them.
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
