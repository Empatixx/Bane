package cz.Empatix.Main;

import net.arikia.dev.drpc.DiscordRPC;

public class DiscordRP {

    private boolean running = true;
    private long created;

    private static DiscordRP instance = new DiscordRP();

    public static DiscordRP getInstance() {
        return instance;
    }

    public void start(){
        /*
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


         */

    }
    public void shutdown(){
        running = false;
        DiscordRPC.discordShutdown();

    }
    public void update(String firstLine, String secondLine){
/*
        DiscordRichPresence.Builder rich = new DiscordRichPresence.Builder(secondLine);
        rich.setDetails(firstLine);
        rich.setBigImage("newi","0.9.0");
        rich.setStartTimestamps(created);

        DiscordRPC.discordUpdatePresence(rich.build());

 */

    }
}
