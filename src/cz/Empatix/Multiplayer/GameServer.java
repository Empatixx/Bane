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


public class GameServer {
    private Server server;

    private final List<PlayerMP> connectedPlayers;
    private final List<PlayerReady> readyCheckPlayers;
    private GunsManagerMP gunsManager;
    private ItemManagerMP itemManager;
    private EnemyManagerMP enemyManagerMP;
    private ArtefactManagerMP artefactManager;
    private TileMap tileMap;
    private MiniMap map;
    private GunsManagerMP.GunUpgradesCache upgrades;
    private ACKManager ackManager;
    private ACKCaching ackCaching;

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
                                    playerInfo.idPlayer = player.getIdConnection();
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
                                        if (ready.isNotReady()) {
                                            allReady = false;
                                            break;
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
                                    if (ready.isNotReady()) {
                                        allReady = false;
                                        break;
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
                                            mpStatistics.addPlayer(p.getUsername(),p.getIdConnection());
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
        ackCaching = new ACKCaching();
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
                        if (player.getIdConnection() != request.exceptIdPlayer) {
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
                        server.sendToAllTCP(object);
                        synchronized (readyCheckPlayers) {
                            for (PlayerReady pready : readyCheckPlayers) {
                                if (pready.isThisPlayer(ready.idPlayer)) pready.setReady(ready.state);
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
                        itemManager.handleDrolpInteractPacket((Network.DropInteract) object);
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
        }));
        new Thread("Server-ACK") {
            @Override
            public void run() {
                while(MultiplayerManager.multiplayer){
                    ackManager.update();
                    ackCaching.update();
                }
            }
        }.start();
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
        synchronized (connectedPlayers){
            for(int i = 0;i<connectedPlayers.size();i++){
                PlayerMP player = connectedPlayers.get(i);
                if (player.getIdConnection() == movePlayer.idPlayer) {
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
        int idConnection = connection.getID();
        playerMP.setIdConnection(idConnection);

        playerMP.setPosition(tileMap.getPlayerStartX(),tileMap.getPlayerStartY());

        if(join.host){
            connectedPlayers.add(playerMP);
            readyCheckPlayers.add(new PlayerReady(idConnection));
            upgrades.addPlayer(idConnection);
            ackManager.addPlayer(idConnection);
            ackCaching.addPlayer(idConnection);
        } else {
            for (PlayerMP otherPlayer : connectedPlayers) {
                Network.AddPlayer addPlayer = new Network.AddPlayer();
                addPlayer.username = otherPlayer.getUsername();
                addPlayer.idPlayer = otherPlayer.getIdConnection();
                // send other players to new player
                connection.sendTCP(addPlayer);
            }
            connectedPlayers.add(playerMP);
            readyCheckPlayers.add(new PlayerReady(idConnection));
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
        private HashMap<Integer,PlayerACKS> players;
        private ArrayList<PacketWaitingACK> waitingForAdd;
        private Lock lock;
        private Lock waitingLock;
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
                    for(PlayerACKS playerACKS : players.values()){
                        playerACKS.packets.addAll(waitingForAdd);
                    }
                    waitingForAdd.clear();
                } finally {
                    waitingLock.unlock();
                }

                for(PlayerACKS playerACKS : players.values()){
                    for(int i = 0;i<playerACKS.totalConfirmedACKs;i++){
                        int finalI = i;
                        playerACKS.packets.removeIf(ack -> playerACKS.confirmedACKs[finalI] == ack.id);
                    }
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
