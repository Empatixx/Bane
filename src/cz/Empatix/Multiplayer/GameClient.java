package cz.Empatix.Multiplayer;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.EnemyManager;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.GameState;
import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Gamestates.Multiplayer.InGameMP;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Gamestates.Multiplayer.ProgressRoomMP;
import cz.Empatix.Render.Alerts.AlertManager;

import java.io.IOException;
import java.util.ArrayList;

public class GameClient{
    private final GameStateManager gsm;
    private final Client client;

    private int numPlayers;

    public GameClient(GameStateManager gsm, String ipAdress){
        this.gsm = gsm;
        numPlayers = 1;

        client = new Client();
        client.start();

        client.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                if (object instanceof Network.AddPlayer) {
                    Network.AddPlayer player = (Network.AddPlayer) object;

                    GameState gameState = gsm.getCurrentGamestate();
                    String packetUsername = player.username;

                    if (gameState instanceof ProgressRoomMP) {
                        PlayerMP playerMP = new PlayerMP(((ProgressRoomMP) gameState).tileMap, packetUsername);
                        ((ProgressRoomMP) gameState).player[numPlayers] = playerMP;
                        ((ProgressRoomMP) gameState).playerReadies[numPlayers] = new ProgressRoomMP.PlayerReady(packetUsername);
                        numPlayers++;
                    }
                    else if (gameState instanceof InGameMP) {
                        PlayerMP playerMP = new PlayerMP(((InGameMP) gameState).tileMap, packetUsername);
                        ((InGameMP) gameState).player[numPlayers] = playerMP;
                        numPlayers++;
                    }
                }
                else if (object instanceof Network.Disconnect) {
                    Network.Disconnect packet = (Network.Disconnect) object;

                    GameState gameState = gsm.getCurrentGamestate();
                    numPlayers--;
                    String packetUsername = packet.username;
                    String playerUsername = MultiplayerManager.getInstance().getUsername();
                    if(gameState instanceof ProgressRoomMP){
                        ((PlayerMP)((ProgressRoomMP) gameState).player[numPlayers]).remove();
                        ((ProgressRoomMP) gameState).player[numPlayers] = null;
                        ((ProgressRoomMP) gameState).playerReadies[numPlayers] = null;
                        if(!packetUsername.equalsIgnoreCase(playerUsername)) AlertManager.add(AlertManager.WARNING,packetUsername+" has left the lobby!");
                    }
                    if(gameState instanceof InGameMP){
                        (((InGameMP) gameState).player[numPlayers]).remove();
                        ((InGameMP) gameState).player[numPlayers] = null;
                        if(!packetUsername.equalsIgnoreCase(playerUsername)) AlertManager.add(AlertManager.WARNING,packetUsername+" has left the game!");
                    }
                }
                else if (object instanceof Network.MovePlayer){
                    handleMove((Network.MovePlayer) object);
                }
                else if (object instanceof Network.Ready){
                    boolean state = ((Network.Ready) object).state;

                    GameState gameState = gsm.getCurrentGamestate();
                    String packetUsername = ((Network.Ready) object).username;
                    if(gameState instanceof ProgressRoomMP) {
                        for(ProgressRoomMP.PlayerReady playerReady : ((ProgressRoomMP) gameState).playerReadies){
                            if(playerReady == null) continue;
                            if(playerReady.getUsername().equalsIgnoreCase(packetUsername)){
                                playerReady.setReady(state);
                            }
                        }
                    }
                    if(state){
                        String playerUsername = MultiplayerManager.getInstance().getUsername();
                        if(!packetUsername.equalsIgnoreCase(playerUsername)) AlertManager.add(AlertManager.INFORMATION,packetUsername+" is ready!");
                    }
                }
                else if (object instanceof Network.TransferRoomMap){
                    Network.TransferRoomMap map = (Network.TransferRoomMap) object;
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        ((InGameMP)gameState).tileMap.handleRoomMapPacket(map);
                    }
                }
                else if (object instanceof Network.TransferRoom){
                    Network.TransferRoom room = (Network.TransferRoom) object;
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        ((InGameMP)gameState).tileMap.handleRoomPacket(room);
                    }
                }
                else if(object instanceof Network.MapLoaded){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        ((InGameMP)gameState).mapLoaded = true;
                    }
                }
                else if(object instanceof Network.AddBullet){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        ((InGameMP)gameState).gunsManager.handleAddBulletPacket((Network.AddBullet) object);
                    }
                }
                else if(object instanceof Network.HitBullet){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        ((InGameMP)gameState).gunsManager.handleHitBulletPacket((Network.HitBullet) object);
                    }
                }
                else if(object instanceof Network.WeaponInfo){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        ((InGameMP)gameState).gunsManager.handleWeaponInfoPacket((Network.WeaponInfo) object);
                    }
                }
                else if(object instanceof Network.DropItem){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        ((InGameMP)gameState).itemManager.handleDropItemPacket((Network.DropItem) object);
                    }
                }
                else if(object instanceof Network.RemoveItem){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        ((InGameMP)gameState).itemManager.handleRemoveItemPacket((Network.RemoveItem) object);
                    }
                }
                else if (object instanceof Network.MoveBullet){
                    handleMove((Network.MoveBullet) object);
                }
                else if(object instanceof Network.AddEnemy){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        ((InGameMP)gameState).enemyManager.addEnemyPacket((Network.AddEnemy) object);
                        ((InGameMP)gameState).tileMap.lockRoom();
                    }
                }
                else if (object instanceof Network.MoveEnemy){
                    handleMove((Network.MoveEnemy) object);
                }
                else if (object instanceof Network.SwitchWeaponSlot){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        ((InGameMP)gameState).gunsManager.handleSwitchWeaponPacket((Network.SwitchWeaponSlot)object);
                    }
                }
                else if (object instanceof Network.PlayerDropWeapon){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        ((InGameMP)gameState).gunsManager.handleDropPlayerWeaponPacket((Network.PlayerDropWeapon)object);
                    }
                }
                else if (object instanceof Network.DropWeapon){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        ((InGameMP)gameState).gunsManager.handleDropWeaponPacket((Network.DropWeapon)object);
                    }
                }
                else if (object instanceof Network.ObjectInteract){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        ((InGameMP)gameState).itemManager.handleObjectInteract((Network.ObjectInteract)object);
                    }
                    System.out.println("RECEIVED BACK");
                }
            }
        });

        Network.register(client);

        try {
            client.connect(5000, "127.0.0.1", 54555, 54777);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Client getClient() {
        return client;
    }

    public int getTotalPlayers() {
        return numPlayers;
    }
    public void handleMove(Network.MovePlayer movePlayerPacket){
        GameState gameState = gsm.getCurrentGamestate();
        if(gameState instanceof ProgressRoomMP) {
            Player[] players = ((ProgressRoomMP) gameState).player;
            // if gamestate have not been loaded yet, cancel
            if(players == null) return;
            for (Player p : players) {
                if (p != null) {
                    if (p instanceof PlayerMP) {
                        if (((PlayerMP) p).getUsername().equalsIgnoreCase(movePlayerPacket.username)) {
                            p.setPosition(movePlayerPacket.x, movePlayerPacket.y);
                            if(!((PlayerMP) p).isOrigin()){
                                p.setDown(movePlayerPacket.down);
                                p.setUp(movePlayerPacket.up);
                                p.setRight(movePlayerPacket.right);
                                p.setLeft(movePlayerPacket.left);
                            }
                        }
                    }
                }
            }
        } else if(gameState instanceof InGameMP) {
            Player[] players = ((InGameMP) gameState).player;
            // if gamestate have not been loaded yet, cancel
            if(players == null) return;
            for (Player p : players) {
                if (p != null) {
                    if (p instanceof PlayerMP) {
                        if (((PlayerMP) p).getUsername().equalsIgnoreCase(movePlayerPacket.username)) {
                            p.setPosition(movePlayerPacket.x, movePlayerPacket.y);
                            if(!((PlayerMP) p).isOrigin()){
                                p.setDown(movePlayerPacket.down);
                                p.setUp(movePlayerPacket.up);
                                p.setRight(movePlayerPacket.right);
                                p.setLeft(movePlayerPacket.left);
                            }
                        }
                    }
                }
            }
        }
    }
    public void handleMove(Network.MoveEnemy moveEnemyPacket){
        GameState gameState = gsm.getCurrentGamestate();
        if(gameState instanceof InGameMP) {
            EnemyManager enemyManager = EnemyManager.getInstance();
            ArrayList<Enemy> enemies = enemyManager.getEnemies();
            // if gamestate have not been loaded yet, cancel
            for (Enemy e : enemies) {
                if (e.idEnemy == moveEnemyPacket.id) {
                    e.setPosition(moveEnemyPacket.x, moveEnemyPacket.y);
                    e.setDown(moveEnemyPacket.down);
                    e.setUp(moveEnemyPacket.up);
                    e.setRight(moveEnemyPacket.right);
                    e.setLeft(moveEnemyPacket.left);
                    e.setFacingRight(moveEnemyPacket.facingRight);
                }
            }
        }
    }
    public void handleMove(Network.MoveBullet movePacket) {
        GameState gameState = gsm.getCurrentGamestate();
        if (gameState instanceof InGameMP) {
            ((InGameMP) gameState).gunsManager.handleBulletMovePacket(movePacket);
        }
    }
    public void close(){
        client.close();
    }

    public void setNumPlayers(int numPlayers) {
        this.numPlayers = numPlayers;
    }
}
