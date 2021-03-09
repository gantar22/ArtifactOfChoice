package artifactOfChoice.patches;

import artifactOfChoice.DefaultMod;
import artifactOfChoice.relics.ObtainRelicLater;
import basemod.BaseMod;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.RewardItem;

@SpirePatch(clz = AbstractRelic.class,method="bossObtainLogic",paramtypez = {})
public class AbstractRelicPatch {


    public static SpireReturn Prefix(AbstractRelic __Instance)
    {
        DefaultMod.relicReplacement.receiveRelicGet(__Instance,relic -> AbstractDungeon.effectsQueue.add(0, new ObtainRelicLater(relic)));
        return SpireReturn.Return(null);
    }
}
