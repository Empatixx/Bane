package cz.Empatix.Java;

import java.util.SplittableRandom;

public class Random {
    private static SplittableRandom random;

    public static void init(){
        random = new SplittableRandom();
    }
    public static int nextInt(int max){
        return random.nextInt(max);
    }
    public static double nextDouble(){
        return random.nextDouble();
    }
}
