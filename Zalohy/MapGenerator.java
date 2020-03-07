package Render;

import java.util.Random;

public class MapGenerator {

    private Room[][] Rooms;

    private final int[] classicRooms = {1};



    private int roomX;
    private int roomY;

    private int maxY = 2;
    private int maxX = 2;
    private int minY = -2;
    private int minX = -2;

    // current level
    private int floor;

    // basic vars
    private final int maxRooms = 6;
    private final int startingRoom = 1;


    // map orientation
    private int Direction = 0;
    private int previousDirection;
    private Room previousGenRoom;

    private final int directionRight = 1;
    private final int directionLeft = 2;
    private final int directionUp = 3;
    private final int directionDown = 4;

    // booleans for checking if room was already created
    private boolean shop = false;
    private boolean loot = false;



    public Room getRoom(int x, int y) {
        return Rooms[x][y];
    }
    public void setRoom(int x, int y, int id, int type){
        Room room = new Room(id,type);
        Rooms[x][y] = room;
    }
    public void generateMap(){
        // CREATING STARTING ROOM FOR PLAYER/S
        roomX = random(minX,maxX);
        roomY = random(minY,maxY);
        Rooms[roomX][roomY] = new Room(1,Room.STARTROOM);

        for(int i = maxRooms;i == maxRooms;i++){

            nextDirection();
            // Avoiding to create shop/lootbox after starting room
            if (previousGenRoom.getType() != Room.STARTROOM){

                int typeroom = random(2,4);

                if (shop && loot){
                    typeroom = Room.CLASSIC;
                }
                else if (shop){
                    typeroom = random(3,4);
                }
                else if (loot){
                    while(typeroom != 3){
                        typeroom = random(2,4);
                    }
                }

                Room createdroom = new Room(random(0,classicRooms.length-1),typeroom);



            } else {

            }
        }

    }

    private int random(int min, int max){
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }


    private void nextDirection(){
        while(previousDirection != Direction){
            Direction = random(1,4);

        }
        // allowed directions
        boolean right = true;
        boolean left = true;
        boolean up = true;
        boolean down = true;

        if (roomX == minX) left = false;
        if (roomX == maxX) right = false;
        if (roomY == maxY) down = false;
        if (roomY == minY) up = false;


    }
}
