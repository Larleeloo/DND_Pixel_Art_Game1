import java.awt.*;
import java.util.ArrayList;

class EntityManager {

    private ArrayList<Entity> entities = new ArrayList<>();

    public void addEntity(Entity e) {
        entities.add(e);
    }

    public void updateAll(InputManager input) {
        PlayerEntity player = null;

        // Find the player
        for (Entity e : entities) {
            if (e instanceof PlayerEntity) {
                player = (PlayerEntity) e;
                break;
            }
        }

        // Update player with entity list for collisions
        if (player != null) {
            player.update(input, entities);
        }

        // Update all other entities (items, obstacles, etc.)
        for (Entity e : entities) {
            if (!(e instanceof PlayerEntity)) {
                e.update(input);
            }
        }
    }

    public void drawAll(Graphics g) {
        for (Entity e : entities) {
            e.draw(g);
        }
    }

    public PlayerEntity getPlayer() {
        for (Entity e : entities) {
            if (e instanceof PlayerEntity) {
                return (PlayerEntity) e;
            }
        }
        return null;
    }
}