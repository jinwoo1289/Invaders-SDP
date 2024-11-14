package entity;

import engine.DrawManager.SpriteType;
import engine.Drawable;

import java.awt.*;


public class Block extends Entity implements Drawable {
    /**
     * Constructor, establishes the entity's generic properties.
     *
     * @param positionX Initial position of the entity in the X axis.
     * @param positionY Initial position of the entity in the Y axis.
     */
    public Block(int positionX, int positionY) {
        super(positionX, positionY, 20 * 2, 7 * 2, Color.WHITE);
        this.spriteType = SpriteType.Block;
    }
    public int getPositionX() {return positionX;}
    public int getPositionY() {return positionY;}

}