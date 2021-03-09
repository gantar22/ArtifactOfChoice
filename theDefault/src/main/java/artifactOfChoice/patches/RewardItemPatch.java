package artifactOfChoice.patches;

import artifactOfChoice.DefaultMod;
import artifactOfChoice.relics.ObtainRelicLater;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rewards.RewardItem;

@SpirePatch(clz = RewardItem.class,method="claimReward",paramtypez = {})
public class RewardItemPatch {

    public static SpireReturn<Boolean> Prefix(RewardItem __Instance)
    {
        switch (__Instance.type)
        {
            case RELIC:
                DefaultMod.relicReplacement.receiveRelicGet(__Instance.relic,relic -> AbstractDungeon.effectsQueue.add(0, new ObtainRelicLater(relic)));
                return SpireReturn.Return(true);
            case POTION:
                DefaultMod.potionReplacement.getPotion(__Instance.potion,potion -> AbstractDungeon.player.obtainPotion((potion)));
                return  SpireReturn.Return(true);
            default:
                return SpireReturn.Continue();
        }

    }
}
