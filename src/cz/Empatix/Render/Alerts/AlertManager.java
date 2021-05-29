package cz.Empatix.Render.Alerts;

import java.util.ArrayList;

public class AlertManager {
    private static ArrayList<Alert> alerts;

    public static final int INFORMATION = 1;
    public static final int WARNING = 0;

    public AlertManager(){
        alerts = new ArrayList<>();
    }
    public static void add(int type, String message){
        Alert alert = new Alert(type,message);
        alerts.add(alert);
    }
    public void update(){
        for(int i = 0;i<alerts.size();i++){
            Alert a = alerts.get(i);
            a.update(alerts.size()-(i+1));
            if(a.shouldRemove()){
                alerts.remove(i);
                i--;
            }
        }
    }
    public void draw(){
        for(Alert a : alerts){
            a.draw();
        }
    }
}
