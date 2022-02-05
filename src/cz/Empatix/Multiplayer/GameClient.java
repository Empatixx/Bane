package cz.Empatix.Multiplayer;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import cz.Empatix.Gamestates.GameState;
import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Gamestates.Multiplayer.InGameMP;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Gamestates.Multiplayer.ProgressRoomMP;
import cz.Empatix.Main.DiscordRP;
import cz.Empatix.Render.Alerts.AlertManager;

import java.io.IOException;

public class GameClient{

    private PacketHolder packetHolder;
    private MultiplayerManager mpManager;
    private final GameStateManager gsm;
    private final Client client;

    private int numPlayers;

    public GameClient(GameStateManager gsm, String ipAdress){
        this.gsm = gsm;
        numPlayers = 1;

        mpManager = MultiplayerManager.getInstance();
        packetHolder = mpManager.packetHolder;

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
                        DiscordRP.getInstance().update("Multiplayer - In-Game","Lobby "+getTotalPlayers()+"/2");
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
                    String playerUsername = mpManager.getUsername();
                    if(gameState instanceof ProgressRoomMP){
                        ((ProgressRoomMP) gameState).player[numPlayers].remove();
                        ((ProgressRoomMP) gameState).player[numPlayers] = null;
                        ((ProgressRoomMP) gameState).playerReadies[numPlayers] = null;
                        if(!packetUsername.equalsIgnoreCase(playerUsername)) AlertManager.add(AlertManager.WARNING,packetUsername+" has left the lobby!");
                        DiscordRP.getInstance().update("Multiplayer - In-Game","Lobby "+getTotalPlayers()+"/2");
                    }
                    if(gameState instanceof InGameMP){
                        ((InGameMP) gameState).player[numPlayers].remove();
                        ((InGameMP) gameState).player[numPlayers] = null;
                        if(!packetUsername.equalsIgnoreCase(playerUsername)) AlertManager.add(AlertManager.WARNING,packetUsername+" has left the game!");
                    }
                }
                else if (object instanceof Network.MovePlayer){
                    packetHolder.add(object,PacketHolder.MOVEPLAYER);
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
                        String playerUsername = mpManager.getUsername();
                        if(!packetUsername.equalsIgnoreCase(playerUsername)) AlertManager.add(AlertManager.INFORMATION,packetUsername+" is ready!");
                    }
                }
                else if (object instanceof Network.TransferRoomMap){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        packetHolder.add(object,PacketHolder.TRANSFERROOMMAP);
                    }
                }
                else if (object instanceof Network.TransferRoom){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        packetHolder.add(object,PacketHolder.TRANSFERROOM);
                    }
                }
                else if(object instanceof Network.MapLoaded){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        packetHolder.add(object,PacketHolder.MAPLOADED);
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
                else if(object instanceof Network.WeaponInfo){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        ((InGameMP)gameState).gunsManager.handleWeaponInfoPacket((Network.WeaponInfo) object);
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
                        ((InGameMP)gameState).itemManager.handleMoveDropItemPacket((Network.MoveDropItem)object);
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
                    handleMove((Network.MoveRoomObject) object);
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
                else if (object instanceof Network.OpenChest){
                    GameState gameState = gsm.getCurrentGamestate();
                    if(gameState instanceof InGameMP) {
                        packetHolder.add(object,PacketHolder.OPENCHEST);
                    }
                }
                else if (object instanceof Network.PlayerInfo){
                    /*GameState gameState = gsm.getCurrentGamestate();
                    Network.PlayerInfo info = (Network.PlayerInfo) object;
                    if(gameState instanceof InGameMP) {
                        if(((InGameMP) gameState).player == null) return;
                        for(PlayerMP player : ((InGameMP) gameState).player) {
                            if(player == null) continue;
                            if(player.getUsername().equalsIgnoreCase(info.username)){
                                player.setHealth(info.health);
                                System.out.println("info: "+info.health);
                                player.setCoins(info.coins);
                                player.setArmor(info.armor);
                                player.setMaxArmor(info.maxArmor);
                                player.setMaxHealth(info.maxHealth);
                            }
                        }
                    }

                     */
                    packetHolder.add(object,PacketHolder.PLAYERINFO);
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
    public void handleMove(Network.MoveBullet movePacket) {
        GameState gameState = gsm.getCurrentGamestate();
        if (gameState instanceof InGameMP) {
            ((InGameMP) gameState).gunsManager.handleBulletMovePacket(movePacket);
            ((InGameMP) gameState).artefactManager.handleBulletMovePacket(movePacket);

        }
    }
    public void handleMove(Network.MoveRoomObject movePacket) {
        GameState gameState = gsm.getCurrentGamestate();
        if (gameState instanceof InGameMP) {
            ((InGameMP) gameState).tileMap.handleRoomMovePacket(movePacket);
        }
    }
    public void close(){
        client.close();
    }

    public void setNumPlayers(int numPlayers) {
        this.numPlayers = numPlayers;
    }
}
