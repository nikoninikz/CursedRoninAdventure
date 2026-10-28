package RogueLike;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Player extends Entity implements Damageable {

    private static final int FRAME_SIZE = 64;
    private static final double PLAYER_SPEED = 220;

    private static final double SNEAK_MULTIPLIER = 0.5;
    private static final double JUMP_SPEED = 500;
    private static final double GRAVITY = 1200;

    private static final double WALK_FRAME_TIME = 0.12;
    private static final double SNEAK_FRAME_TIME = 0.24;

    private static final int MAX_HEALTH = 100;

    private static final double DASH_SPEED = 1000;
    private static final double DASH_DURATION = 0.18;
    private static final double DASH_COOLDOWN = 0.6;

    private static final double COMBO_WINDOW = 0.9;   // time to click again after a swing
    private static final int COMBO_BONUS_DAMAGE = 10; // extra damage on hit 2

    private InputHandler input;
    private CollisionHandler collision;
    private MouseHandler mouse;
    private Camera camera;
    private ParticleManager particles;

    private PlayerState state = PlayerState.IDLE;
    private double facingX = 0;
    private double facingY = 1;

    private double z = 0;
    private double zVelocity = 0;
    private boolean onGround = true;

    private Animation idleAnim;
    private Animation walkAnim;
    private Animation jumpAnim;
    private Animation currentAnim;

    private Weapon weapon = new Weapon("Katana", 25, 70, 0.18, 0.35);
    private double attackTimer = 0;
    private double cooldownTimer = 0;
    private Rectangle pendingHitbox;
    private int pendingDamage;              // NEW: damage for the current swing

    private int comboIndex = 0;             // 0 = first hit, 1 = second hit
    private double comboTimer = 0;          // window left to continue the combo

    private double dashTimer = 0;
    private double dashCooldownTimer = 0;
    private double dashDirX = 0;
    private double dashDirY = 1;

    private double blockFlashTimer = 0;

    private int health = MAX_HEALTH;

    public Player(double x, double y, BufferedImage sheet, InputHandler input,
                  CollisionHandler collision, MouseHandler mouse, Camera camera) {
        super(x, y, FRAME_SIZE, FRAME_SIZE, PLAYER_SPEED, null);
        this.input = input;
        this.collision = collision;
        this.mouse = mouse;
        this.camera = camera;

        hitboxWidth = 28;
        hitboxHeight = 20;
        hitboxOffsetX = (FRAME_SIZE - hitboxWidth) / 2;
        hitboxOffsetY = 40;

        idleAnim = new Animation(AssetManager.sliceRow(sheet, FRAME_SIZE, 0, 3), 0.6);
        walkAnim = new Animation(AssetManager.sliceRow(sheet, FRAME_SIZE, 1, 4), WALK_FRAME_TIME);
        jumpAnim = new Animation(AssetManager.sliceRow(sheet, FRAME_SIZE, 2, 2), 1.0);
        currentAnim = idleAnim;
        sprite = currentAnim.getCurrentFrame();
    }

    @Override
    public void update(double dt) {
        updateFacing();
        updateDash(dt);          // NEW: may move the player itself, and skips normal movement below

        boolean moving = false;
        if (dashTimer <= 0) {
            double dx = 0;
            double dy = 0;

            if (input.isUp())    { dy -= 1; }
            if (input.isDown())  { dy += 1; }
            if (input.isLeft())  { dx -= 1; }
            if (input.isRight()) { dx += 1; }

            moving = (dx != 0 || dy != 0);

            if (dx != 0 && dy != 0) {
                double length = Math.sqrt(dx * dx + dy * dy);
                dx = dx / length;
                dy = dy / length;
            }

            double currentSpeed = speed;
            if (input.isSneak()) {
                currentSpeed = speed * SNEAK_MULTIPLIER;
            }

            collision.moveEntity(this, dx * currentSpeed * dt, dy * currentSpeed * dt);
        } else {
            moving = true;   // treat dashing as "moving" for animation purposes
        }

        updateJump(dt);
        updateCombat(dt);
        updateState(moving);
        updateAnimation(dt, moving);

        if (blockFlashTimer > 0) {
            blockFlashTimer -= dt;
        }
    }

    private void updateFacing() {
        double mouseWorldX = mouse.getMouseX() + camera.getX();
        double mouseWorldY = mouse.getMouseY() + camera.getY();

        double dx = mouseWorldX - getHitboxCenterX();
        double dy = mouseWorldY - getHitboxCenterY();
        double length = Math.sqrt(dx * dx + dy * dy);

        if (length > 0.001) {
            facingX = dx / length;
            facingY = dy / length;
        }
    }

    // NEW: starts and runs the dash.
    private void updateDash(double dt) {
        if (dashCooldownTimer > 0) {
            dashCooldownTimer -= dt;
        }

        boolean wantsDash = input.consumeDash();
        boolean canStartDash = dashCooldownTimer <= 0 && dashTimer <= 0
                              && attackTimer <= 0 && !isBlocking();

        if (wantsDash && canStartDash) {
            dashTimer = DASH_DURATION;
            dashCooldownTimer = DASH_COOLDOWN;
            dashDirX = facingX;
            dashDirY = facingY;
        }

        if (dashTimer > 0) {
            collision.moveEntity(this, dashDirX * DASH_SPEED * dt, dashDirY * DASH_SPEED * dt);
            dashTimer -= dt;
        }
    }

    public boolean isDashing()   { return dashTimer > 0; }
    public boolean isInvulnerable() { return isDashing(); }   // NEW: i-frames while dashing
    public boolean isBlocking()  { return mouse.isRightHeld(); }   // NEW

    private void updateCombat(double dt) {
        pendingHitbox = null;

        if (cooldownTimer > 0) { cooldownTimer -= dt; }
        if (attackTimer > 0)   { attackTimer -= dt; }
        if (comboTimer > 0)    { comboTimer -= dt; }
        if (comboTimer <= 0)   { comboIndex = 0; }   // combo window expired: reset

        boolean clicked = mouse.consumeLeftClick();
        boolean canAttack = cooldownTimer <= 0 && dashTimer <= 0 && !isBlocking();

        if (clicked && canAttack) {
            if (comboTimer > 0) {
                comboIndex = (comboIndex + 1) % 2;
            } else {
                comboIndex = 0;
            }
            comboTimer = COMBO_WINDOW;

            pendingDamage = weapon.getDamage();
            if (comboIndex == 1) {
                pendingDamage += COMBO_BONUS_DAMAGE;   // second hit hits harder
            }

            attackTimer = weapon.getAttackDuration();
            cooldownTimer = weapon.getCooldown();

            double range = weapon.getRange();
            double centerX = getHitboxCenterX() + facingX * range / 2.0;
            double centerY = getHitboxCenterY() + facingY * range / 2.0;
            pendingHitbox = new Rectangle((int) (centerX - range / 2.0),
                                          (int) (centerY - range / 2.0),
                                          (int) range, (int) range);
        }
    }

    public Rectangle consumeAttackHitbox() {
        Rectangle result = pendingHitbox;
        pendingHitbox = null;
        return result;
    }

    public int getPendingAttackDamage() { return pendingDamage; }   // NEW
    public boolean isAttacking() { return attackTimer > 0; }

    private void updateState(boolean moving) {
        if (isDashing()) {
            state = PlayerState.DASHING;
        } else if (isAttacking()) {
            state = PlayerState.ATTACKING;
        } else if (isBlocking()) {
            state = PlayerState.BLOCKING;
        } else if (!onGround) {
            state = PlayerState.JUMPING;
        } else if (input.isSneak()) {
            state = PlayerState.SNEAKING;
        } else if (moving) {
            state = PlayerState.WALKING;
        } else {
            state = PlayerState.IDLE;
        }
    }

    private void updateAnimation(double dt, boolean moving) {
        Animation next;
        if (state == PlayerState.JUMPING) {
            next = jumpAnim;
        } else if (state == PlayerState.DASHING || moving) {
            next = walkAnim;
        } else {
            next = idleAnim;
        }

        if (next != currentAnim) {
            currentAnim = next;
            currentAnim.reset();
        }

        if (input.isSneak()) {
            walkAnim.setFrameTime(SNEAK_FRAME_TIME);
        } else {
            walkAnim.setFrameTime(WALK_FRAME_TIME);
        }

        currentAnim.update(dt);
        sprite = currentAnim.getCurrentFrame();
    }

    private void updateJump(double dt) {
        if (input.isJump() && onGround) {
            zVelocity = JUMP_SPEED;
            onGround = false;
        }

        if (!onGround) {
            zVelocity -= GRAVITY * dt;
            z += zVelocity * dt;

            if (z <= 0) {
                z = 0;
                zVelocity = 0;
                onGround = true;
            }
        }

        drawOffsetY = z;
    }

    @Override
    public void render(Graphics2D g2) {
        if (isDashing()) {                 // NEW: a fading trail behind the dash
            renderDashTrail(g2);
        }

        int shadowWidth = (int) (width * 0.6 - z * 0.5);
        if (shadowWidth < 8) { shadowWidth = 8; }
        int shadowHeight = shadowWidth / 3;

        int shadowX = (int) (x + width / 2.0 - shadowWidth / 2.0);
        int shadowY = (int) (y + height - shadowHeight - 2);

        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillOval(shadowX, shadowY, shadowWidth, shadowHeight);

        if (state == PlayerState.SNEAKING && sprite != null) {
            int crouchHeight = height - 8;
            int drawY = (int) (y - drawOffsetY) + (height - crouchHeight);

            Composite oldComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
            if (facingX >= 0) {

                g2.drawImage(
                    sprite,
                    (int) x,
                    drawY,
                    width,
                    crouchHeight,
                    null
                );
            } else {
                g2.drawImage(
                    sprite,
                    (int) (x + width),
                    drawY,
                    -width,
                    crouchHeight,
                    null
                );
            }
            g2.setComposite(oldComposite);
        } else {
        	if (sprite != null) {

                if (facingX >= 0) {
                    g2.drawImage(
                        sprite,
                        (int) x,
                        (int) (y - drawOffsetY),
                        width,
                        height,
                        null
                    );
                } else {
                    g2.drawImage(
                        sprite,
                        (int) (x + width),
                        (int) (y - drawOffsetY),
                        -width,
                        height,
                        null
                    );
                }
            } else {

                super.render(g2);

            }
        }

        if (isAttacking()) {
            renderSlash(g2);
        }
        if (isBlocking()) {                // NEW
            renderBlock(g2);
        }
        if (blockFlashTimer > 0) {         // NEW
            renderBlockFlash(g2);
        }
    }

    // NEW: a few faded copies of the sprite trailing behind the dash direction.
    private void renderDashTrail(Graphics2D g2) {
        Composite oldComposite = g2.getComposite();
        for (int i = 1; i <= 3; i++) {
            float alpha = 0.25f - i * 0.06f;
            if (alpha <= 0 || sprite == null) { continue; }
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            int offsetX = (int) (-dashDirX * i * 10);
            int offsetY = (int) (-dashDirY * i * 10);
            g2.drawImage(sprite, (int) x + offsetX, (int) (y - drawOffsetY) + offsetY,
                        width, height, null);
        }
        g2.setComposite(oldComposite);
    }

    private void renderSlash(Graphics2D g2) {
        double progress = 1 - (attackTimer / weapon.getAttackDuration());
        int alpha = (int) (200 * (1 - progress));

        double angle = Math.atan2(facingY, facingX);
        double arcDegrees = 80;
        int startAngle = (int) Math.toDegrees(-angle) - (int) (arcDegrees / 2);

        int radius = (int) weapon.getRange();
        int cx = (int) getHitboxCenterX() - radius;
        int cy = (int) getHitboxCenterY() - radius;

        // Second combo hit is drawn a bit bigger/brighter for feedback.
        Color slashColor = (comboIndex == 1)
            ? new Color(255, 230, 160, Math.max(alpha, 0))
            : new Color(230, 240, 255, Math.max(alpha, 0));

        g2.setColor(slashColor);
        g2.fillArc(cx, cy, radius * 2, radius * 2, startAngle, (int) arcDegrees);
    }

    // NEW: a translucent shield-like arc in front while holding right-click.
    private void renderBlock(Graphics2D g2) {
        double angle = Math.atan2(facingY, facingX);
        int radius = 40;
        int cx = (int) getHitboxCenterX() - radius;
        int cy = (int) getHitboxCenterY() - radius;

        g2.setColor(new Color(90, 220, 240, 90));
        g2.fillArc(cx, cy, radius * 2, radius * 2, (int) Math.toDegrees(-angle) - 60, 120);
        g2.setColor(new Color(150, 240, 255, 160));
        g2.drawArc(cx, cy, radius * 2, radius * 2, (int) Math.toDegrees(-angle) - 60, 120);
    }

    // NEW: a quick bright ring when a hit is actually blocked.
    private void renderBlockFlash(Graphics2D g2) {
        float progress = (float) (blockFlashTimer / 0.2);
        int radius = (int) (30 + (1 - progress) * 20);
        int alpha = (int) (200 * progress);
        int cx = (int) getHitboxCenterX() - radius;
        int cy = (int) getHitboxCenterY() - radius;

        g2.setColor(new Color(160, 240, 255, Math.max(alpha, 0)));
        g2.drawOval(cx, cy, radius * 2, radius * 2);
    }

    @Override
    public void takeDamage(int amount) {
        if (isInvulnerable()) {
            return;               // dashing through the hit
        }
        if (isBlocking()) {
            blockFlashTimer = 0.2;
            return;                // blocked, no damage
        }
        health -= amount;
        if (particles != null) {
            particles.createBloodBurst(
                getHitboxCenterX(),
                getHitboxCenterY(),
                16
            );
        }
        if (health < 0) { health = 0; }
    }

    @Override
    public boolean isAlive() {
        return health > 0;
    }

    public int getHealth()    { return health; }
    public int getMaxHealth() { return MAX_HEALTH; }

    public PlayerState getState() { return state; }
    public double getFacingX()    { return facingX; }
    public double getFacingY()    { return facingY; }
    public Weapon getWeapon()     { return weapon; }
    
    public void setParticleManager(ParticleManager particles) {
        this.particles = particles;
    }
    
}