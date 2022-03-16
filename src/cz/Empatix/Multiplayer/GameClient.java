package cz.Empatix.Multiplayer;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import cz.Empatix.Gamestates.GameState;
import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Gamestates.Multiplayer.InGameMP;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Gamestates.Multiplayer.ProgressRoomMP;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GameClient{

    private final PacketHolder packetHolder;
    private final MultiplayerManager mpManager;
    private final GameStateManager gsm;
    private final Client client;

    private final ACKManager ackManager;
    private final ACKCaching ackCaching;

    private int numPlayers;
    private boolean recon;

    public GameClient(GameStateManager gsm, String ipAddress){
        this.gsm = gsm;
        numPlayers = 1;

        mpManager = MultiplayerManager.getInstance();
        packetHolder = mpManager.packetHolder;

        client = new Client(16384,4096);
        client.start();

        recon = false;
        ackManager = new ACKManager();
        ackCaching = new ACKCaching();

        new Thread("Client-ACK") {
            @Override
            public void run() {
                while(MultiplayerManager.multiplayer){
                    ackManager.update();
                }
            }
        }.start();
        client.addListener(new Listener.ThreadedListener(new Listener() {
            public void received (Connection connection, Object object) {
                if (object instanceof Network.AddPlayer) {
                    packetHolder.add(object,PacketHolder.JOINPLAYER);
                }
                if (object instanceof Network.Ping) {
                    client.updateReturnTripTime();
                }
                else if (object instanceof Network.Disconnect) {
                    packetHolder.add(object,PacketHolder.DISCONNECTPLAYER);
                }
                else if (object instanceof Network.MovePlayer){
                    packetHolder.add(object,PacketHolder.MOVEPLAYER);
                }
                else if (object instanceof Network.CanJoin){
                    packetHolder.add(object,PacketHolder.CANJOIN);
                }
                else if (object instanceof Network.PstatsUpdate){
                    packetHolder.add(object,PacketHolder.PLAYERSSTATS);
                }
                else if (object instanceof Network.Ready){
                    Network.Ready ready = (Network.Ready)object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = ready.idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(ready.idPacket)){
                        boolean state = ((Network.Ready) object).state;

                        GameState gameState = gsm.getCurrentGamestate();
                        if(gameState instanceof ProgressRoomMP) {
                            for(PlayerReady playerReady : ((ProgressRoomMP) gameState).playerReadies){
                                if(playerReady == null) continue;
                                if(playerReady.isEqual(((Network.Ready) object).idPlayer)){
                                    playerReady.setReady(state);
                                    int selfId = mpManager.getIdConnection(); // origin id
                                    if(selfId != ((Network.Ready) object).idPlayer && state){
                                        Network.Alert alert = new Network.Alert();
                                        alert.text = playerReady.getUsername()+" is ready!";
                                        alert.idPlayer = mpManager.getIdConnection();
                                        alert.warning = false;
                                        packetHolder.add(alert,PacketHolder.ALERT);
                                    }
                                }
                            }
                        }
                        if(gameState instanceof InGameMP) {
                            for(PlayerReady playerReady : ((InGameMP) gameState).playerReadies){
                                if(playerReady == null) continue;
                                if(playerReady.isEqual(((Network.Ready) object).idPlayer)){
                                    playerReady.setReady(state);
                                }
                            }
                        }
                        ackCaching.add(ack);
                    }
                }
                else if (object instanceof Network.TransferRoomMap){
                    packetHolder.add(object,PacketHolder.TRANSFERROOMMAP);

                }
                else if (object instanceof Network.TransferRoom){
                    packetHolder.add(object,PacketHolder.TRANSFERROOM);
                }
                else if(object instanceof Network.MapLoaded){
                    packetHolder.add(object,PacketHolder.MAPLOADED);
                }
                else if(object instanceof Network.RoomObjectAnimationSync){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        packetHolder.add(object,PacketHolder.ROANIMSYNC);
                    }
                }
                else if(object instanceof Network.TrapArrowAdd){
                    Network.TrapArrowAdd packet = (Network.TrapArrowAdd) object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = packet.idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(packet.idPacket)){
                        GameState gameState = gsm.getCurrentGamestate();
                        if(gameState instanceof InGameMP) {
                            packetHolder.add(object,PacketHolder.TRAPARROWADD);
                        }
                        ackCaching.add(ack);
                    }
                }
                else if(object instanceof Network.TrapArrowMove){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        packetHolder.add(object,PacketHolder.TRAPARROWMOVE);
                    }
                }
                else if(object instanceof Network.TrapArrowHit){
                    GameState gameState = gsm.getCurrentGamestate();
                    Network.TrapArrowHit packet = (Network.TrapArrowHit) object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = packet.idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(packet.idPacket)) {
                        if (gameState instanceof InGameMP) {
                            packetHolder.add(object, PacketHolder.TRAPARROWHIT);
                        }
                        ackCaching.add(ack);
                    }
                }
                else if(object instanceof Network.AddBullet){
                    GameState gameState = gsm.getCurrentGamestate();
                    Network.AddBullet packet = (Network.AddBullet) object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = packet.idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(packet.idPacket)) {
                        if (gameState instanceof InGameMP) {
                            ((InGameMP) gameState).gunsManager.handleAddBulletPacket((Network.AddBullet) object);
                        }
                        ackCaching.add(ack);
                    }
                }
                else if(object instanceof Network.HitBullet){
                    GameState gameState = gsm.getCurrentGamestate();
                    Network.HitBullet packet = (Network.HitBullet) object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = packet.idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(packet.idPacket)) {
                        if (gameState instanceof InGameMP) {
                            packetHolder.add(object, PacketHolder.HITBULLET);
                        }
                        ackCaching.add(ack);
                    }
                }
                else if(object instanceof Network.ArtefactEventState){
                    GameState gameState = gsm.getCurrentGamestate();
                    Network.ArtefactEventState packet = (Network.ArtefactEventState) object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = packet.idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(packet.idPacket)) {
                        if(gameState instanceof InGameMP) {
                            packetHolder.add(object,PacketHolder.ARTEFACTSTATE);
                        }
                        ackCaching.add(ack);
                    }

                }
                else if(object instanceof Network.ArtefactAddBullet){
                    GameState gameState = gsm.getCurrentGamestate();
                    Network.ArtefactAddBullet packet = (Network.ArtefactAddBullet) object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = packet.idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(packet.idPacket)) {
                        if (gameState instanceof InGameMP) {
                            packetHolder.add(object, PacketHolder.ARTEFACTADDBULLET);
                        }
                        ackCaching.add(ack);
                    }
                }
                else if(object instanceof Network.EnemyHealthHeal){
                    GameState gameState = gsm.getCurrentGamestate();
                    Network.EnemyHealthHeal packet = (Network.EnemyHealthHeal) object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = packet.idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(packet.idPacket)) {
                        if(gameState instanceof InGameMP) {
                            packetHolder.add(object,PacketHolder.ENEMYHEAL);
                        }
                        ackCaching.add(ack);
                    }
                }
                else if(object instanceof Network.WeaponInfo){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        if(((InGameMP)gameState).gunsManager != null){
                            packetHolder.add(object,PacketHolder.WEAPONINFO);
                        }
                    }
                }
                else if(object instanceof Network.DropItem){
                    GameState gameState = gsm.getCurrentGamestate();
                    Network.DropItem packet = (Network.DropItem) object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = packet.idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(packet.idPacket)) {
                        if(gameState instanceof InGameMP) {
                            packetHolder.add(object,PacketHolder.DROPITEM);
                        }
                        ackCaching.add(ack);
                    }
                }
                else if(object instanceof Network.MoveDropItem){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        if(((InGameMP)gameState).itemManager != null){
                            packetHolder.add(object,PacketHolder.MOVEITEM);
                        }
                    }
                }
                else if(object instanceof Network.RemoveItem){
                    GameState gameState = gsm.getCurrentGamestate();
                    Network.RemoveItem packet = (Network.RemoveItem) object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = packet.idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(packet.idPacket)) {
                        if(gameState instanceof InGameMP) {
                            packetHolder.add(object,PacketHolder.REMOVEITEM);
                        }
                        ackCaching.add(ack);
                    }
                }
                else if(object instanceof Network.NextFloor){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        packetHolder.add(object,PacketHolder.NEXTFLOOR);
                    }
                }
                else if (object instanceof Network.MoveBullet){
                    handleMove((Network.MoveBullet) object);
                }
                else if(object instanceof Network.AddEnemy){
                    GameState gameState = gsm.getCurrentGamestate();
                    Network.AddEnemy packet = (Network.AddEnemy) object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = packet.idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(packet.idPacket)) {
                        if(gameState instanceof InGameMP) {
                            packetHolder.add(object,PacketHolder.ADDENEMY);
                        }
                        ackCaching.add(ack);
                    }
                }
                else if (object instanceof Network.LockRoom){
                    GameState gameState = gsm.getCurrentGamestate();
                    Network.LockRoom packet = (Network.LockRoom) object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = packet.idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(packet.idPacket)) {
                        if (gameState instanceof InGameMP) {
                            packetHolder.add(object, PacketHolder.LOCKROOM);
                        }
                        ackCaching.add(ack);
                    }
                }


                else if(object instanceof Network.PlayerHit){
                    GameState gameState = gsm.getCurrentGamestate();
                    Network.PlayerHit packet = (Network.PlayerHit) object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = packet.idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(packet.idPacket)) {
                        if (gameState instanceof InGameMP) {
                            packetHolder.add(object, PacketHolder.PLAYERHIT);
                        }
                        ackCaching.add(ack);
                    }
                }
                else if (object instanceof Network.MoveEnemy){
                    packetHolder.add(object,PacketHolder.MOVEENEMY);
                }
                else if (object instanceof Network.MoveRoomObject){
                    //handleMove((Network.MoveRoomObject) object);
                    packetHolder.add(object,PacketHolder.MOVEROOMOBJECT);
                }
                else if (object instanceof Network.SwitchWeaponSlot){
                    GameState gameState = gsm.getCurrentGamestate();
                    Network.SwitchWeaponSlot packet = (Network.SwitchWeaponSlot) object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = packet.idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(packet.idPacket)) {
                        if(gameState instanceof InGameMP) {
                            ((InGameMP)gameState).gunsManager.handleSwitchWeaponPacket((Network.SwitchWeaponSlot)object);
                        }
                        ackCaching.add(ack);
                    }
                }
                else if (object instanceof Network.PlayerDropWeapon){
                    GameState gameState = gsm.getCurrentGamestate();
                    Network.PlayerDropWeapon packet = (Network.PlayerDropWeapon) object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = packet.idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(packet.idPacket)) {
                        if(gameState instanceof InGameMP) {
                            ((InGameMP)gameState).gunsManager.handleDropPlayerWeaponPacket((Network.PlayerDropWeapon)object);
                        }
                        ackCaching.add(ack);
                    }
                }
                else if (object instanceof Network.DropWeapon){
                    GameState gameState = gsm.getCurrentGamestate();
                    Network.DropWeapon packet = (Network.DropWeapon) object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = packet.idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(packet.idPacket)) {
                        if (gameState instanceof InGameMP) {
                            ((InGameMP) gameState).gunsManager.handleDropWeaponPacket((Network.DropWeapon) object);
                        }
                        ackCaching.add(ack);
                    }
                }
                else if (object instanceof Network.DropArtefact){
                    GameState gameState = gsm.getCurrentGamestate();
                    Network.DropArtefact packet = (Network.DropArtefact) object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = packet.idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(packet.idPacket)) {
                        if (gameState instanceof InGameMP) {
                            packetHolder.add(object, PacketHolder.DROPARTEFACT);
                        }
                        ackCaching.add(ack);
                    }
                }
                else if (object instanceof Network.DropInteract){
                    GameState gameState = gsm.getCurrentGamestate();
                    Network.DropInteract packet = (Network.DropInteract) object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = packet.idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(packet.idPacket)) {
                        if (gameState instanceof InGameMP) {
                            packetHolder.add(object, PacketHolder.OBJECTINTERACT);
                        }
                        ackCaching.add(ack);
                    }
                }
                else if (object instanceof Network.AddRoomObject){
                    Network.AddRoomObject packet = (Network.AddRoomObject) object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = packet.idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(packet.idPacket)) {
                        packetHolder.add(object,PacketHolder.ADDROOMOBJECT);
                        ackCaching.add(ack);
                    }
                }
                else if (object instanceof Network.AddEnemyProjectile){
                    GameState gameState = gsm.getCurrentGamestate();
                    Network.AddEnemyProjectile packet = (Network.AddEnemyProjectile) object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = packet.idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(packet.idPacket)) {
                        if(gameState instanceof InGameMP) {
                            packetHolder.add(object, PacketHolder.ADD_ENEMYPROJECTION);
                        }
                        ackCaching.add(ack);
                    }
                }
                else if (object instanceof Network.RemoveEnemy){
                    GameState gameState = gsm.getCurrentGamestate();
                    Network.RemoveEnemy packet = (Network.RemoveEnemy) object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = packet.idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(packet.idPacket)) {
                        if(gameState instanceof InGameMP) {
                            packetHolder.add(object,PacketHolder.REMOVEENEMY);
                        }
                        ackCaching.add(ack);
                    }
                }
                else if (object instanceof Network.MoveEnemyProjectile){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        packetHolder.add(object,PacketHolder.MOVE_ENEMYPROJECTILE);
                    }
                }
                else if (object instanceof Network.HitEnemyProjectile){
                    GameState gameState = gsm.getCurrentGamestate();
                    Network.HitEnemyProjectile packet = (Network.HitEnemyProjectile) object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = packet.idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(packet.idPacket)) {
                        if(gameState instanceof InGameMP) {
                            packetHolder.add(object,PacketHolder.HIT_ENEMYPROJECTILE);
                        }
                        ackCaching.add(ack);
                    }
                }
                else if (object instanceof Network.ShopDropitem){
                    packetHolder.add(object,PacketHolder.SHOPITEM);
                }
                else if (object instanceof Network.Alert){
                    Network.Alert packet = (Network.Alert) object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = packet.idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(packet.idPacket)) {
                        packetHolder.add(object,PacketHolder.ALERT);
                        ackCaching.add(ack);
                    }
                }
                else if (object instanceof Network.AllPlayersDeath){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        packetHolder.add(object,PacketHolder.ALLPLAYERDEAD);
                    }
                }
                else if (object instanceof Network.OpenChest){
                    GameState gameState = gsm.getCurrentGamestate();
                    Network.OpenChest packet = (Network.OpenChest) object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = packet.idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(packet.idPacket)) {
                        if(gameState instanceof InGameMP) {
                            packetHolder.add(object,PacketHolder.OPENCHEST);
                        }
                        ackCaching.add(ack);
                    }
                }
                else if (object instanceof Network.PlayerInfo){
                    packetHolder.add(object,PacketHolder.PLAYERINFO);
                }
                else if (object instanceof Network.EnemySync){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        packetHolder.add(object, PacketHolder.ENEMYSYNC);
                    }
                }
                else if (object instanceof Network.LaserBeamSync){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        packetHolder.add(object, PacketHolder.LASERBEAMSYNC);
                    }
                }
                else if (object instanceof Network.LaserBeamHit){
                    GameState gameState = gsm.getCurrentGamestate();
                    Network.LaserBeamHit packet = (Network.LaserBeamHit) object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = packet.idPacket;
                    connection.sendUDP(ack);
                    if(!ackCaching.checkDuplicate(packet.idPacket)) {
                        if(gameState instanceof InGameMP) {
                            packetHolder.add(object, PacketHolder.LASERBEAMHIT);
                        }
                        ackCaching.add(ack);
                    }
                }
                else if (object instanceof Network.ArtefactActivate) {
                    GameState gameState = gsm.getCurrentGamestate();
                    Network.ArtefactActivate packet = (Network.ArtefactActivate) object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = packet.idPacket;
                    connection.sendUDP(ack);
                    if (!ackCaching.checkDuplicate(packet.idPacket)) {
                        if (gameState instanceof InGameMP) {
                            packetHolder.add(object, PacketHolder.ARTEFACTACTIVATED);
                        }
                        ackCaching.add(ack);
                    }
                } else if (object instanceof Network.ExplosionDamage) {
                    GameState gameState = gsm.getCurrentGamestate();
                    Network.ExplosionDamage packet = (Network.ExplosionDamage) object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = packet.idPacket;
                    connection.sendUDP(ack);
                    if (!ackCaching.checkDuplicate(packet.idPacket)) {
                        if (gameState instanceof InGameMP) {
                            packetHolder.add(object, PacketHolder.EXPLOSIONDAMAGE);
                        }
                        ackCaching.add(ack);
                    }
                } else if (object instanceof Network.TrapRoomObjectDamage){
                    GameState gameState = gsm.getCurrentGamestate();
                    Network.TrapRoomObjectDamage packet = (Network.TrapRoomObjectDamage) object;
                    Network.PacketACK ack = new Network.PacketACK();
                    ack.id = packet.idPacket;
                    connection.sendUDP(ack);
                    if (!ackCaching.checkDuplicate(packet.idPacket)) {
                        if (gameState instanceof InGameMP) {
                            packetHolder.add(object, PacketHolder.TRAPRODAMAGE);
                        }
                        ackCaching.add(ack);
                    }
                } else if (object instanceof Network.PacketACK){
                    ackManager.acknowledged(((Network.PacketACK) object).id);
                }
            }
        }));

        Network.register(client);

        try {
            client.connect(5000, ipAddress, 54555, 54777);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void requestACK(Object packet, int idPacket){
        ackManager.add(packet,idPacket);
    }

    public Client getClient() {
        return client;
    }

    public int getTotalPlayers() {
        return numPlayers;
    }
    public void handleMove(Network.MoveBullet movePacket) {
        GameState gameState = gsm.getCurrentGamestate();
        if (gameState instanceof InGameMP) {
            ((InGameMP) gameState).gunsManager.handleBulletMovePacket(movePacket);
            ((InGameMP) gameState).artefactManager.handleBulletMovePacket(movePacket);
        }
    }
    public void close(){
        client.close();
    }

    public void setNumPlayers(int numPlayers) {
        this.numPlayers = numPlayers;
    }

    public void tryReconnect() {
        /*if(recon) return;
        new Thread(){
            @Override
            public void run() {
                super.run();
                long delay = System.currentTimeMillis();
                while(MultiplayerManager.multiplayer){
                    if(System.currentTimeMillis() - delay > 5000){
                        delay+=5000;
                        try{
                            client.reconnect();
                        }catch (Exception e){

                        }
                        if(client.isConnected()){
                            recon = false;
                            break;
                        }
                    }
                }
            }
        };*/
    }
    private static class ACKManager{
        private final LinkedList<PacketWaitingACK> packets;
        private final LinkedList<PacketWaitingACK> waitingForAdd;
        private final int[] confirmedACKs;
        private int totalConfirmedACKs;
        private final Lock waitingLock;
        private final Lock ackLock;
        ACKManager(){
            packets = new LinkedList<>();
            waitingLock = new ReentrantLock();
            ackLock = new ReentrantLock();
            confirmedACKs = new int[200];
            waitingForAdd = new LinkedList<>();
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
                if(waitingForAdd.size() != 0) { // optimization
                    packets.addAll(waitingForAdd);
                    waitingForAdd.clear();
                }
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
                    MultiplayerManager.getInstance().client.getClient().sendUDP(p.getPacket());
                }
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
        private final LinkedList<ACKCache> list;
        private final Lock lock;
        ACKCaching(){
            list = new LinkedList<>();
            lock = new ReentrantLock();
        }
        public void update() {
            lock.lock();
            try {
                list.removeIf(ACKCache::shouldRemove);
            } finally {
                lock.unlock();
            }
        }
        public boolean checkDuplicate(int id){
            lock.lock();
            try {
                for(ACKCache cache : list){
                    if(cache.ack.id == id) return true;
                }
            } finally {
                lock.unlock();
            }
            return false;
        }
        public void add(Network.PacketACK ack){
            lock.lock();
            try {
                list.add(new ACKCache(ack));
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
