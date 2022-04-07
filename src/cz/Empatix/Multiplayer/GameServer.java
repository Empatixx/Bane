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
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class GameServer {
    private final Server server;

    private final List<PlayerMP> connectedPlayers;
    private final List<PlayerReady> readyCheckPlayers;
    private final ReentrantReadWriteLock playerLock;
    private final ReentrantReadWriteLock readyLock;

    private GunsManagerMP gunsManager;
    private ItemManagerMP itemManager;
    private EnemyManagerMP enemyManagerMP;
    private ArtefactManagerMP artefactManager;
    private final TileMap tileMap;
    private final GunsManagerMP.GunUpgradesCache upgrades;
    private final ACKManager ackManager;
    private final ACKCaching ackCaching;

    private int gameState;

    private final static int MAX_PLAYERS = 2;

    private static MPStatistics mpStatistics;

    public MPStatistics getMpStatistics() {
        return mpStatistics;
    }
    private AtomicBoolean randomInit;

    public static class PlayerReady{
        private int id;
        private boolean ready;
        public boolean isThisPlayer(PlayerMP p){return p.getIdConnection() == id;}
        public boolean isThisPlayer(int id){return this.id == id;}
        public boolean isNotReady(){return !ready;}
        public void setReady(boolean ready) {
            this.ready = ready;
        }
        private PlayerReady(int id){
            this.id = id;
        }
    }

    public GameServer() {
        randomInit = new AtomicBoolean();
        randomInit.set(false);

        ackManager = new ACKManager();
        ackCaching = new ACKCaching();

        server = new Server(16384, 4096);
        Network.register(server);

        new Thread("Server") {
            @Override
            public void run() {
                Random.init();
                randomInit.set(true);
                long lastTime = System.nanoTime();
                long startACK = System.nanoTime();
                long timer = System.currentTimeMillis();
                final double ns = 1000000000.0 / 60.0;

                double delta = 0;

                // UPS  counter
                int updates = 0;

                gameState = GameStateManager.PROGRESSROOM;

                boolean apdPacket = false; // all players dead
                while (MultiplayerManager.multiplayer) {
                    try {
                        server.update(250);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(System.nanoTime() - startACK > 10_000_000){
                        startACK+=10_000_000L;
                        ackManager.update();
                        ackCaching.update();
                    }
                    long now = System.nanoTime();
                    delta += (now - lastTime) / ns;
                    lastTime = now;
                    while (delta >= 1) {
                        if(delta < 2){
                            playerLock.readLock().lock();
                            try{
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
                                    server.sendToAllUDP(movePlayer);
                                }
                            } finally {
                                playerLock.readLock().unlock();
                            }
                        }
                        if (gameState == GameStateManager.INGAME) {
                            itemManager.update();
                            ItemManagerMP.InteractionAcknowledge[] interAck = itemManager.checkDropInteractions();
                            tileMap.updateCurrentRoom();
                            tileMap.updateObjects();
                            tileMap.checkObjectsInteractions(interAck);

                            gunsManager.updatePlayerLocations();
                            gunsManager.shoot();
                            gunsManager.update();

                            enemyManagerMP.update();
                            ArrayList<Enemy> enemies = EnemyManagerMP.getInstance().getEnemies();
                            gunsManager.checkCollisions(enemies);
                            playerLock.writeLock().lock();
                            try{
                                for (PlayerMP player : connectedPlayers) {
                                    player.checkCollision(enemies);
                                }
                            } finally {
                                playerLock.writeLock().unlock();
                            }
                            artefactManager.update();
                            mpStatistics.sentPackets();
                            playerLock.readLock().lock();
                            try{
                                for (PlayerMP player : connectedPlayers) {
                                    Network.PlayerInfo playerInfo = new Network.PlayerInfo();
                                    playerInfo.idPlayer = player.getIdConnection();
                                    playerInfo.coins = (short) player.getCoins();
                                    playerInfo.health = (byte) player.getHealth();
                                    playerInfo.maxHealth = (byte) player.getMaxHealth();
                                    playerInfo.armor = (byte) player.getArmor();
                                    playerInfo.maxArmor = (byte) player.getMaxArmor();
                                    server.sendToAllUDP(playerInfo);
                                }
                            } finally {
                                playerLock.readLock().unlock();
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
                                readyLock.readLock().lock();
                                try{
                                    for (PlayerReady ready : readyCheckPlayers) {
                                        if (ready.isNotReady()) {
                                            allReady = false;
                                            break;
                                        }
                                    }
                                } finally {
                                    readyLock.readLock().unlock();
                                }
                                if (allReady) {
                                    tileMap.loadProgressRoom();
                                    playerLock.writeLock().lock();
                                    try{
                                        for (PlayerMP p : connectedPlayers) {
                                            p.setPosition(tileMap.getPlayerStartX(), tileMap.getPlayerStartY());
                                            p.reset(); // reseting collisions
                                        }
                                    } finally {
                                        playerLock.writeLock().unlock();
                                    }
                                    artefactManager = null;
                                    gunsManager = null;
                                    itemManager = null;
                                    enemyManagerMP = null;
                                    gameState = GameStateManager.PROGRESSROOM;
                                    readyLock.writeLock().lock();
                                    try{
                                        for (PlayerReady ready : readyCheckPlayers) {
                                            ready.setReady(false);
                                        }
                                    } finally {
                                        readyLock.writeLock().unlock();
                                    }
                                    apdPacket = false;
                                }
                            }
                        } else {
                            boolean allReady = true;
                            readyLock.readLock().lock();
                            int size;
                            try {
                                size = connectedPlayers.size();
                                for (PlayerReady ready : readyCheckPlayers) {
                                    if (ready.isNotReady()) {
                                        allReady = false;
                                        break;
                                    }
                                }
                            } finally {
                                readyLock.readLock().unlock();
                            }
                            //TODO: for less then 1 player not//if (size <= 1) allReady = false;
                            if (allReady) {
                                PlayerMP[] players;
                                playerLock.writeLock().lock();
                                try{
                                    players = new PlayerMP[connectedPlayers.size()];
                                    // reseting health/armor, speed movement etc.
                                    for (int i = 0; i < players.length; i++) {
                                        players[i] = connectedPlayers.get(i);
                                        players[i].reset();
                                        connectedPlayers.set(i, players[i]);
                                    }
                                } finally {
                                    playerLock.writeLock().unlock();
                                }
                                tileMap.setPlayers(players);
                                artefactManager = new ArtefactManagerMP(tileMap, players);
                                gunsManager = new GunsManagerMP(tileMap, players, upgrades);
                                itemManager = new ItemManagerMP(tileMap, gunsManager, artefactManager, players);
                                enemyManagerMP = new EnemyManagerMP(players, tileMap);
                                tileMap.loadMap();
                                mpStatistics = new MPStatistics();
                                playerLock.writeLock().lock();
                                try{
                                    for (PlayerMP p : connectedPlayers) {
                                        if (p != null) {
                                            p.setPosition(tileMap.getPlayerStartX(), tileMap.getPlayerStartY());
                                            mpStatistics.addPlayer(p.getUsername(),p.getIdConnection());
                                        }
                                    }
                                } finally {
                                    playerLock.writeLock().unlock();
                                }
                                gameState = GameStateManager.INGAME;
                                readyLock.writeLock().lock();
                                try{
                                    for (PlayerReady ready : readyCheckPlayers) {
                                        ready.setReady(false);
                                    }
                                }finally {
                                    readyLock.writeLock().unlock();
                                }
                            }
                        }
                        updates++;
                        delta--;
                    }

                    if (System.currentTimeMillis() - timer > 1000) {
                        timer += 1000;
                        System.out.print("UPS SERVER: "+updates+"\n");
                        updates = 0;
                    }
                }
            }
        }.start();
        connectedPlayers = new ArrayList<>();
        readyCheckPlayers = new ArrayList<>();
        playerLock = new ReentrantReadWriteLock();
        readyLock = new ReentrantReadWriteLock();
        MiniMap map = new MiniMap(true);
        tileMap = new TileMap(64, map, 2);
        tileMap.loadTilesMP("Textures\\tileset64.tga");
        while(!randomInit.get());
        tileMap.loadProgressRoom();

        upgrades = new GunsManagerMP.GunUpgradesCache();

        //Log.DEBUG();

        //server.start();
        try {
            server.bind(54555, 54777);
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.addListener(new Listener() {

            @Override
            public void disconnected(Connection connection) {
                Network.Disconnect disconnect = new Network.Disconnect();
                disconnect.idPlayer = connection.getID();
                server.sendToAllTCP(disconnect);
                if (gameState == GameStateManager.INGAME) {
                    playerLock.readLock().lock();
                    try{
                        for (PlayerMP player : connectedPlayers) {
                            if (player.getIdConnection() == connection.getID()) {
                                player.setDead();
                                tileMap.clearEnemiesInPlayersRoom(player);
                                break;
                            }
                        }
                    } finally {
                        playerLock.readLock().unlock();
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
                    playerLock.readLock().lock();
                    int size;
                    try{
                        size = connectedPlayers.size();
                    } finally {
                        playerLock.readLock().unlock();
                    }
                    if (size >= MAX_PLAYERS) {
                        Network.CanJoin canJoin = new Network.CanJoin();
                        canJoin.can = false;
                        connection.sendTCP(canJoin); // sending packet that player can't join
                        connection.close();
                        return;
                    }
                    // player is already connected
                    playerLock.readLock().lock();
                    try{
                        for (PlayerMP player : connectedPlayers) {
                            if (player.getUsername().equalsIgnoreCase(packetUsername)) {
                                Network.CanJoin canJoin = new Network.CanJoin();
                                canJoin.can = false;
                                connection.sendTCP(canJoin); // sending packet that player can't join
                                connection.close();
                                return;
                            }
                        }
                    } finally {
                        playerLock.readLock().unlock();
                    }
                    Network.CanJoin canJoin = new Network.CanJoin();
                    canJoin.can = true;
                    canJoin.idPlayer = connection.getID();
                    connection.sendTCP(canJoin); // sending packet that player can join
                    handleJoin(joinPacket, connection);
                } else if (object instanceof Network.RequestForPlayers) {
                    Network.RequestForPlayers request = (Network.RequestForPlayers) object;
                    playerLock.readLock().lock();
                    try{
                        for (PlayerMP player : connectedPlayers) {
                            if (player.getIdConnection() != request.exceptIdPlayer) {
                                Network.AddPlayer addPlayer = new Network.AddPlayer();
                                addPlayer.username = player.getUsername();
                                addPlayer.idPlayer = player.getIdConnection();
                                connection.sendTCP(addPlayer);
                            }
                        }
                    } finally {
                        playerLock.readLock().unlock();
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
                    Network.Reload reload = (Network.Reload) object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = ((Network.Reload) object).idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(connection.getID(),reload.idPacket)){
                        gunsManager.reload(reload.idPlayer);
                        ackCaching.add(connection.getID(),ack);
                    }
                } else if (object instanceof Network.SwitchWeaponSlot) {
                    Network.SwitchWeaponSlot switchWeaponSlot = (Network.SwitchWeaponSlot)object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = switchWeaponSlot.idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(connection.getID(),switchWeaponSlot.idPacket)){
                        gunsManager.switchWeaponSlot(switchWeaponSlot);
                        ackCaching.add(connection.getID(),ack);
                    }
                } else if (object instanceof Network.PlayerDropWeapon) {
                    Network.PlayerDropWeapon playerDropWeapon = (Network.PlayerDropWeapon)object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = playerDropWeapon.idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(connection.getID(),playerDropWeapon.idPacket)){
                        gunsManager.handleDropWeaponPacket(playerDropWeapon);
                        ackCaching.add(connection.getID(),ack);
                    }
                } else if (object instanceof Network.Ready) {
                    Network.Ready ready = (Network.Ready) object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = ready.idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(connection.getID(),ready.idPacket)){
                        playerLock.readLock().lock();
                        int totalPlayers;
                        try{
                            totalPlayers = connectedPlayers.size();
                        } finally {
                            playerLock.readLock().unlock();
                        }
                        if(totalPlayers > 0){//todo: not 1 guy playing mp
                            server.sendToAllTCP(object);
                            readyLock.writeLock().lock();
                            try{
                                for (PlayerReady pready : readyCheckPlayers) {
                                    if (pready.isThisPlayer(ready.idPlayer)){
                                        pready.setReady(ready.state);
                                        break;
                                    }
                                }
                            } finally {
                                readyLock.writeLock().unlock();
                            }
                        }
                        ackCaching.add(connection.getID(),ack);
                    }
                } else if (object instanceof Network.DropInteract) {
                    Network.DropInteract dropInteract = (Network.DropInteract) object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = ((Network.DropInteract) object).idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(connection.getID(),dropInteract.idPacket)){
                        itemManager.handleDropInteractPacket((Network.DropInteract) object);
                        ackCaching.add(connection.getID(),ack);
                    }
                } else if (object instanceof Network.ObjectInteract) {
                    Network.ObjectInteract objectInteract = (Network.ObjectInteract)object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = ((Network.ObjectInteract) object).idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(connection.getID(),objectInteract.idPacket)){
                        tileMap.handleObjectInteractPacket(objectInteract);
                        ackCaching.add(connection.getID(),ack);
                    }
                } else if (object instanceof Network.StopShooting) {
                    Network.StopShooting shooting = (Network.StopShooting)object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = shooting.idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(connection.getID(),shooting.idPacket)){
                        gunsManager.stopShooting(shooting.idPlayer);
                        ackCaching.add(connection.getID(),ack);
                    }
                } else if (object instanceof Network.StartShooting) {
                    Network.StartShooting shooting = (Network.StartShooting)object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = shooting.idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(connection.getID(),shooting.idPacket)){
                        gunsManager.startShooting(shooting.idPlayer);
                        ackCaching.add(connection.getID(),ack);
                    }
                } else if (object instanceof Network.ArtefactActivate) {
                    Network.ArtefactActivate activate = (Network.ArtefactActivate)object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = activate.idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(connection.getID(),activate.idPacket)){
                        artefactManager.activate(activate);
                        ackCaching.add(connection.getID(),ack);
                    }
                } else if (object instanceof Network.Ping) {
                    connection.sendUDP(object);
                } else if (object instanceof Network.PacketACK){
                    ackManager.acknowledged(connection.getID(), ((Network.PacketACK) object).id);
                }
            }
        });
    }

    private void handleDisconnect(Connection connection) {
        // player is already connected
        synchronized (connectedPlayers){
            for(int i = 0;i<connectedPlayers.size();i++){
                PlayerMP player = connectedPlayers.get(i);
                if (player.getIdConnection() == connection.getID()) {
                    connectedPlayers.remove(i);
                    synchronized (readyCheckPlayers){
                        for(PlayerReady playerReady : readyCheckPlayers){
                            if(playerReady.isThisPlayer(player)){
                                readyCheckPlayers.remove(playerReady);
                                break;
                            }
                        }
                        if(gameState == GameStateManager.PROGRESSROOM){
                            for(PlayerReady r : readyCheckPlayers) {
                                r.setReady(false);
                            }
                        }
                    }
                    upgrades.removePlayer(player.getIdConnection());
                    ackCaching.removePlayer(player.getIdConnection());
                    ackManager.removePlayer(player.getIdConnection());
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
        playerLock.writeLock().lock();
        try{
            for (PlayerMP player : connectedPlayers) {
                if (player.getIdConnection() == movePlayer.idPlayer) {
                    player.setUp(movePlayer.up);
                    player.setDown(movePlayer.down);
                    player.setRight(movePlayer.right);
                    player.setLeft(movePlayer.left);
                    break;
                }
            }
        } finally {
            playerLock.writeLock().unlock();
        }

    }
    private void handleJoin(Network.Join join, Connection connection) {
        PlayerMP playerMP = new PlayerMP(tileMap, join.username);
        int idConnection = connection.getID();
        playerMP.setIdConnection(idConnection);

        playerMP.setPosition(tileMap.getPlayerStartX(),tileMap.getPlayerStartY());

        if(join.host){
            playerLock.writeLock().lock();
            try{
                connectedPlayers.add(playerMP);
            } finally {
                playerLock.writeLock().unlock();
            }
            readyLock.writeLock().lock();
            try{
                readyCheckPlayers.add(new PlayerReady(idConnection));
            } finally {
                readyLock.writeLock().unlock();
            }
            upgrades.addPlayer(idConnection);
            ackManager.addPlayer(idConnection);
            ackCaching.addPlayer(idConnection);
        } else {
            playerLock.writeLock().lock();
            try{
                for (PlayerMP otherPlayer : connectedPlayers) {
                    Network.AddPlayer addPlayer = new Network.AddPlayer();
                    addPlayer.username = otherPlayer.getUsername();
                    addPlayer.idPlayer = otherPlayer.getIdConnection();
                    // send other players to new player
                    connection.sendTCP(addPlayer);
                }
                connectedPlayers.add(playerMP);
            } finally {
                playerLock.writeLock().unlock();
            }
            readyLock.writeLock().lock();
            try{
                readyCheckPlayers.add(new PlayerReady(idConnection));
            } finally {
                readyLock.writeLock().unlock();
            }
            if(gameState == GameStateManager.PROGRESSROOM){
                readyLock.writeLock().lock();
                try{
                    synchronized (readyCheckPlayers){
                        for(PlayerReady r : readyCheckPlayers){
                            r.setReady(false);
                        }
                    }
                } finally {
                    readyLock.writeLock().unlock();
                }
            }
            upgrades.addPlayer(idConnection);
            ackCaching.addPlayer(idConnection);
            ackManager.addPlayer(idConnection);
            Network.AddPlayer addPlayer = new Network.AddPlayer();
            addPlayer.username = join.username;
            addPlayer.idPlayer = idConnection;
            // send new player to others players
            server.sendToAllExceptTCP(idConnection,addPlayer);
        }
    }
    public Server getServer() {
        return server;
    }
    public void close(){
        server.close();
        Random.closeMP();
    }
    public void requestACK(Object o, int id){
        ackManager.add(o,id);
    }
    private static class ACKManager{
        private final HashMap<Integer,PlayerACKS> players;
        private final ArrayList<PacketWaitingACK> waitingForAdd;
        private final Lock lock;
        private final Lock waitingLock;
        ACKManager(){
            players = new HashMap<>();
            lock = new ReentrantLock();
            waitingLock = new ReentrantLock();
            waitingForAdd = new ArrayList<>();
        }
        public void addPlayer(int idcon){
            lock.lock();
            try{
                players.put(idcon,new PlayerACKS());
            } finally {
                lock.unlock();
            }
        }
        public void removePlayer(int idcon){
            lock.lock();
            try{
                players.remove(idcon);
            } finally {
                lock.unlock();
            }
        }
        public void acknowledged(int idCon, int id){
            lock.lock();
            try{
                PlayerACKS playerACKS = players.get(idCon);
                playerACKS.confirmedACKs[playerACKS.totalConfirmedACKs] = id;
                playerACKS.totalConfirmedACKs++;
            } finally {
                lock.unlock();
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
            lock.lock();
            try{
                waitingLock.lock();
                try{
                    if(waitingForAdd.size() != 0) { // optimization
                        for (PlayerACKS playerACKS : players.values()) {
                            playerACKS.packets.addAll(waitingForAdd);
                        }
                    }
                    waitingForAdd.clear();
                } finally {
                    waitingLock.unlock();
                }

                for(PlayerACKS playerACKS : players.values()){
                    int[] acks = Arrays.copyOf(playerACKS.confirmedACKs,playerACKS.totalConfirmedACKs);
                    for(int i : acks){
                        playerACKS.packets.removeIf(ack -> i == ack.id);
                    }
                    playerACKS.clearAcks();
                }
                for(Map.Entry<Integer, PlayerACKS> entry : players.entrySet()) {
                    for(PacketWaitingACK p : entry.getValue().packets){
                        if(p.shouldResend()){
                            MultiplayerManager.getInstance().server.getServer().sendToUDP(entry.getKey(),p.getPacket());
                        }
                    }
                }
            } finally {
                lock.unlock();
            }
        }
        private static class PlayerACKS{
            public LinkedList<PacketWaitingACK> packets;
            private int[] confirmedACKs;
            private int totalConfirmedACKs;
            public PlayerACKS(){
                packets = new LinkedList<>();
                confirmedACKs = new int[200];
                totalConfirmedACKs = 0;
            }

            public void clearAcks() {
                totalConfirmedACKs = 0;
            }
        }
        private static class PacketWaitingACK{
            private final Object packet;
            private final int id;
            private long timeSent;
            PacketWaitingACK(Object packet, int id){
                this.id = id;
                this.timeSent = System.nanoTime();
                this.packet = packet;
            }
            boolean shouldResend(){
                boolean sent = System.nanoTime() - timeSent > 100000000;
                if(sent) timeSent = System.nanoTime();
                return sent;
            }
            public Object getPacket() {
                return packet;
            }
        }
    }
    private static class ACKCaching{
        private HashMap<Integer,LinkedList<ACKCache>> list;
        private Lock lock;
        ACKCaching(){
            list = new HashMap<>();
            lock = new ReentrantLock();
        }
        public void addPlayer(int idConnection){
            lock.lock();
            try{
                list.put(idConnection,new LinkedList<>());
            } finally {
                lock.unlock();
            }
        }
        public void removePlayer(int idConnection){
            lock.lock();
            try{
                list.remove(idConnection);
            } finally {
                lock.unlock();
            }
        }
        public void update() {
            lock.lock();
            try {
                for(LinkedList<ACKCache> cachelist: list.values()){
                    cachelist.removeIf(ACKCache::shouldRemove);
                }
            } finally {
                lock.unlock();
            }
        }
        public boolean checkDuplicate(int idcon, int id){
            lock.lock();
            try {
                for(ACKCache cache : list.get(idcon)){
                    if(cache.ack.id == id) return true;
                }
            } finally {
                lock.unlock();
            }
            return false;
        }
        public void add(int idcon, Network.PacketACK ack){
            lock.lock();
            try {
                list.get(idcon).add(new ACKCache(ack));
            } finally {
                lock.unlock();
            }
        }
        private static class ACKCache{
            private final Network.PacketACK ack;
            private final long time;
            public ACKCache(Network.PacketACK ack){
                this.ack = ack;
                time = System.nanoTime();
            }
            public boolean shouldRemove(){return System.nanoTime() - time > 5000000000L;}
        }
    }
}
