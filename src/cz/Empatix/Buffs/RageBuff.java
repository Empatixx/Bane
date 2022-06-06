package cz.Empatix.Buffs;

public class RageBuff extends Buff{
    public RageBuff(){
        super(20000);
        idBuff = RAGE;

    }
    public RageBuff(int idPlayer){
        super(idPlayer,20000);
        idBuff = RAGE;
    }
    @Override
    public void apply() {
        if(idPlayer == -1){
            BuffManager buffManager = BuffManager.getInstance();
            buffManager.baseCriticalChanceMultiplier++;
        } else { // multiplayer
            BuffManagerMP buffManager = BuffManagerMP.getInstance();
            buffManager.getPlayerBuffs(idPlayer).baseCriticalChanceMultiplier++;
        }
    }
    @Override
    public void update() {

    }

    @Override
    public void remove() {
        if(idPlayer == -1){
            BuffManager buffManager = BuffManager.getInstance();
            buffManager.baseCriticalChanceMultiplier--;
        } else { // multiplayer
            BuffManagerMP buffManager = BuffManagerMP.getInstance();
            buffManager.getPlayerBuffs(idPlayer).baseCriticalChanceMultiplier--;
        }
    }

    @Override
    public void draw() {

    }
}
