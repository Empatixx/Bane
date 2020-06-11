package cz.Empatix.Render;


import static org.lwjgl.opengl.GL11.*;

public class PathWall extends RoomObject {
    PathWall(TileMap tm){
        super(tm);
        collision=false;
        moveable=false;
        height = 16;
        width = 16;
        cwidth = 16;
        cheight = 16;
        scale = 8;

        facingRight = true;
        // because of scaling image by 8x
        width *= scale;
        height *= scale;
        cwidth *= scale;
        cheight *= scale;
    }
    public void update() {
        setMapPosition();
    }
    @Override
    public void draw() {
            glColor3i(255,255,255);
            glBegin(GL_LINE_LOOP);
            // BOTTOM LEFT
            glVertex2f(position.x+xmap-cwidth/2,position.y+ymap-cheight/2);
            // TOP LEFT
            glVertex2f(position.x+xmap-cwidth/2, position.y+ymap+cheight/2);
            // TOP RIGHT
            glVertex2f(position.x+xmap+cwidth/2, position.y+ymap+cheight/2);
            // BOTTOM RIGHT
            glVertex2f(position.x+xmap+cwidth/2, position.y+ymap-cheight/2);
            glEnd();

            glPointSize(10);
            glColor3i(255,0,0);
            glBegin(GL_POINTS);
            glVertex2f(position.x+xmap,position.y+ymap);
            glEnd();


    }

    @Override
    public void touchEvent() {

    }
}
