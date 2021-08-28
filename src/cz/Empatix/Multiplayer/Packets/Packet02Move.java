package cz.Empatix.Multiplayer.Packets;

import cz.Empatix.Multiplayer.GameClient;
import cz.Empatix.Multiplayer.GameServer;

public class Packet02Move extends Packet {

    private String username;
    private int x,y;
    private byte movementDirections;

    private final static int UP = 0x1;
    private final static int DOWN = 0x2;
    private final static int LEFT = 0x4;
    private final static int RIGHT = 0x8;

    public Packet02Move(byte[] data){
        super(02);
        String[] dataArray = readData(data).split(",");
        this.username = dataArray[0];
        this.x = Integer.parseInt(dataArray[1]);
        this.y = Integer.parseInt(dataArray[2]);
        this.movementDirections = Byte.parseByte(dataArray[3]);

    }

    public Packet02Move(String username, int x, int y){
        super(02);
        this.username = username;
        this.y = y;
        this.x = x;
    }

    /**
     *
     * @param u - direction up
     * @param d - direction down
     * @param r - direction right
     * @param l - direction left
     */
    public void setMovementDirections(boolean u, boolean d, boolean r, boolean l){
        this.movementDirections = 0;
        if(u) movementDirections |= UP;
        if(d) movementDirections |= DOWN;
        if(l) movementDirections |= LEFT;
        if(r) movementDirections |= RIGHT;
    }

    @Override
    public void writeData(GameClient client) {
        client.sendData(getData());
    }

    @Override
    public byte[] getData() {
        return ("02" + this.username + "," + x + "," + y + ","+movementDirections).getBytes();
    }

    @Override
    public void writeData(GameServer server) {
        server.sendDataToAllClients(getData());
    }
    public String getUsername(){return username;}

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    public boolean isMovingUp(){
        return ((int)movementDirections & UP) == UP;
    }
    public boolean isMovingDown(){
        return ((int)movementDirections & DOWN) == DOWN;
    }
    public boolean isMovingLeft(){
        return ((int)movementDirections & LEFT) == LEFT;
    }
    public boolean isMovingRight(){
        return ((int)movementDirections & RIGHT) == RIGHT;
    }
}
