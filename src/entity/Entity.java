package entity;
import block.*;
import input.*;
import graphics.*;
import animation.*;
import audio.*;

import java.awt.*;

abstract class Entity {

    protected int x, y;

    public Entity(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public abstract Rectangle getBounds();

    public abstract void draw(Graphics g);

    public void update(InputManager input) {}
}
