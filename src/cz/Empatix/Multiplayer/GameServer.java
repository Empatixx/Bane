package cz.Empatix.Multiplayer;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Java.Random;
import cz.Empatix.Render.Hud.Minimap.MiniMap;
import cz.Empatix.Render.TileMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class GameServer {
    private Server server;

    private List<PlayerMP> connectedPlayers;
    private List<PlayerReady> readyCheckPlayers;
    private GunsManagerMP gunsManager;
    private ItemManagerMP itemManager;
    private EnemyManagerMP enemyManagerMP;
    private ArtefactManagerMP artefactManager;
    private TileMap tileMap;
    private MiniMap map;
    private GunsManagerMP.GunUpgradesCache upgrades;
    private ACKManager ackManager;

    private int gameState;

    private final static int MAX_PLAYERS = 2;

    private static MPStatistics mpStatistics;

    public MPStatistics getMpStatistics() {
        return mpStatistics;
    }
    private AtomicBoolean randomInit;

    public static class PlayerReady{
        private String username;
        private boolean ready;
        public boolean isThisPlayer(PlayerMP p){return username.equalsIgnoreCase(p.getUsername());}
        public boolean isThisPlayer(String username){return username.equalsIgnoreCase(this.username);}
        public boolean isReady(){return ready;};
        public void setReady(boolean ready) {
            this.ready = ready;
        }
        private PlayerReady(String username){
            this.username = username;
        }
    }

    public GameServer() {
        randomInit = new AtomicBoolean();
        randomInit.set(false);
        new Thread("Server-Logic") {
            @Override
            public void run() {
                Random.init();
                randomInit.set(true);
                long lastTime = System.nanoTime();
                long timer = System.currentTimeMillis();
                final double ns = 1000000000.0 / 60.0;

                double delta = 0;

                // UPS  counter
                int updates = 0;

                gameState = GameStateManager.PROGRESSROOM;

                while (MultiplayerManager.multiplayer) {
                    long now = System.nanoTime();
                    delta += (now - lastTime) / ns;
                    lastTime = now;
                    boolean apdPacket = false; // all players dead
                    while (delta >= 1) {
                        if (gameState == GameStateManager.INGAME) {
                            itemManager.update();
                            ItemManagerMP.InteractionAcknowledge[] interAck = itemManager.checkDropInteractions();
                            synchronized (connectedPlayers) {
                                for (PlayerMP player : connectedPlayers) {
                                    player.update();
                                    Network.MovePlayer movePlayer = new Network.MovePlayer();
                                    movePlayer.idPlayer = player.getIdConnection();
                                    movePlayer.x = player.getX();
                                    movePlayer.y = player.getY();
                                    movePlayer.up = player.isMovingUp();
                                    movePlayer.down = player.isMovingDown();
                                    movePlayer.right = player.isMovingRight();
                                    movePlayer.left = player.isMovingLeft();
                                    movePlayer.time = System.nanoTime();
                                    server.sendToAllUDP(movePlayer);
                                }
                            }
                            tileMap.updateCurrentRoom();
                            tileMap.updateObjects();
                            tileMap.checkObjectsInteractions(interAck);

                            gunsManager.updatePlayerLocations();
                            gunsManager.shoot();
                            gunsManager.update();

                            enemyManagerMP.update();
                            ArrayList<Enemy> enemies = EnemyManagerMP.getInstance().getEnemies();
                            gunsManager.checkCollisions(enemies);
                            synchronized (connectedPlayers) {
                                for (PlayerMP player : connectedPlayers) {
                                    player.checkCollision(enemies);
                                }
                            }
                            artefactManager.update();
                            mpStatistics.sentPackets();
                            synchronized (connectedPlayers) {
                                for (PlayerMP player : connectedPlayers) {
                                    Network.PlayerInfo playerInfo = new Network.PlayerInfo();
                                    playerInfo.username = player.getUsername();
                                    playerInfo.coins = (short) player.getCoins();
                                    playerInfo.health = (byte) player.getHealth();
                                    playerInfo.maxHealth = (byte) player.getMaxHealth();
                                    playerInfo.armor = (byte) player.getArmor();
                                    playerInfo.maxArmor = (byte) player.getMaxArmor();
                                    server.sendToAllUDP(playerInfo);
                                }
                            }
                            boolean allDead = true;
                            synchronized (connectedPlayers) {
                                for (Player player : connectedPlayers) {
                                    if (!player.isDead()) {
                                        allDead = false;
                                        break;
                                    }
                                }
                            }
                            if (allDead && !apdPacket) {
                                Network.AllPlayersDeath allPlayersDeath = new Network.AllPlayersDeath();
                                Server server = MultiplayerManager.getInstance().server.getServer();
                                server.sendToAllTCP(allPlayersDeath);

                                apdPacket = true;
                            }
                            if (apdPacket) {
                                boolean allReady = true;
                                synchronized (readyCheckPlayers) {
                                    for (PlayerReady ready : readyCheckPlayers) {
                                        if (!ready.isReady()) {
                                            allReady = false;
                                        }
                                    }
                                }
                                if (allReady) {
                                    tileMap.loadProgressRoom();
                                    synchronized (connectedPlayers) {
                                        for (PlayerMP p : connectedPlayers) {
                                            p.setPosition(tileMap.getPlayerStartX(), tileMap.getPlayerStartY());
                                            p.reset(); // reseting collisions
                                        }
                                    }
                                    artefactManager = null;
                                    gunsManager = null;
                                    itemManager = null;
                                    enemyManagerMP = null;
                                    gameState = GameStateManager.PROGRESSROOM;
                                    synchronized (readyCheckPlayers) {
                                        for (PlayerReady ready : readyCheckPlayers) {
                                            ready.setReady(false);
                                        }
                                    }
                                    apdPacket = false;
                                }
                            }
                        } else {
                            synchronized (connectedPlayers) {
                                for (PlayerMP player : connectedPlayers) {
                                    player.update();
                                    Network.MovePlayer movePlayer = new Network.MovePlayer();
                                    movePlayer.idPlayer = player.getIdConnection();
                                    movePlayer.x = player.getX();
                                    movePlayer.y = player.getY();
                                    movePlayer.up = player.isMovingUp();
                                    movePlayer.down = player.isMovingDown();
                                    movePlayer.right = player.isMovingRight();
                                    movePlayer.left = player.isMovingLeft();
                                    movePlayer.time = System.nanoTime();
                                    server.sendToAllUDP(movePlayer);
                                }
                            }
                            boolean allReady = true;
                            synchronized (readyCheckPlayers) {
                                for (PlayerReady ready : readyCheckPlayers) {
                                    if (!ready.isReady()) {
                                        allReady = false;
                                    }
                                }
                            }
                            if (connectedPlayers.size() == 0) allReady = false;
                            if (allReady) {
                                PlayerMP[] players;
                                synchronized (connectedPlayers) {
                                    players = new PlayerMP[connectedPlayers.size()];
                                    // reseting health/armor, speed movement etc.
                                    for (int i = 0; i < players.length; i++) {
                                        players[i] = connectedPlayers.get(i);
                                        players[i].reset();
                                        connectedPlayers.set(i, players[i]);
                                    }
                                }
                                tileMap.setPlayers(players);
                                artefactManager = new ArtefactManagerMP(tileMap, players);
                                gunsManager = new GunsManagerMP(tileMap, players, upgrades);
                                itemManager = new ItemManagerMP(tileMap, gunsManager, artefactManager, players);
                                enemyManagerMP = new EnemyManagerMP(players, tileMap);
                                tileMap.loadMap();
                                mpStatistics = new MPStatistics();
                                synchronized (connectedPlayers) {
                                    for (PlayerMP p : connectedPlayers) {
                                        if (p != null) {
                                            p.setPosition(tileMap.getPlayerStartX(), tileMap.getPlayerStartY());
                                            mpStatistics.addPlayer(p.getUsername());
                                        }
                                    }
                                }
                                gameState = GameStateManager.INGAME;
                                synchronized (readyCheckPlayers) {
                                    for (PlayerReady ready : readyCheckPlayers) {
                                        ready.setReady(false);
                                    }
                                }
                            }
                        }
                        updates++;
                        delta--;
                    }

                    if (System.currentTimeMillis() - timer > 1000) {
                        timer += 1000;
                        //System.out.print("UPS SERVER: "+updates+"\n");
                        updates = 0;
                    }
                }
            }
        }.start();
        connectedPlayers = Collections.synchronizedList(new ArrayList<>());
        readyCheckPlayers = Collections.synchronizedList(new ArrayList<>());
        map = new MiniMap(true);
        tileMap = new TileMap(64, map, 2);
        tileMap.loadTilesMP("Textures\\tileset64.tga");
        while(!randomInit.get());
        tileMap.loadProgressRoom();

        ackManager = new ACKManager();
        upgrades = new GunsManagerMP.GunUpgradesCache();

        server = new Server(16384, 4096);
        Network.register(server);
        //Log.DEBUG();

        server.start();
        try {
            server.bind(54555, 54777);
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.addListener(new Listener.ThreadedListener(new Listener() {

            @Override
            public void disconnected(Connection connection) {
                Network.Disconnect disconnect = new Network.Disconnect();
                disconnect.idPlayer = connection.getID();
                server.sendToAllTCP(disconnect);
                if (gameState == GameStateManager.INGAME) {
                    synchronized (connectedPlayers){
                        for (PlayerMP player : connectedPlayers) {
                            if (player.getIdConnection() == connection.getID()) {
                                player.setDead();
                                tileMap.clearEnemiesInPlayersRoom(player);
                            }
                        }
                    }
                }
                handleDisconnect(connection);
            }

            public void received(Connection connection, Object object) {
                if (object instanceof Network.Join) {
                    Network.Join joinPacket = (Network.Join) object;
                    String packetUsername = joinPacket.username;
                    // players cant join if player is already ingame
                    if (gameState == GameStateManager.INGAME) {
                        Network.CanJoin canJoin = new Network.CanJoin();
                        canJoin.can = false;
                        connection.sendTCP(canJoin); // sending packet that player can't join
                        connection.close();
                        return;
                    }
                    // server is full
                    if (connectedPlayers.size() >= MAX_PLAYERS) {
                        Network.CanJoin canJoin = new Network.CanJoin();
                        canJoin.can = false;
                        connection.sendTCP(canJoin); // sending packet that player can't join
                        connection.close();
                        return;
                    }
                    // player is already connected
                    for (PlayerMP player : connectedPlayers) {
                        if (player.getUsername().equalsIgnoreCase(packetUsername)) {
                            Network.CanJoin canJoin = new Network.CanJoin();
                            canJoin.can = false;
                            connection.sendTCP(canJoin); // sending packet that player can't join
                            connection.close();
                            return;
                        }
                    }
                    Network.CanJoin canJoin = new Network.CanJoin();
                    canJoin.can = true;
                    canJoin.idPlayer = connection.getID();
                    connection.sendTCP(canJoin); // sending packet that player can join
                    handleJoin(joinPacket, connection);
                } else if (object instanceof Network.RequestForPlayers) {
                    Network.RequestForPlayers request = (Network.RequestForPlayers) object;
                    for (PlayerMP player : connectedPlayers) {
                        if (!player.getUsername().equalsIgnoreCase(request.exceptUsername)) {
                            Network.AddPlayer addPlayer = new Network.AddPlayer();
                            addPlayer.username = player.getUsername();
                            addPlayer.idPlayer = player.getIdConnection();
                            connection.sendTCP(addPlayer);
                        }
                    }
                } else if (object instanceof Network.MovePlayerInput) {
                    handleMove((Network.MovePlayerInput) object);
                } else if (object instanceof Network.NumUpgrades) {
                    upgrades.handleNumUpgradesPacket((Network.NumUpgrades) object);
                } else if (object instanceof Network.NumUpgradesUpdate) {
                    upgrades.handleNumUpgradesPacketUpdate((Network.NumUpgradesUpdate) object);
                } else if (object instanceof Network.MouseCoords) {
                    if (gameState == GameStateManager.INGAME) {
                        gunsManager.handleMouseCoords((Network.MouseCoords) object);
                    }
                } else if (object instanceof Network.Reload) {
                    gunsManager.reload(((Network.Reload) object).username);
                } else if (object instanceof Network.SwitchWeaponSlot) {
                    gunsManager.switchWeaponSlot((Network.SwitchWeaponSlot) object);
                } else if (object instanceof Network.PlayerDropWeapon) {
                    gunsManager.handleDropWeaponPacket((Network.PlayerDropWeapon) object);
                } else if (object instanceof Network.Ready) {
                    server.sendToAllTCP(object);
                    synchronized (readyCheckPlayers) {
                        for (PlayerReady pready : readyCheckPlayers) {
                            Network.Ready ready = (Network.Ready) object;
                            if (pready.isThisPlayer(ready.username)) pready.setReady(ready.state);
                        }
                    }
                } else if (object instanceof Network.DropInteract) {
                    itemManager.handleDrolpInteractPacket((Network.DropInteract) object);
                } else if (object instanceof Network.ObjectInteract) {
                    tileMap.handleObjectInteractPacket((Network.ObjectInteract) object);
                } else if (object instanceof Network.StopShooting) {
                    gunsManager.stopShooting(((Network.StopShooting) object).username);
                } else if (object instanceof Network.StartShooting) {
                    gunsManager.startShooting(((Network.StartShooting) object).username);
                } else if (object instanceof Network.ArtefactActivate) {
                    artefactManager.activate((((Network.ArtefactActivate) object).username), (Network.ArtefactActivate) object);
                } else if (object instanceof Network.Ping) {
                    connection.sendUDP(object);
                }
            }
        }));
        /*new Thread("Server-ACK") {
            @Override
            public void run() {
                while(true){
                    ackManager.update();
                }
            }
        }.start();*/
    }

    private void handleDisconnect(Connection connection) {
        // player is already connected
        synchronized (connectedPlayers){
            for(int i = 0;i<connectedPlayers.size();i++){
                PlayerMP player = connectedPlayers.get(i);
                if (player.getIdConnection() == connection.getID()) {
                    connectedPlayers.remove(i);
                    synchronized (readyCheckPlayers){
                        for(int j = 0;j<readyCheckPlayers.size();j++){
                            PlayerReady playerReady = readyCheckPlayers.get(j);
                            if(playerReady.isThisPlayer(player)){
                                readyCheckPlayers.remove(i);
                                break;
                            }
                        }
                    }
                    upgrades.removePlayer(player.getUsername());
                    i--;
                }
            }
        }
        synchronized (readyCheckPlayers){
            for(PlayerReady r : readyCheckPlayers){
                r.setReady(false);
            }
        }
    }
    private void handleMove(Network.MovePlayerInput movePlayer) {
        // player is already connceted
        synchronized (connectedPlayers){
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

    }
    private void handleJoin(Network.Join join, Connection connection) {
        PlayerMP playerMP = new PlayerMP(tileMap, join.username);
        playerMP.setIdConnection(connection.getID());


        playerMP.setPosition(tileMap.getPlayerStartX(),tileMap.getPlayerStartY());

        if(join.host){
            connectedPlayers.add(playerMP);
            readyCheckPlayers.add(new PlayerReady(playerMP.getUsername()));
            upgrades.addPlayer(join.username);
        } else {
            for (PlayerMP otherPlayer : connectedPlayers) {
                Network.AddPlayer addPlayer = new Network.AddPlayer();
                addPlayer.username = otherPlayer.getUsername();
                addPlayer.idPlayer = otherPlayer.getIdConnection();
                // send other players to new player
                connection.sendTCP(addPlayer);
            }
            connectedPlayers.add(playerMP);
            readyCheckPlayers.add(new PlayerReady(playerMP.getUsername()));
            upgrades.addPlayer(join.username);
            Network.AddPlayer addPlayer = new Network.AddPlayer();
            addPlayer.username = join.username;
            addPlayer.idPlayer = connection.getID();
            // send new player to others players
            server.sendToAllExceptTCP(connection.getID(),addPlayer);
        }
    }
    public Server getServer() {
        return server;
    }
    public void close(){
        server.close();
        Random.closeMP();
    }
    private static class ACKManager{
        private ArrayList<PacketWaitingACK> packets;
        private ArrayList<PacketWaitingACK> waitingForAdd;
        private int[] confirmedACKs;
        private int totalConfirmedACKs;
        private Lock waitingLock;
        private Lock ackLock;
        ACKManager(){
            packets = new ArrayList<>(300);
            waitingLock = new ReentrantLock();
        }
        public void acknowledged(int id){
            ackLock.lock();
            try{
                confirmedACKs[totalConfirmedACKs] = id;
                totalConfirmedACKs++;
            } finally {
                ackLock.unlock();
            }
        }
        public void add(Object o,int id){
            waitingLock.lock();
            try{
                waitingForAdd.add(new PacketWaitingACK(o,id));
            } finally {
                waitingLock.unlock();
            }
        }
        public void update(){
            waitingLock.lock();
            try{
                packets.addAll(waitingForAdd);
                waitingForAdd.clear();
            } finally {
                waitingLock.unlock();
            }
            ackLock.lock();
            int[] acks;
            try{
                acks = Arrays.copyOf(confirmedACKs,totalConfirmedACKs);
                totalConfirmedACKs = 0;
            } finally {
                ackLock.unlock();
            }
            for(int ackId : acks){
                packets.removeIf(ack -> ack.id == ackId);
            }
            for(PacketWaitingACK p : packets){
                if(p.shouldResend()){
                    MultiplayerManager.getInstance().server.server.sendToAllUDP(p.getPacket());
                }
            }
        }
        private static class PacketWaitingACK{
            private Object packet;
            private int id;
            private long timeSent;
            PacketWaitingACK(Object packet, int id){
                this.id = id;
                this.timeSent = System.nanoTime();
                this.packet = packet;
            }
            boolean shouldResend(){
                return System.nanoTime() - timeSent > 30000;
            }
            public Object getPacket() {
                return packet;
            }
        }
    }
}
