package RogueLike;

public abstract class Interactable extends Entity {

    protected double interactRange = 100;   // how close the player must be (pixels)
    protected String prompt;                // text shown as "[E] prompt"

    public Interactable(double x, double y, int width, int height, String prompt) {
        super(x, y, width, height, 0, null);   // speed 0: it doesn't move
        this.prompt = prompt;
    }
    

    public boolean isAvailable() {
        return true;
    }

    public abstract String interact();

    public String getPrompt() { return prompt; }
    public double getInteractRange() { return interactRange; }
}
