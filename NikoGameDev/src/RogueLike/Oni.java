package RogueLike;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Oni extends Enemy {

    private static final int ATTACK_DAMAGE = 20;
    private static final double ATTACK_TOTAL_TIME = 0.90;   // matches your attack cooldown feel
    private static final double HIT_FRAME_TIME = 0.15;      // when damage actually lands

    private Animation idleAnim;
    private Animation walkAnim;
    private BufferedImage[] attackFrames;   // 3 frames, drawn manually (not looped)  // 3 frames

    private boolean attacking = false;
    private double attackTimer = 0;
    private boolean damageApplied = false;
    private boolean facingLeft = false;

    public Oni(double x, double y, CollisionHandler collision) {
        super(x, y, 160, 160, AssetManager.loadImage("res/images/oni_sheet.png"), collision,
              90, 60, 200, 56, 1.4);

        hitboxWidth = 45; 
        hitboxHeight = 45;
        hitboxOffsetX = (width - hitboxWidth) / 2;
        hitboxOffsetY = 95;

        BufferedImage sheet = AssetManager.loadImage("res/images/oni_sheet.png");
        idleAnim = new Animation(AssetManager.sliceRow(sheet, 160, 0, 3), 0.4);
        walkAnim = new Animation(AssetManager.sliceRow(sheet, 160, 2, 3), 0.12);
        attackFrames = AssetManager.sliceRow(sheet, 160, 1, 2);
        sprite = idleAnim.getCurrentFrame();
    }

    @Override
    public void aiUpdate(double dt, Player player) {
        if (isDying()) {
        	handleDying(dt);
            return;
        }

        double dx = player.getHitboxCenterX() - getHitboxCenterX();
        double dy = player.getHitboxCenterY() - getHitboxCenterY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        facingLeft = dx > 0;

        if (attacking) {
            attackTimer -= dt;
            int frame = (int) ((1 - attackTimer / ATTACK_TOTAL_TIME) * attackFrames.length);
            frame = Math.min(frame, attackFrames.length - 1);
            sprite = attackFrames[frame];

            if (!damageApplied && attackTimer <= ATTACK_TOTAL_TIME - HIT_FRAME_TIME) {
                if (player.isAlive()) { performAttack(player); }
                damageApplied = true;
            }
            if (attackTimer <= 0) { attacking = false; }
            return;
        }

        if (distance <= getAttackRange()) {
            attacking = true;
            attackTimer = ATTACK_TOTAL_TIME;
            damageApplied = false;
        } else if (distance <= getDetectRange()) {
            double moveX = (dx / distance) * 60 * dt;
            double moveY = (dy / distance) * 60 * dt;
            collision.moveEntity(this, moveX, moveY);
            walkAnim.update(dt);
            sprite = walkAnim.getCurrentFrame();
        } else {
            idleAnim.update(dt);
            sprite = idleAnim.getCurrentFrame();
        }
    }

    @Override
    protected void performAttack(Player player) {
        player.takeDamage(ATTACK_DAMAGE);
    }

    @Override
    public void render(Graphics2D g2) {

        if (sprite == null) {
            return;
        }

        int drawX = (int) (getHitboxCenterX() - width / 2.0);
        int drawY = (int) (getHitboxCenterY() - height / 2.0);

        // Save the current graphics state
        java.awt.Composite oldComposite = g2.getComposite();

        // Apply death fade
        if (isDying()) {
            float alpha = (float) getDeathFadeRatio();

            g2.setComposite(
                java.awt.AlphaComposite.getInstance(
                    java.awt.AlphaComposite.SRC_OVER,
                    alpha
                )
            );
        }

        // Draw Oni with left/right mirroring
        if (facingLeft) {
            g2.drawImage(
                sprite,
                drawX + width,
                drawY,
                -width,
                height,
                null
            );
        } else {
            g2.drawImage(
                sprite,
                drawX,
                drawY,
                width,
                height,
                null
            );
        }

        // Restore normal transparency
        g2.setComposite(oldComposite);

        // Don't show effects/HP bar while dying
        if (isDying()) {
            return;
        }

        // Damage flash
        if (isFlashing()) {
            g2.setColor(new Color(255, 60, 60, 120));
            g2.fillRect(
                drawX,
                drawY,
                width,
                height
            );
        }

        // HP bar
        int barWidth = 50;

        int barX = (int) (
            x + width / 2.0 - barWidth / 2.0
        );

        int barY = (int) y - 8;

        g2.setColor(Color.DARK_GRAY);

        g2.fillRect(
            barX,
            barY,
            barWidth,
            6
        );

        g2.setColor(new Color(200, 40, 200));

        g2.fillRect(
            barX,
            barY,
            (int) (barWidth * getHealthRatio()),
            6
        );
    }
}