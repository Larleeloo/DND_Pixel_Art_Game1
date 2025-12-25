package entity;
import block.*;
import input.*;
import graphics.*;
import animation.*;
import audio.*;

import java.awt.*;

/**
 * Visual indicator for the ground level.
 * Should be added LAST to EntityManager so it draws on top.
 */
public class GroundEntity extends Entity {

    private static final int GROUND_Y = 1800;

    public GroundEntity() {
        super(0, GROUND_Y);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(0, GROUND_Y, 1080, 10);
    }

    @Override
    public void draw(Graphics g) {
        // Draw a bright green ground line on top of everything
        g.setColor(Color.GREEN);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(5)); // Thick line
        g.drawLine(0, GROUND_Y, 1080, GROUND_Y);
    }
}