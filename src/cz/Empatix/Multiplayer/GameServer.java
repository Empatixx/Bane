package cz.Empatix.Multiplayer;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Render.Hud.Minimap.MiniMap;
import cz.Empatix.Render.TileMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameServer {
    private final Server server;

    private final List<PlayerMP> connectedPlayers;
    private GunsManagerMP gunsManager;
    private ItemManagerMP itemManager;
    private EnemyManagerMP enemyManagerMP;
    private ArtefactManagerMP artefactManager;
    private TileMap tileMap;
    private MiniMap map;

    private int gameState;

    int changeGamestateConfirms;

    public GameServer() {
        connectedPlayers = Collections.synchronizedList(new ArrayList<>());
        map = new MiniMap(true);
        tileMap = new TileMap(64,map,2);
        tileMap.loadTilesMP("Textures\\tileset64.tga");
        tileMap.loadProgressRoom();

        changeGamestateConfirms = 0;

        server = new Server();
        server.start();
        try {
            server.bind(54555, 54777);
        } catch (IOException e) {
            e.printStackTrace();
        }

        server.addListener(new Listener() {
            public void received(Connection connection, Object object) {
                if (object instanceof Network.Join) {
                    Network.Join joinPacket = (Network.Join) object;

                    String packetUsername = joinPacket.username;

                    // player is already connected
                    for (PlayerMP player : connectedPlayers) {
                        if (player.getUsername().equalsIgnoreCase(packetUsername)) {
                            close();
                            return;
                        }
                    }

                    handleJoin(joinPacket, connection);
                }
                else if (object instanceof Network.Disconnect) {
                    Network.Disconnect disconnectPacket = (Network.Disconnect) object;
                    handleDisconnect(disconnectPacket);
                }
                else if (object instanceof Network.RequestForPlayers) {
                    Network.RequestForPlayers request = (Network.RequestForPlayers) object;
                    for(PlayerMP player : connectedPlayers){
                        if(!player.getUsername().equalsIgnoreCase(request.exceptUsername)){
                            Network.AddPlayer addPlayer = new Network.AddPlayer();
                            addPlayer.username = player.getUsername();
                            connection.sendTCP(addPlayer);
                        }
                    }
                }
                else if (object instanceof Network.MovePlayerInput) {
                    handleMove((Network.MovePlayerInput) object);
                }
                else if(object instanceof Network.ConfirmChangeGS){
                    changeGamestateConfirms++;
                    if(changeGamestateConfirms == connectedPlayers.size()){
                        PlayerMP[] players = connectedPlayers.toArray(new PlayerMP[connectedPlayers.size()]);
                        tileMap.setPlayers(players);
                        map = new MiniMap(true);
                        artefactManager = new ArtefactManagerMP(tileMap,players);
                        gunsManager = new GunsManagerMP(tileMap,players);
                        itemManager = new ItemManagerMP(tileMap,gunsManager,artefactManager,players);
                        EnemyManagerMP.init(players,tileMap);
                        enemyManagerMP = EnemyManagerMP.getInstance();
                        tileMap.loadMap();
                        for(Player p : connectedPlayers){
                            if(p != null) p.setPosition(tileMap.getPlayerStartX(),tileMap.getPlayerStartY());
                        }
                        gameState = GameStateManager.INGAME;
                    }
                } else if(object instanceof Network.MouseCoords){
                    gunsManager.handleMouseCoords((Network.MouseCoords) object);
                }
                else if(object instanceof Network.Reload){
                    gunsManager.reload(((Network.Reload) object).username);
                }
                else if(object instanceof Network.SwitchWeaponSlot){
                    gunsManager.switchWeaponSlot((Network.SwitchWeaponSlot) object);
                }
                else if(object instanceof Network.PlayerDropWeapon){
                    gunsManager.handleDropWeaponPacket((Network.PlayerDropWeapon)object);
                }
                else if (object instanceof Network.Ready){
                    server.sendToAllTCP(object);
                }
                else if (object instanceof Network.ObjectInteract){
                    itemManager.handleObjectInteractPacket((Network.ObjectInteract)object);
                }
                else if (object instanceof Network.StopShooting){
                    gunsManager.stopShooting(((Network.StopShooting) object).username);
                }
                else if (object instanceof Network.StartShooting){
                    gunsManager.startShooting(((Network.StartShooting) object).username);
                }
                else if (object instanceof Network.ArtefactActivate){
                    artefactManager.activate((((Network.ArtefactActivate) object).username));
                }
            }
        });

        Network.register(server);

        new Thread(){
            @Override
            public void run() {
                super.run();
                long lastTime = System.nanoTime();
                long timer = System.currentTimeMillis();
                final double ns = 1000000000.0 / 60.0;

                double delta = 0;

                // UPS  counter
                int updates = 0;

                while (MultiplayerManager.multiplayer) {
                    long now = System.nanoTime();
                    delta += (now-lastTime) / ns;
                    lastTime = now;

                    while (delta >= 1){
                             if(gameState == GameStateManager.INGAME){
                                 itemManager.update();
                                 for(Player player: connectedPlayers){
                                     player.update();
                                 }
                                 tileMap.updateObjects();
                                 tileMap.updateCurrentRoom();

                                 gunsManager.updatePlayerLocations();
                                 gunsManager.shoot();
                                 gunsManager.update();

                                 enemyManagerMP.update();
                                 ArrayList<Enemy> enemies = EnemyManagerMP.getInstance().getEnemies();
                                 gunsManager.checkCollisions(enemies);
                                 for(Player player : connectedPlayers){
                                    player.checkCollision(enemies);
                                 }
                                 artefactManager.update();
                             } else {
                                 for(Player player: connectedPlayers){
                                     player.update();
                                 }
                             }
                             updates++;
                             if(delta < 2){
                                 for(PlayerMP player: connectedPlayers){
                                     Network.MovePlayer movePlayer = new Network.MovePlayer();
                                     movePlayer.username = player.getUsername();
                                     movePlayer.x = player.getX();
                                     movePlayer.y = player.getY();
                                     movePlayer.up = player.isMovingUp();
                                     movePlayer.down = player.isMovingDown();
                                     movePlayer.right = player.isMovingRight();
                                     movePlayer.left = player.isMovingLeft();
                                     movePlayer.time = System.nanoTime();
                                     server.sendToAllUDP(movePlayer);

                                     Network.PlayerInfo playerInfo = new Network.PlayerInfo();
                                     playerInfo.username = player.getUsername();
                                     playerInfo.coins = player.getCoins();
                                     playerInfo.health = player.getHealth();
                                     playerInfo.maxHealth = player.getMaxHealth();
                                     playerInfo.armor = player.getArmor();
                                     playerInfo.maxArmor = player.getMaxArmor();
                                     server.sendToAllUDP(playerInfo);
                                 }
                             }
                             delta--;
                    }

                    if (System.currentTimeMillis() - timer > 1000){
                        timer += 1000;
                        System.out.print("UPS SERVER: "+updates+"\n");
                        updates = 0;
                    }
                }
            }
        }.start();
    }

    private void handleDisconnect(Network.Disconnect disconnectPacket) {
        String packetUsername = disconnectPacket.username;

        // player is already connceted
        for(int i = 0;i<connectedPlayers.size();i++){
            PlayerMP player = connectedPlayers.get(i);
            if (player.getUsername().equalsIgnoreCase(packetUsername)) {
                connectedPlayers.remove(i);
                i--;
            }
        }
        server.sendToAllTCP(disconnectPacket);

    }
    private void handleMove(Network.MovePlayerInput movePlayer) {
        // player is already connceted
        for(int i = 0;i<connectedPlayers.size();i++){
            PlayerMP player = connectedPlayers.get(i);
            if (player.getUsername().equalsIgnoreCase(movePlayer.username)) {
                player.setUp(movePlayer.up);
                player.setDown(movePlayer.down);
                player.setRight(movePlayer.right);
                player.setLeft(movePlayer.left);
            }
        }

    }
    private void handleJoin(Network.Join join, Connection connection) {
        PlayerMP playerMP = new PlayerMP(tileMap, join.username);

        playerMP.setPosition(tileMap.getPlayerStartX(),tileMap.getPlayerStartY());

        if(join.host){
            connectedPlayers.add(playerMP);
        } else {
            for (PlayerMP otherPlayer : connectedPlayers) {
                Network.AddPlayer addPlayer = new Network.AddPlayer();
                addPlayer.username = otherPlayer.getUsername();
                // send other players to new player
                connection.sendTCP(addPlayer);
            }
            connectedPlayers.add(playerMP);
            Network.AddPlayer addPlayer = new Network.AddPlayer();
            addPlayer.username = join.username;
            // send new player to others players
            server.sendToAllExceptTCP(connection.getID(),addPlayer);
        }
    }
    public Server getServer() {
        return server;
    }
    public void close(){
        server.close();
    }


}
