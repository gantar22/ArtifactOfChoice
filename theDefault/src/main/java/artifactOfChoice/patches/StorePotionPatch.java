package artifactOfChoice.patches;


import artifactOfChoice.DefaultMod;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.screens.runHistory.CopyableTextElement;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StorePotion;
import com.megacrit.cardcrawl.shop.StoreRelic;

import java.lang.reflect.Field;
import java.util.HashSet;

@SpirePatch(clz = StorePotion.class,method="purchasePotion",paramtypez = {})
public class StorePotionPatch {

    private static HashSet<AbstractPotion> abstractPotions = new HashSet<AbstractPotion>();

    public static SpireReturn Prefix(StorePotion __Instance)
    {
        if(abstractPotions.contains(__Instance.potion))
        {
            return SpireReturn.Continue();
        }
        DefaultMod.potionReplacement.getPotion(__Instance.potion, potion ->
        {
            try {
                Field shopScreenField = StorePotion.class.getDeclaredField("shopScreen");

                shopScreenField.setAccessible(true);

                ShopScreen shopScreen = (ShopScreen) shopScreenField.get(__Instance);

                StorePotion newRelic = new StorePotion(potion,0, shopScreen);
                newRelic.price = __Instance.price;
                __Instance.isPurchased = true;

                abstractPotions.add(potion);
                newRelic.purchasePotion();
                shopScreen.open();
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });



        return SpireReturn.Return(null);
    }
}
