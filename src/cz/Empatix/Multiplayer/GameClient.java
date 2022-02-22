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

public class GameClient{

    private PacketHolder packetHolder;
    private MultiplayerManager mpManager;
    private GameStateManager gsm;
    private Client client;

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

        client.addListener(new Listener() {
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
                    boolean state = ((Network.Ready) object).state;

                    GameState gameState = gsm.getCurrentGamestate();
                    String packetUsername = ((Network.Ready) object).username;
                    if(gameState instanceof ProgressRoomMP) {
                        for(PlayerReady playerReady : ((ProgressRoomMP) gameState).playerReadies){
                            if(playerReady == null) continue;
                            if(playerReady.getUsername().equalsIgnoreCase(packetUsername)){
                                playerReady.setReady(state);
                                String playerUsername = mpManager.getUsername();
                                if(!packetUsername.equalsIgnoreCase(playerUsername) && state){
                                    Network.Alert alert = new Network.Alert();
                                    alert.text = packetUsername+" is ready!";
                                    alert.username = MultiplayerManager.getInstance().getUsername();
                                    alert.warning = false;
                                    packetHolder.add(alert,PacketHolder.ALERT);
                                }
                            }
                        }
                    }
                    if(gameState instanceof InGameMP) {
                        for(PlayerReady playerReady : ((InGameMP) gameState).playerReadies){
                            if(playerReady == null) continue;
                            if(playerReady.getUsername().equalsIgnoreCase(packetUsername)){
                                playerReady.setReady(state);
                            }
                        }
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
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        packetHolder.add(object,PacketHolder.TRAPARROWADD);
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
                    if(gameState instanceof InGameMP) {
                        packetHolder.add(object,PacketHolder.TRAPARROWHIT);
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
                        packetHolder.add(object,PacketHolder.HITBULLET);
                    }
                }
                else if(object instanceof Network.ArtefactAddBullet){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        packetHolder.add(object,PacketHolder.ARTEFACTADDBULLET);
                    }
                }
                else if(object instanceof Network.EnemyHealthHeal){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        packetHolder.add(object,PacketHolder.ENEMYHEAL);
                    }
                }
                else if(object instanceof Network.WeaponInfo){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        if(((InGameMP)gameState).gunsManager != null)((InGameMP)gameState).gunsManager.handleWeaponInfoPacket((Network.WeaponInfo) object);
                    }
                }
                else if(object instanceof Network.DropItem){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        packetHolder.add(object,PacketHolder.DROPITEM);
                    }
                }
                else if(object instanceof Network.MoveDropItem){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        if(((InGameMP)gameState).itemManager != null){
                            ((InGameMP)gameState).itemManager.handleMoveDropItemPacket((Network.MoveDropItem)object);
                        }
                    }
                }
                else if(object instanceof Network.RemoveItem){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        packetHolder.add(object,PacketHolder.REMOVEITEM);
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
                    if(gameState instanceof InGameMP) {
                        packetHolder.add(object,PacketHolder.ADDENEMY);
                    }
                }
                else if (object instanceof Network.LockRoom){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        packetHolder.add(object,PacketHolder.LOCKROOM);
                    }
                }


                else if(object instanceof Network.PlayerHit){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        packetHolder.add(object,PacketHolder.PLAYERHIT);
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
                else if (object instanceof Network.DropArtefact){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        packetHolder.add(object,PacketHolder.DROPARTEFACT);
                    }
                }
                else if (object instanceof Network.DropInteract){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        packetHolder.add(object,PacketHolder.OBJECTINTERACT);
                    }
                }
                else if (object instanceof Network.AddRoomObject){
                    packetHolder.add(object,PacketHolder.ADDROOMOBJECT);
                }
                else if (object instanceof Network.AddEnemyProjectile){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        packetHolder.add(object,PacketHolder.ADD_ENEMYPROJECTION);
                    }
                }
                else if (object instanceof Network.RemoveEnemy){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        packetHolder.add(object,PacketHolder.REMOVEENEMY);
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
                    if(gameState instanceof InGameMP) {
                        packetHolder.add(object,PacketHolder.HIT_ENEMYPROJECTILE);
                    }
                }
                else if (object instanceof Network.ShopDropitem){
                    packetHolder.add(object,PacketHolder.SHOPITEM);
                }
                else if (object instanceof Network.Alert){
                    packetHolder.add(object,PacketHolder.ALERT);
                }
                else if (object instanceof Network.AllPlayersDeath){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        packetHolder.add(object,PacketHolder.ALLPLAYERDEAD);
                    }
                }
                else if (object instanceof Network.OpenChest){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        packetHolder.add(object,PacketHolder.OPENCHEST);
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
                    if(gameState instanceof InGameMP) {
                        packetHolder.add(object, PacketHolder.LASERBEAMHIT);
                    }
                }
                else if (object instanceof Network.ArtefactActivate){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        packetHolder.add(object,PacketHolder.ARTEFACTACTIVATED);
                    }
                }
            }
        });

        Network.register(client);

        try {
            client.connect(5000, ipAddress, 54555, 54777);
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
}
