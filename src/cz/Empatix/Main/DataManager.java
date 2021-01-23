package cz.Empatix.Main;

import cz.Empatix.Gamestates.InGame;

import java.io.*;

public class DataManager {

    public static InGame load(){
        InGame game = null;
        try {
            FileInputStream fileIn = new FileInputStream("gamesave.dat");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            game = (InGame) in.readObject();
            in.close();
            fileIn.close();
        } catch (ClassNotFoundException c) {
            System.out.println("InGame class not found");
            c.printStackTrace();
            return game;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            return game;
        }
        return game;
    }
    public static void saveGame(InGame game){
        try {
            FileOutputStream fileOut =
                    new FileOutputStream("gamesave.dat");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(game);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }
}
