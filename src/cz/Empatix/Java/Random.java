package cz.Empatix.Java;


import java.util.SplittableRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class Random {
    private static SplittableRandom[] random;
    private static AtomicInteger count;

    public static void init(){
        if(count == null){
            count = new AtomicInteger();
            random = new SplittableRandom[2];
        }
        random[count.getAndIncrement()] = new SplittableRandom();
    }
    // removing second random, bcs it is no longer in memory, we closed multiplayer
    public static void closeMP(){
        random[count.decrementAndGet()] = null;
    }
    public static int nextInt(int max){
        if(Thread.currentThread().getName().equalsIgnoreCase("Server-Logic")) return random[1].nextInt(max);
        return random[0].nextInt(max);
    }
    public static double nextDouble(){
        if(Thread.currentThread().getName().equalsIgnoreCase("Server-Logic")) return random[1].nextDouble();
        return random[0].nextDouble();
    }
}
