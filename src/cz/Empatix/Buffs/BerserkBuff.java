package cz.Empatix.Buffs;

public class BerserkBuff extends Buff{
    public BerserkBuff(){
        super(20000);
        idBuff = BERSERK;
    }
    public BerserkBuff(int idPlayer){
        super(idPlayer,20000);
        idBuff = BERSERK;
    }
    @Override
    public void apply() {
        if(idPlayer == -1){
            BuffManager buffManager = BuffManager.getInstance();
            buffManager.bonusMovementVelocityPercent += 0.25f;
        } else { // multiplayer
            BuffManagerMP buffManager = BuffManagerMP.getInstance();
            buffManager.getPlayerBuffs(idPlayer).bonusMovementVelocityPercent += 0.25f;
        }
    }


    @Override
    public void update() {

    }

    @Override
    public void remove() {
        if(idPlayer == -1){
            BuffManager buffManager = BuffManager.getInstance();
            buffManager.bonusMovementVelocityPercent -= 0.25f;
        } else { // multiplayer
            BuffManagerMP buffManager = BuffManagerMP.getInstance();
            buffManager.getPlayerBuffs(idPlayer).bonusMovementVelocityPercent -= 0.25f;
        }
    }
    @Override
    public void draw() {

    }
}
