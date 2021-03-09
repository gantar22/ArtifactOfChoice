package artifactOfChoice.patches;


import artifactOfChoice.DefaultMod;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StoreRelic;

import java.lang.reflect.Field;
import java.util.HashSet;

@SpirePatch(clz = StoreRelic.class,method="purchaseRelic",paramtypez = {})
public class StoreRelicPatch
{
    private static HashSet<AbstractRelic> relicsToAllow = new HashSet<AbstractRelic>();

    public static SpireReturn Prefix(StoreRelic __Instance)
    {
        if(relicsToAllow.contains(__Instance.relic))
        {
            return SpireReturn.Continue();
        }
        DefaultMod.relicReplacement.receiveRelicGet(__Instance.relic,relic ->
        {
            try {
                Field shopScreenField = StoreRelic.class.getDeclaredField("shopScreen");

                shopScreenField.setAccessible(true);

                ShopScreen shopScreen = (ShopScreen) shopScreenField.get(__Instance);

                StoreRelic newRelic = new StoreRelic(relic,0, shopScreen);
                newRelic.price = __Instance.price;
                __Instance.isPurchased = true;

                relicsToAllow.add(relic);
                newRelic.purchaseRelic();
                shopScreen.open();
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });



        return SpireReturn.Return(null);
    }
}
