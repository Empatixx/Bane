package cz.Empatix.Render.Text;

import org.lwjgl.opengl.GL11;

public class Font {
    private final int mapWidth;
    private final int mapHeight;

    private final int cellHeight;
    private final int cellWidth;

    private final Character[] characters;

    private final int textureId;


    Font(int mapWidth, int mapHeight, int cellWidth, int cellHeight, int textureId) {

        this.mapHeight = mapHeight;
        this.mapWidth = mapWidth;

        this.cellHeight = cellHeight;
        this.cellWidth = cellWidth;

        this.textureId = textureId;

        characters = new Character[mapWidth / cellWidth * mapHeight / cellHeight];


    }
    public int countChars(){
        return characters.length;
    }
    public void setCharacter(Character character, int index){
        characters[index] = character;
    }

    public Character[] getCharacters() {
        return characters;
    }

    public void bindTexture() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D,textureId);
    }
}
