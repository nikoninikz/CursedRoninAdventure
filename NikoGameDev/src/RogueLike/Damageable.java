package RogueLike;

/**
 * Anything that can take damage: the training dummy now,
 * enemies and the player later.
 *
 * Note: getHitbox() is already provided by Entity, so any class
 * that extends Entity AND implements Damageable gets it for free.
 */
public interface Damageable {
    void takeDamage(int amount);
    boolean isAlive();
}