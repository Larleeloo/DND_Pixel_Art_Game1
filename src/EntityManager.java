import java.awt.*;
import java.util.ArrayList;

class EntityManager {

    private ArrayList<Entity> entities = new ArrayList<>();

    public void addEntity(Entity e) {
        entities.add(e);
    }

    public void updateAll(InputManager input) {
        PlayerEntity player = null;

        for (Entity e : entities) {
            if (e instanceof PlayerEntity) {
                player = (PlayerEntity) e;
                break;
            }
        }

        if (player != null) {
            player.update(input, entities);
        }

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
}
