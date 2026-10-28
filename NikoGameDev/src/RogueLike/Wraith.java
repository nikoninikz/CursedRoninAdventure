package RogueLike;

/**
 * The basic melee enemy from Milestone 6, rewritten as an Enemy subclass.
 * Same numbers as before: 40 HP, chases at 90 px/s, hits for 10 on contact.
 */
public class Wraith extends Enemy {

    private static final int ATTACK_DAMAGE = 10;

    public Wraith(double x, double y, CollisionHandler collision) {
        super(x, y, 64, 64, AssetManager.loadImage("res/images/enemy.png"), collision,
              40, 90, 220, 46, 1.0);

        hitboxWidth = 32;
        hitboxHeight = 32;
        hitboxOffsetX = (width - hitboxWidth) / 2;
        hitboxOffsetY = 16;
    }

    @Override
    protected void performAttack(Player player) {
        player.takeDamage(ATTACK_DAMAGE);
    }
}