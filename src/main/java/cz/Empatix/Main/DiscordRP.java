package cz.Empatix.Main;

import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;

public class DiscordRP {
    private final boolean DISABLED = true;

    private boolean running = true;
    private long created;

    private static DiscordRP instance = new DiscordRP();

    public static DiscordRP getInstance() {
        return instance;
    }

    public void start(){
        if(DISABLED) return;

        new Thread("Discord RPC Callback") {
            @Override
            public void run() {
                created = System.currentTimeMillis();

                DiscordEventHandlers handlers = new DiscordEventHandlers.Builder().setReadyEventHandler((user) -> {
                    System.out.println("Discord found: " + user.username + "#" + user.discriminator);
                }).build();

                DiscordRPC.discordInitialize("805802430553653279", handlers, true);
                while(running){
                    DiscordRPC.discordRunCallbacks();
                }
            }
        }.start();




    }
    public void shutdown(){
        if(DISABLED) return;
        running = false;
        DiscordRPC.discordShutdown();

    }
    public void update(String firstLine, String secondLine){
        if(DISABLED) return;

        DiscordRichPresence.Builder rich = new DiscordRichPresence.Builder(secondLine);
        rich.setDetails(firstLine);
        rich.setBigImage("newi","0.9.0");
        rich.setStartTimestamps(created);

        DiscordRPC.discordUpdatePresence(rich.build());



    }
}
