package RogueLike;

import java.util.ArrayList;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public abstract class Enemy extends Entity implements Damageable {

    private static final double DEATH_FADE_TIME = 0.4;

    protected CollisionHandler collision;
    protected ParticleManager particles;

    private int maxHealth;
    private int health;
    private double speed;
    private double detectRange;
    private double attackRange;
    private double attackCooldown;

    private EnemyState state = EnemyState.IDLE;
    private double attackTimer = 0;
    private double flashTimer = 0;

    private boolean dying = false;
    private double deathTimer = 0;

    // NEW: only ranged subclasses (like Archer) ever set this.
    protected Projectile pendingProjectile;
    

    public Enemy(double x, double y, int width, int height, BufferedImage sprite,
                CollisionHandler collision, int maxHealth, double speed,
                double detectRange, double attackRange, double attackCooldown) {
        super(x, y, width, height, speed, sprite);
        this.collision = collision;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.speed = speed;
        this.detectRange = detectRange;
        this.attackRange = attackRange;
        this.attackCooldown = attackCooldown;
    }

    // Entity requires this to exist; the real logic runs in aiUpdate below,
    // since enemies need the player's position and update(dt) alone doesn't have it.
    @Override
    public void update(double dt) {
    }

    public void aiUpdate(double dt, Player player) {
        if (flashTimer > 0) {
            flashTimer -= dt;
        }

        if (dying) {
            deathTimer -= dt;
            return;
        }

        double dx = player.getHitboxCenterX() - getHitboxCenterX();
        double dy = player.getHitboxCenterY() - getHitboxCenterY();
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (attackTimer > 0) {
            attackTimer -= dt;
        }

        if (distance <= attackRange) {
            state = EnemyState.ATTACKING;
            if (attackTimer <= 0 && player.isAlive()) {
                performAttack(player);
                attackTimer = attackCooldown;
            }
        } else if (distance <= detectRange) {
            state = EnemyState.CHASING;
            double moveX = (dx / distance) * speed * dt;
            double moveY = (dy / distance) * speed * dt;
            collision.moveEntity(this, moveX, moveY);
        } else {
            state = EnemyState.IDLE;
        }
    }
    
    protected boolean handleDying(double dt) {
        if (dying) {
            deathTimer -= dt;
        }
        return dying;
    }

    protected boolean isDying()          { return dying; }
    protected boolean isFlashing()       { return flashTimer > 0; }
    protected double getHealthRatio()    { return health / (double) maxHealth; }
    protected double getDeathFadeRatio() { return Math.max(0, deathTimer / DEATH_FADE_TIME); }

    // NEW: every enemy type defines its own attack here.
    // Wraith hits the player directly; Archer fires a Projectile instead.
    protected abstract void performAttack(Player player);

    public Projectile consumeProjectile() {
        Projectile p = pendingProjectile;
        pendingProjectile = null;
        return p;
    }

    @Override
    public void takeDamage(int amount) {
        if (dying) {
            return;
        }
        health -= amount;
        if (particles != null) {
            particles.createBloodBurst(
                getHitboxCenterX(),
                getHitboxCenterY(),
                16
            );
        }
        flashTimer = 0.15;
        if (health <= 0) {
            health = 0;
            dying = true;
            deathTimer = DEATH_FADE_TIME;
        }
    }

    @Override
    public boolean isAlive() {
        return health > 0;
    }

    public boolean isReadyToRemove() {
        return dying && deathTimer <= 0;
    }

    public EnemyState getState() {
        return state;
    }

    protected void setState(EnemyState state) {
        this.state = state;
    }
    public double getDetectRange() { return detectRange; }
    public double getAttackRange() { return attackRange; }

    @Override
    public void render(Graphics2D g2) {
        Composite oldComposite = g2.getComposite();

        if (dying) {
            float alpha = (float) Math.max(0, deathTimer / DEATH_FADE_TIME);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }

        super.render(g2);
        g2.setComposite(oldComposite);

        if (dying) {
            return;
        }

        if (flashTimer > 0) {
            g2.setColor(new Color(255, 60, 60, 120));
            g2.fillRect((int) x, (int) y, width, height);
        }

        int barWidth = 40;
        int barX = (int) (x + width / 2.0 - barWidth / 2.0);
        int barY = (int) y - 8;
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(barX, barY, barWidth, 5);
        g2.setColor(new Color(200, 40, 200));
        g2.fillRect(barX, barY, (int) (barWidth * (health / (double) maxHealth)), 5);
    }
    
    public void setParticleManager(ParticleManager particles) {
        this.particles = particles;
    }
    
    // NEW: a separate multi-projectile queue for enemies that fire several
    // bolts at once (like the boss's volley). Archer's single-shot
    // pendingProjectile field is untouched.
    protected ArrayList<Projectile> pendingProjectiles = new ArrayList<Projectile>();

    protected void queueProjectile(Projectile p) {
        pendingProjectiles.add(p);
    }

    public ArrayList<Projectile> consumeProjectiles() {
        ArrayList<Projectile> result = pendingProjectiles;
        pendingProjectiles = new ArrayList<Projectile>();
        return result;
    }
    
}