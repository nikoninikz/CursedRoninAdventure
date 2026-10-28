package RogueLike;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable {

    public static final int SCREEN_WIDTH = 960;
    public static final int SCREEN_HEIGHT = 540;

    private Thread gameThread;
    private boolean running;
    private int fps;

    private InputHandler input;
    private MouseHandler mouseHandler;
    private World world;
    private Camera camera;
    private CollisionHandler collision;
    private Player player;
    private ParticleManager particles;

    private Interactable nearbyInteractable;
    private String hudMessage = "";
    private double hudMessageTimer = 0; 

    private GhostRecorder recorder = new GhostRecorder();
    private GhostRonin ghost;   // null until the player has died once
    private MemoryJournal journal = new MemoryJournal();

    public GamePanel() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(new Color(30, 30, 40));
        setDoubleBuffered(true);

        input = new InputHandler();
        addKeyListener(input);

        mouseHandler = new MouseHandler();
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        setFocusable(true);
        
        particles = new ParticleManager();
        
        camera = new Camera(SCREEN_WIDTH, SCREEN_HEIGHT, World.WIDTH, World.HEIGHT);
        
        startFreshRun();
    }

    private void startFreshRun() {
        world = new World(journal, particles);   // CHANGED
        collision = new CollisionHandler(world);

        BufferedImage playerSheet = AssetManager.loadImage("res/images/player_sheet.png");
        player = new Player(world.getStartX() - 32, world.getStartY() - 32,
                            playerSheet, input, collision, mouseHandler, camera);
        
        player.setParticleManager(particles);
        
        nearbyInteractable = null;
        hudMessage = "";
        hudMessageTimer = 0;
    }

    private void startNewRunAfterDeath() {
        ArrayList<Snapshot> lastRun = recorder.getFrames();
        if (!lastRun.isEmpty()) {
            BufferedImage ghostSheet = AssetManager.loadImage("res/images/player_sheet.png");
            ghost = new GhostRonin(lastRun, ghostSheet);
        }

        startFreshRun();
        recorder = new GhostRecorder();   // start recording the NEW run from scratch

        hudMessage = "A Ghost Ronin lingers in the mist...";
        hudMessageTimer = 3.0;
    }

    public void startGameThread() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
        requestFocusInWindow();
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double fpsTimer = 0;
        int frameCount = 0;

        while (running) {
            long now = System.nanoTime();
            double dt = (now - lastTime) / 1_000_000_000.0;
            lastTime = now;

            if (dt > 0.1) {
                dt = 0.1;
            }

            update(dt);
            repaint();

            frameCount++;
            fpsTimer += dt;
            if (fpsTimer >= 1.0) {
                fps = frameCount;
                frameCount = 0;
                fpsTimer -= 1.0;
            }

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void update(double dt) {
        player.update(dt);
        world.updateDummies(dt);
        particles.update(dt);
        world.updateEnemies(dt, player);
        world.updateProjectiles(dt, player);   // NEW
        world.update(dt);
        camera.follow(player);

        updateInteraction(dt);
        updateCombat();       // CHANGED: now also feeds the recorder
        updateGhost(dt);       // NEW

        if (input.consumeTestDamage()) {
            player.takeDamage(10);
        }

        if (!player.isAlive()) {   // NEW: the core Ghost Ronin trigger
            startNewRunAfterDeath();
        }
    }

    private void updateCombat() {
        Rectangle hitbox = player.consumeAttackHitbox();
        if (hitbox != null) {
            world.resolveAttack(hitbox, player.getPendingAttackDamage());
        }
        recorder.record(player, hitbox);   // NEW: log this frame either way
    }

    // NEW: advances the ghost and lets its recorded attacks hit things.
    private void updateGhost(double dt) {
        if (ghost == null) {
            return;
        }
        ghost.update(dt);
        Rectangle ghostHitbox = ghost.consumeAttackHitbox();
        if (ghostHitbox != null) {
            world.resolveAttack(ghostHitbox, GhostRonin.getAttackDamage());
        }
    }

    private void updateInteraction(double dt) {
        nearbyInteractable = world.findNearbyInteractable(player);

        boolean pressedE = input.consumeInteract();
        if (pressedE && nearbyInteractable != null) {
            hudMessage = nearbyInteractable.interact();
            hudMessageTimer = (nearbyInteractable instanceof MemoryFragment) ? 5.0 : 2.5;   // CHANGED
        }

        if (hudMessageTimer > 0) {
            hudMessageTimer -= dt;
            if (hudMessageTimer <= 0) {
                hudMessage = "";
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        render(g2);
        Toolkit.getDefaultToolkit().sync();
    }

    private void render(Graphics2D g2) {
        AffineTransform screenTransform = g2.getTransform();

        g2.translate(-(int) camera.getX(), -(int) camera.getY());
        world.render(g2, camera);
        if (ghost != null) {          // NEW
            ghost.render(g2);
        }
        player.render(g2);
        particles.render(g2);
        if (input.isDebug()) {
            renderDebugWorld(g2);
        }

        g2.setTransform(screenTransform);

        renderHud(g2);
    }

    private void renderDebugWorld(Graphics2D g2) {
        Rectangle box = player.getHitbox();
        g2.setColor(Color.GREEN);
        g2.drawRect(box.x, box.y, box.width, box.height);

        int centerX = (int) player.getHitboxCenterX();
        int centerY = (int) player.getHitboxCenterY();
        g2.setColor(Color.YELLOW);
        g2.drawLine(centerX, centerY,
                    (int) (centerX + player.getFacingX() * 40),
                    (int) (centerY + player.getFacingY() * 40));

        g2.setColor(new Color(255, 180, 60, 120));
        for (Enemy enemy : world.getEnemies()) {
            int r1 = (int) enemy.getDetectRange();
            int r2 = (int) enemy.getAttackRange();
            int cx = (int) enemy.getHitboxCenterX();
            int cy = (int) enemy.getHitboxCenterY();
            g2.drawOval(cx - r1, cy - r1, r1 * 2, r1 * 2);
            g2.setColor(Color.RED);
            g2.drawOval(cx - r2, cy - r2, r2 * 2, r2 * 2);
            g2.setColor(new Color(255, 180, 60, 120));
        }
    }

    private void renderHud(Graphics2D g2) {
        g2.setFont(new Font("Monospaced", Font.BOLD, 16));
        g2.setColor(Color.WHITE);
        g2.drawString("FPS: " + fps, 10, 24);

        int barX = 10, barY = 34, barWidth = 160, barHeight = 14;
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(barX, barY, barWidth, barHeight);
        double ratio = player.getHealth() / (double) player.getMaxHealth();
        g2.setColor(Color.RED);
        g2.fillRect(barX, barY, (int) (barWidth * ratio), barHeight);
        g2.setColor(Color.WHITE);
        g2.drawRect(barX, barY, barWidth, barHeight);
        g2.setFont(new Font("Monospaced", Font.BOLD, 14));
        drawCenteredText(g2, "Cursed Guardian", - 2);
        g2.drawString("HP: " + player.getHealth() + "/" + player.getMaxHealth(), barX + 4, barY + 12);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 13));
        g2.setColor(new Color(200, 170, 255));
        g2.drawString("Memories: " + journal.getCollectedCount() + "/" + journal.getTotalCount(),
                      barX, barY + barHeight + 20);

        if (ghost != null) { 
            g2.setColor(new Color(150, 200, 255));
            g2.drawString("A Ghost Ronin Now Walks With You", 10, 84);
        }

        if (input.isDebug()) {
            g2.setColor(Color.WHITE);
            g2.drawString("State: " + player.getState(), 10, 104);
            g2.drawString(String.format("Facing: %.1f, %.1f",
                          player.getFacingX(), player.getFacingY()), 10, 124);
        }
            g2.setFont(new Font("Monospaced", Font.BOLD, 18));
            if (nearbyInteractable != null) {
                g2.setColor(Color.WHITE);
                drawCenteredText(g2, "[E] " + nearbyInteractable.getPrompt(), SCREEN_HEIGHT - 50);
            }

            if (!hudMessage.isEmpty()) {
                g2.setColor(new Color(150, 250, 240));
                String[] lines = hudMessage.split("\n");
                int lineY = 60;
                for (String line : lines) {
                    drawCenteredText(g2, line, lineY);
                    lineY += 20;
                }
            }

            g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
            g2.setColor(new Color(200, 200, 200));
            g2.drawString("WASD move | Shift sneak | Space jump | Click attack | Q dash | RMB block | E interact | J journal | F3 debug",
                    10, SCREEN_HEIGHT - 12);
            
            if (input.isJournalOpen()) {
                renderJournal(g2);
            }           
            
        }
    
    private void renderJournal(Graphics2D g2) {
        g2.setColor(new Color(10, 8, 16, 210));
        g2.fillRect(60, 40, SCREEN_WIDTH - 120, SCREEN_HEIGHT - 80);

        g2.setFont(new Font("Monospaced", Font.BOLD, 18));
        g2.setColor(new Color(150, 250, 240));
        g2.drawString("Memories  (" + journal.getCollectedCount() + "/" + journal.getTotalCount() + ")", 80, 70);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 13));
        int lineY = 100;
        for (int id = 0; id < journal.getTotalCount(); id++) {
            boolean known = journal.isCollected(id);
            String text = known ? journal.getText(id) : "??? (not yet recovered)";
            g2.setColor(known ? Color.WHITE : new Color(120, 120, 130));
            g2.drawString((id + 1) + ". " + text, 80, lineY);
            lineY += 24;
        }

        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2.setColor(new Color(200, 200, 200));
        g2.drawString("Press J to close", 80, SCREEN_HEIGHT - 50);
    }


    private void drawCenteredText(Graphics2D g2, String text, int y) {
        FontMetrics metrics = g2.getFontMetrics();
        int textX = (SCREEN_WIDTH - metrics.stringWidth(text)) / 2;
        g2.drawString(text, textX, y);
    }
}