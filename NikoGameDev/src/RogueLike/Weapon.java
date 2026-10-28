package RogueLike;

/**
 * Weapon holds the stats for one melee weapon. Only the katana exists
 * for now; later weapons (Milestone 10) can become their own class,
 * or this class can grow fields as needed.
 */
public class Weapon {

    private String name;
    private int damage;
    private double range;           // how far the hitbox reaches, in pixels
    private double attackDuration;  // how long the swing visual lasts (seconds)
    private double cooldown;        // time before you can attack again (seconds)

    public Weapon(String name, int damage, double range,
                  double attackDuration, double cooldown) {
        this.name = name;
        this.damage = damage;
        this.range = range;
        this.attackDuration = attackDuration;
        this.cooldown = cooldown;
    }

    public String getName()            { return name; }
    public int getDamage()             { return damage; }
    public double getRange()           { return range; }
    public double getAttackDuration()  { return attackDuration; }
    public double getCooldown()        { return cooldown; }
}