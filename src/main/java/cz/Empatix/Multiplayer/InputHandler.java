package cz.Empatix.Multiplayer;

import com.esotericsoftware.kryonet.Client;

import java.util.ArrayList;
import java.util.List;

public class InputHandler {
    public final static byte MOVE_UP = 1;
    public final static byte MOVE_DOWN = 2;
    public final static byte MOVE_LEFT = 3;
    public final static byte MOVE_RIGHT = 4;

    public final static byte INTERACT = 5;
    public final static byte USE_ARTEFACT = 6;

    public final static byte RELOAD = 7;
    public final static byte DROP_GUN = 8;
    public final static byte CHANGE_FSLOT = 9; // first slot - 1
    public final static byte CHANGE_SSLOT = 10; // secondary slot - 2

    private int lastTickSent;
    private int idPlayer;

    private List<Byte> commands;
    public InputHandler(int originIdPlayer){
        idPlayer = originIdPlayer;
        commands = new ArrayList<>();
        lastTickSent = 0;
    }
    public void addCommand(byte command, boolean pressed){
        if(!pressed) commands.add((byte) -command);
        else commands.add(command);
    }
    public void sendCommand(){
        if(lastTickSent != GameClient.serverTick){
            lastTickSent = GameClient.serverTick;
            Client client = MultiplayerManager.getInstance().client.getClient();
            Network.InputCommand input = new Network.InputCommand();
            input.idPlayer = idPlayer;
            input.tick = GameClient.serverTick;
            byte[] result = new byte[commands.size()];
            for(int i = 0; i < commands.size(); i++) {
                result[i] = commands.get(i);
            }
            input.commands = result;
            client.sendUDP(input);
            commands.clear();
        }
    }
}
