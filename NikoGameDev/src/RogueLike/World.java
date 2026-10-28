package RogueLike;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class World {

    public static final int TILE_SIZE = 64;
    public static final int ROOM_W = 12;
 	public static final int ROOM_H = 10;	

    // =========================================================
    // CURSED VILLAGE SIZE
    // =========================================================

    public static final int COLS = 28;
    public static final int ROWS = 20;

    public static final int WIDTH = COLS * TILE_SIZE;
    public static final int HEIGHT = ROWS * TILE_SIZE;

    public static final int FLOOR = 0;
    public static final int WALL = 1;
    public static final int BLOCKED = 2;

    // =========================================================
    // VILLAGE TILE TYPES
    // =========================================================

    private static final int TILE_GRASS_A = 0;
    private static final int TILE_GRASS_B = 1;
    private static final int TILE_PATH = 2;
    private static final int TILE_TREE = 3;
    private static final int TILE_HUT = 4;
    private static final int TILE_WATER = 5;
    private static final int TILE_RUBBLE = 6;

    // =========================================================
    // CURSED VILLAGE MAP
    // =========================================================

    /*
     * . = grass
     * , = dirt path
     * # = dead tree
     * H = ruined hut
     * ~ = corrupted water
     * o = rubble / broken well
     */

    private static final String[] VILLAGE_LAYOUT = {

        "############################",
        "#............,.............#",
        "#........HHHHHHHHH.........#",
        "#........H.......H....#....#",
        "#........H.......H......#..#",
        "#........H.......H.........#",
        "#........HHHHHHHHH.......#.#",
        "#............,.............#",
        "#............,..#..........#",
        "#..HH.HH.....,....#..~~....#",
        "#..HH.HH,,,,,,......~~~~...#",
        "#............,,,,,,,~~~~~..#",
        "#...HH.......,.....~~~~~~..#",
        "#...HH.......,......~~~~...#",
        "#.....#......,.......~~....#",
        "#..#.....#...,.............#",
        "#............,......#......#",
        "#............oo..#.........#",
        "#............,.............#",
        "############################"
    };

    // =========================================================
    // MAP DATA
    // =========================================================

    private int[][] map = new int[ROWS][COLS];
    private int[][] tileVariant = new int[ROWS][COLS];

    private BufferedImage[] villageTiles;

    private double startX;
    private double startY;

    // =========================================================
    // GAME OBJECTS
    // =========================================================

    private ArrayList<Interactable> interactables =
            new ArrayList<Interactable>();

    private ArrayList<TrainingDummy> dummies =
            new ArrayList<TrainingDummy>();

    private ArrayList<Enemy> enemies =
            new ArrayList<Enemy>();

    private ArrayList<Projectile> projectiles =
            new ArrayList<Projectile>();

    private CollisionHandler collision;

    private MemoryJournal journal;

    private ParticleManager particles;

    // No boss in the Cursed Village for now.
    private Boss boss;

    // =========================================================
    // CONSTRUCTORS
    // =========================================================

    /*
     * Main constructor used by the newer game code.
     */
    public World(MemoryJournal journal, ParticleManager particles) {

        this.journal = journal;
        this.particles = particles;

        BufferedImage sheet =
                AssetManager.loadImage("res/images/village_tiles.png");

        villageTiles =
                AssetManager.sliceRow(sheet, TILE_SIZE, 0, 7);

        loadVillage();

        collision = new CollisionHandler(this);

        placeVillageObjects();
    }

    /*
     * Compatibility constructor.
     *
     * If another part of the game still creates World using only
     * MemoryJournal, it will still work.
     */
    public World(MemoryJournal journal) {
        this(journal, null);
    }

    // =========================================================
    // LOAD CURSED VILLAGE
    // =========================================================

    private void loadVillage() {

        Random rnd = new Random();

        for (int row = 0; row < ROWS; row++) {

            String line = VILLAGE_LAYOUT[row];

            for (int col = 0; col < COLS; col++) {

                char ch = line.charAt(col);

                switch (ch) {

                    case '#':
                        map[row][col] = WALL;
                        tileVariant[row][col] = TILE_TREE;
                        break;

                    case 'H':
                        map[row][col] = WALL;
                        tileVariant[row][col] = TILE_HUT;
                        break;

                    case '~':
                        map[row][col] = WALL;
                        tileVariant[row][col] = TILE_WATER;
                        break;

                    case 'o':
                        map[row][col] = WALL;
                        tileVariant[row][col] = TILE_RUBBLE;
                        break;

                    case ',':
                        map[row][col] = FLOOR;
                        tileVariant[row][col] = TILE_PATH;
                        break;

                    default:
                        // Grass
                        map[row][col] = FLOOR;

                        tileVariant[row][col] =
                                (rnd.nextInt(6) == 0)
                                ? TILE_GRASS_B
                                : TILE_GRASS_A;

                        break;
                }
            }
        }

        // =====================================================
        // PLAYER START POSITION
        // =====================================================

        /*
         * The player starts on the southern village path.
         *
         * Column 13, Row 16.
         */
        startX =
                13 * TILE_SIZE + TILE_SIZE / 2.0;

        startY =
                16 * TILE_SIZE + TILE_SIZE / 2.0;
    }

    // =========================================================
    // PLACE VILLAGE OBJECTS
    // =========================================================

    private void placeVillageObjects() {

        BufferedImage lanternSheet =
                AssetManager.loadImage("res/images/lantern.png");

        BufferedImage chestSheet =
                AssetManager.loadImage("res/images/chest.png");

        BufferedImage fragmentSprite =
                AssetManager.loadImage("res/images/fragment.png");

        // =====================================================
        // LANTERN
        // =====================================================

        addLantern(lanternSheet, 13, 4);

        // =====================================================
        // TRAINING DUMMY
        // =====================================================

        dummies.add(
                new TrainingDummy(
                        startX + 130,
                        startY
                )
        );

        // =====================================================
        // CHESTS
        // =====================================================

        addChest(
                chestSheet,
                22,
                7,
                "Rusted Coin"
        );

        addChest(
                chestSheet,
                5,
                16,
                "Torn Letter"
        );

        // =====================================================
        // MEMORY FRAGMENT
        // =====================================================

        if (journal != null) {

            int id =
                    journal.randomUncollectedId(
                            new Random(),
                            new HashSet<Integer>()
                    );

            if (id != -1) {

                interactables.add(
                        new MemoryFragment(
                                11 * TILE_SIZE,
                                4 * TILE_SIZE,
                                fragmentSprite,
                                id,
                                journal
                        )
                );
            }
        }

        // =====================================================
        // ENEMIES
        // =====================================================

        Enemy wraith =
                new Wraith(
                        12 * TILE_SIZE,
                        8 * TILE_SIZE,
                        collision
                );

        Enemy archer =
                new Archer(
                        9 * TILE_SIZE,
                        13 * TILE_SIZE,
                        collision
                );

        Enemy oni =
                new Oni(
                        25 * TILE_SIZE,
                        10 * TILE_SIZE,
                        collision
                );

        Enemy assassin =
                new Assassin(
                        5 * TILE_SIZE,
                        17 * TILE_SIZE,
                        collision
                );

        // Give enemies access to particles when available.
        if (particles != null) {
            wraith.setParticleManager(particles);
            archer.setParticleManager(particles);
            oni.setParticleManager(particles);
            assassin.setParticleManager(particles);
        }

        enemies.add(wraith);
        enemies.add(archer);
        enemies.add(oni);
        enemies.add(assassin);
    }

    // =========================================================
    // OBJECT HELPERS
    // =========================================================

    private void addLantern(
            BufferedImage sheet,
            int col,
            int row) {

        interactables.add(
                new SpiritLantern(
                        col * TILE_SIZE,
                        row * TILE_SIZE,
                        sheet
                )
        );

        map[row][col] = BLOCKED;
    }

    private void addChest(
            BufferedImage sheet,
            int col,
            int row,
            String itemName) {

        interactables.add(
                new Chest(
                        col * TILE_SIZE,
                        row * TILE_SIZE,
                        sheet,
                        itemName
                )
        );
    }

    // =========================================================
    // COLLISION
    // =========================================================

    public boolean isSolid(int col, int row) {

        if (col < 0 ||
            row < 0 ||
            col >= COLS ||
            row >= ROWS) {

            return true;
        }

        return map[row][col] != FLOOR;
    }

    // =========================================================
    // PLAYER START
    // =========================================================

    public double getStartX() {
        return startX;
    }

    public double getStartY() {
        return startY;
    }

    // =========================================================
    // BOSS
    // =========================================================

    /*
     * Cursed Village currently has no boss.
     *
     * Kept so GamePanel and other code that expects getBoss()
     * will still compile.
     */
    public Boss getBoss() {
        return boss;
    }

    // =========================================================
    // UPDATE INTERACTABLES
    // =========================================================

    public void update(double dt) {

        for (Interactable item : interactables) {
            item.update(dt);
        }
    }

    // =========================================================
    // UPDATE TRAINING DUMMIES
    // =========================================================

    public void updateDummies(double dt) {

        for (TrainingDummy dummy : dummies) {
            dummy.update(dt);
        }
    }

    // =========================================================
    // UPDATE ENEMIES
    // =========================================================

    public void updateEnemies(
            double dt,
            Player player) {

        for (Enemy enemy : enemies) {

            enemy.aiUpdate(dt, player);

            Projectile p =
                    enemy.consumeProjectile();

            if (p != null) {
                projectiles.add(p);
            }

            projectiles.addAll(
                    enemy.consumeProjectiles()
            );
        }

        enemies.removeIf(
                enemy -> enemy.isReadyToRemove()
        );
    }

    // =========================================================
    // UPDATE PROJECTILES
    // =========================================================

    public void updateProjectiles(
            double dt,
            Player player) {

        for (Projectile p : projectiles) {

            p.update(dt);

            int col =
                    (int)
                    (p.getHitboxCenterX() / TILE_SIZE);

            int row =
                    (int)
                    (p.getHitboxCenterY() / TILE_SIZE);

            // Projectile hit a wall/tree/water/etc.
            if (isSolid(col, row)) {

                p.expire();
                continue;
            }

            // Projectile hit player.
            if (p.getHitbox().intersects(
                    player.getHitbox())) {

                player.takeDamage(
                        p.getDamage()
                );

                p.expire();
            }
        }

        projectiles.removeIf(
                p -> p.isExpired()
        );
    }

    // =========================================================
    // ENEMY ACCESS
    // =========================================================

    public ArrayList<Enemy> getEnemies() {
        return enemies;
    }

    // =========================================================
    // PLAYER ATTACK
    // =========================================================

    public boolean resolveAttack(
            Rectangle attackHitbox,
            int damage) {

        boolean hitSomething = false;

        // Training dummies
        for (TrainingDummy dummy : dummies) {

            if (dummy.isAlive() &&
                attackHitbox.intersects(
                        dummy.getHitbox())) {

                dummy.takeDamage(damage);

                hitSomething = true;
            }
        }

        // Enemies
        for (Enemy enemy : enemies) {

            if (enemy.isAlive() &&
                attackHitbox.intersects(
                        enemy.getHitbox())) {

                enemy.takeDamage(damage);

                hitSomething = true;
            }
        }

        return hitSomething;
    }

    // =========================================================
    // FIND NEARBY INTERACTABLE
    // =========================================================

    public Interactable findNearbyInteractable(
            Entity who) {

        Interactable closest = null;

        double closestDistance =
                Double.MAX_VALUE;

        for (Interactable item : interactables) {

            if (!item.isAvailable()) {
                continue;
            }

            double dx =
                    (item.getX()
                    + item.getWidth() / 2.0)
                    - who.getHitboxCenterX();

            double dy =
                    (item.getY()
                    + item.getHeight() / 2.0)
                    - who.getHitboxCenterY();

            double distance =
                    Math.sqrt(
                            dx * dx +
                            dy * dy
                    );

            if (distance <=
                    item.getInteractRange()
                    &&
                    distance <
                    closestDistance) {

                closest = item;
                closestDistance = distance;
            }
        }

        return closest;
    }

    // =========================================================
    // RENDER WORLD
    // =========================================================

    public void render(
            Graphics2D g2,
            Camera camera) {

        int camX =
                (int) camera.getX();

        int camY =
                (int) camera.getY();

        int startCol =
                Math.max(
                        0,
                        camX / TILE_SIZE
                );

        int startRow =
                Math.max(
                        0,
                        camY / TILE_SIZE
                );

        int endCol =
                (camX +
                 camera.getViewWidth())
                / TILE_SIZE;

        int endRow =
                (camY +
                 camera.getViewHeight())
                / TILE_SIZE;

        if (endCol > COLS - 1) {
            endCol = COLS - 1;
        }

        if (endRow > ROWS - 1) {
            endRow = ROWS - 1;
        }

        // =====================================================
        // DRAW VILLAGE TILES
        // =====================================================

        for (int row = startRow;
             row <= endRow;
             row++) {

            for (int col = startCol;
                 col <= endCol;
                 col++) {

                int drawX =
                        col * TILE_SIZE;

                int drawY =
                        row * TILE_SIZE;

                BufferedImage tile =
                        villageTiles[
                                tileVariant[row][col]
                        ];

                if (tile != null) {

                    g2.drawImage(
                            tile,
                            drawX,
                            drawY,
                            null
                    );

                } else {

                    g2.setColor(
                            new Color(
                                    62,
                                    64,
                                    80
                            )
                    );

                    g2.fillRect(
                            drawX,
                            drawY,
                            TILE_SIZE,
                            TILE_SIZE
                    );
                }
            }
        }

        // =====================================================
        // DRAW INTERACTABLES
        // =====================================================

        for (Interactable item : interactables) {
            item.render(g2);
        }

        // =====================================================
        // DRAW TRAINING DUMMIES
        // =====================================================

        for (TrainingDummy dummy : dummies) {
            dummy.render(g2);
        }

        // =====================================================
        // DRAW ENEMIES
        // =====================================================

        for (Enemy enemy : enemies) {
            enemy.render(g2);
        }

        // =====================================================
        // DRAW PROJECTILES
        // =====================================================

        for (Projectile p : projectiles) {
            p.render(g2);
        }
    }
}