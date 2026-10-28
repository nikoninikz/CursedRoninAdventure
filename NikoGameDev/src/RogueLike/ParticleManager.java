package RogueLike;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ParticleManager {

    private final List<BloodParticle> bloodParticles = new ArrayList<>();

    public void createBloodBurst(double x, double y, int amount) {

        for (int i = 0; i < amount; i++) {
            bloodParticles.add(
                new BloodParticle(x, y)
            );
        }
    }

    public void update(double dt) {

        Iterator<BloodParticle> iterator =
            bloodParticles.iterator();

        while (iterator.hasNext()) {

            BloodParticle particle = iterator.next();

            particle.update(dt);

            if (particle.isDead()) {
                iterator.remove();
            }
        }
    }

    public void render(Graphics2D g2) {

        for (BloodParticle particle : bloodParticles) {
            particle.render(g2);
        }
    }
}