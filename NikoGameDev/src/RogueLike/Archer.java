package RogueLike;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;

public class Archer extends Enemy {

    private static final int PROJECTILE_DAMAGE = 12;
    private static final double PROJECTILE_SPEED = 300;

    // Attack animation timing
    private static final double AIM_TIME = 1;
    private static final double RELEASE_TIME = 0.12;

    private Animation idleAnim;
    private Animation walkAnim;

    // 5 attack directions
    private BufferedImage[] aimFrames;
    private BufferedImage[] releaseFrames;

    private boolean attacking = false;
    private boolean releasing = false;

    private double attackAnimationTimer = 0;

    // Direction
    private int attackDirection = 0;
    private boolean facingLeft = false;

    public Archer(double x, double y, CollisionHandler collision) {

        super(
            x, y,
            64, 64,
            AssetManager.loadImage("res/images/archer_sheet.png"),
            collision,
            28,
            70,
            260,
            240,
            1.4
        );

        hitboxWidth = 30;
        hitboxHeight = 30;
        hitboxOffsetX = (width - hitboxWidth) / 2;
        hitboxOffsetY = 16;

        BufferedImage sheet = AssetManager.loadImage(
            "res/images/archer_sheet.png"
        );

        // Existing animations
        idleAnim = new Animation(
            AssetManager.sliceRow(sheet, 64, 0, 3),
            0.15
        );

        walkAnim = new Animation(
            AssetManager.sliceRow(sheet, 64, 1, 4),
            0.10
        );

        // New directional attack poses
        aimFrames = AssetManager.sliceRow(
            sheet, 64, 2, 5
        );

        releaseFrames = AssetManager.sliceRow(
            sheet, 64, 3, 5
        );

        // Start with idle sprite
        sprite = idleAnim.getCurrentFrame();
    }

    @Override
    public void aiUpdate(double dt, Player player) {

        if (handleDying(dt)) {   // ticks the fade timer AND stops it from acting
            return;
        }

        // Calculate direction toward player
        double dx = player.getHitboxCenterX() - getHitboxCenterX();
        double dy = player.getHitboxCenterY() - getHitboxCenterY();

        updateDirection(dx, dy);

        double distance = Math.sqrt(dx * dx + dy * dy);

        // Continue attack animation
        if (attacking) {

            attackAnimationTimer -= dt;

            if (releasing) {

                sprite = releaseFrames[attackDirection];

                if (attackAnimationTimer <= 0) {
                    releasing = false;
                    attacking = false;
                    sprite = idleAnim.getCurrentFrame();
                }

            } else {

                sprite = aimFrames[attackDirection];

                if (attackAnimationTimer <= 0) {
                    releasing = true;
                    attackAnimationTimer = RELEASE_TIME;

                    // Fire arrow exactly when releasing
                    fireProjectile(player);
                }
            }

            return;
        }

        // Chase player
        if (distance <= getDetectRange()) {

            if (distance > getAttackRange()) {

            	setState(EnemyState.CHASING);

                double moveX = (dx / distance) * 70 * dt;
                double moveY = (dy / distance) * 70 * dt;

                collision.moveEntity(this, moveX, moveY);

                walkAnim.update(dt);
                sprite = walkAnim.getCurrentFrame();

            } else {

            	setState(EnemyState.ATTACKING);

                startAttack();
            }

        } else {

        	setState(EnemyState.IDLE);

            idleAnim.update(dt);
            sprite = idleAnim.getCurrentFrame();
        }
    }

    private void startAttack() {

        attacking = true;
        releasing = false;

        attackAnimationTimer = AIM_TIME;

        sprite = aimFrames[attackDirection];
    }

    private void fireProjectile(Player player) {

        double dx = player.getHitboxCenterX() - getHitboxCenterX();
        double dy = player.getHitboxCenterY() - getHitboxCenterY();

        double length = Math.sqrt(dx * dx + dy * dy);

        if (length < 0.001) {
            return;
        }

        double vx = (dx / length) * PROJECTILE_SPEED;
        double vy = (dy / length) * PROJECTILE_SPEED;

        pendingProjectile = new Projectile(
            getHitboxCenterX(),
            getHitboxCenterY(),
            vx,
            vy,
            PROJECTILE_DAMAGE
        );
    }

    private void updateDirection(double dx, double dy) {

        facingLeft = dx < 0;

        double absX = Math.abs(dx);
        double absY = Math.abs(dy);

        // Mostly horizontal
        if (absX > absY * 1.7) {

            attackDirection = 0; // RIGHT

        }

        // Mostly vertical
        else if (absY > absX * 1.7) {

            if (dy < 0) {
                attackDirection = 2; // UP
            } else {
                attackDirection = 4; // DOWN
            }

        }

        // Diagonal
        else {

            if (dy < 0) {
                attackDirection = 1; // UP-RIGHT
            } else {
                attackDirection = 3; // DOWN-RIGHT
            }
        }
    }

    @Override
    public void render(Graphics2D g2) {
        if (sprite == null) {
            return;
        }

        int drawX = (int) x;
        int drawY = (int) y;

        Composite oldComposite = g2.getComposite();
        if (isDying()) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) getDeathFadeRatio()));
        }

        if (facingLeft) {
            g2.drawImage(sprite, drawX + width, drawY, -width, height, null);
        } else {
            g2.drawImage(sprite, drawX, drawY, width, height, null);
        }

        g2.setComposite(oldComposite);

        if (isDying()) {
            return;   // no flash or health bar while fading out
        }

        if (isFlashing()) {
            g2.setColor(new Color(255, 60, 60, 120));
            g2.fillRect(drawX, drawY, width, height);
        }

        int barWidth = 40;
        int barX = (int) (x + width / 2.0 - barWidth / 2.0);
        int barY = drawY - 8;
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(barX, barY, barWidth, 5);
        g2.setColor(new Color(200, 40, 200));
        g2.fillRect(barX, barY, (int) (barWidth * getHealthRatio()), 5);
    }

	@Override
	protected void performAttack(Player player) {
		// TODO Auto-generated method stub
		
	}
}