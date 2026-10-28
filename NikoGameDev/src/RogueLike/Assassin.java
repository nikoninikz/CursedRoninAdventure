package RogueLike;

/**
 * Fast and fragile. Low health, high speed, hits often on a short cooldown.
 */
public class Assassin extends Enemy {

    private static final int ATTACK_DAMAGE = 18;

    public Assassin(double x, double y, CollisionHandler collision) {
        super(x, y, 64, 64, AssetManager.loadImage("res/images/assassin.png"), collision,
              25, 150, 240, 40, 0.7);

        hitboxWidth = 26;
        hitboxHeight = 26;
        hitboxOffsetX = (width - hitboxWidth) / 2;
        hitboxOffsetY = 18;
    }

    @Override
    protected void performAttack(Player player) {
        player.takeDamage(ATTACK_DAMAGE);
    }
}