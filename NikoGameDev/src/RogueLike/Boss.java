package RogueLike;

import java.awt.image.BufferedImage;

public class Boss extends Enemy {

    private static final int MELEE_DAMAGE = 20;

    // Animation
    private Animation idleAnim;
    private Animation walkAnim;
    private Animation attackAnim;

    private double attackAnimTimer = 0;
    private static final double ATTACK_ANIM_TIME = 0.30;
    private boolean attackAnimating = false;
    private boolean attackHitApplied = false;
    private boolean facingLeft = false;

    public Boss(double x, double y, CollisionHandler collision) {

        super(x, y,160, 160,AssetManager.loadImage("res/images/boss_sheet.png"),
            collision,400, 55, 320, 70, 2.5);

        hitboxWidth = 45;
        hitboxHeight = 45;

        hitboxOffsetX = (width - hitboxWidth) / 2;
        hitboxOffsetY = 95;

        BufferedImage sheet = AssetManager.loadImage("res/images/boss_sheet.png");
        	idleAnim = new Animation(AssetManager.sliceRow(sheet, 160, 0, 3),0.20);
        	walkAnim = new Animation(AssetManager.sliceRow(sheet, 160, 2, 3),0.12);

        BufferedImage[] attackFrames = new BufferedImage[3];
        attackFrames[0] = sheet.getSubimage(160, 160, 160, 160);
        attackFrames[1] = sheet.getSubimage(320, 160, 160, 160);
        attackAnim = new Animation(attackFrames,0.15);
        sprite = idleAnim.getCurrentFrame();
    }

    @Override
    public void aiUpdate(double dt, Player player) {

        double dx = player.getHitboxCenterX() - getHitboxCenterX();
        facingLeft = dx < 0;

        super.aiUpdate(dt, player);

        if (attackAnimating) {

            attackAnimTimer -= dt;
            attackAnim.update(dt);
            sprite = attackAnim.getCurrentFrame();

            if (!attackHitApplied && attackAnimTimer <= 0.10) {
                attackHitApplied = true;

                if (player.isAlive()) {
                    player.takeDamage(MELEE_DAMAGE);
                }
            }

            if (attackAnimTimer <= 0) {
                attackAnimating = false;
                attackAnim.reset();
                idleAnim.reset();
                sprite = idleAnim.getCurrentFrame();
            }

            return;
        }

        // Walking animation
        if (getState() == EnemyState.CHASING) {

            walkAnim.update(dt);
            sprite = walkAnim.getCurrentFrame();

        } else {

            idleAnim.update(dt);
            sprite = idleAnim.getCurrentFrame();
        }
    }
    
	@Override
	 protected void performAttack(Player player) {
	
	     attackAnimating = true;
	     attackAnimTimer = ATTACK_ANIM_TIME;
	     attackAnim.reset();
	
	     attackHitApplied = false;
	 }
   
    @Override
    public void render(java.awt.Graphics2D g2) {

    	if (sprite == null) {
         return;
    	}

    	int drawX = (int) x;
    	int drawY = (int) y;

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

     // Health bar
    	int barWidth = 60;
     	int barX = (int) (x + width / 2.0 - barWidth / 2.0);
     	int barY = (int) y - 10;

     	g2.setColor(java.awt.Color.DARK_GRAY);
     	g2.fillRect(barX, barY, barWidth, 6);

     	g2.setColor(new java.awt.Color(200, 40, 200));
     	g2.fillRect(
     			barX,
     			barY,
     			(int) (barWidth * getHealthRatio()),
     			6
     );
 }
}