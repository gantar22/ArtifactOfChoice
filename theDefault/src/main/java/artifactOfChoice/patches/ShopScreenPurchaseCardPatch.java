package artifactOfChoice.patches;

import artifactOfChoice.CardReplacement;
import artifactOfChoice.DefaultMod;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.shop.ShopScreen;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;

@SpirePatch(clz = ShopScreen.class,method="purchaseCard",paramtypez = {AbstractCard.class})
public class ShopScreenPurchaseCardPatch {

    public static HashSet<AbstractCard> m_CardsToAllow = new HashSet<AbstractCard>();
    public static SpireReturn Prefix(ShopScreen __Instance, AbstractCard hoveredCard)
    {
        if(m_CardsToAllow.contains(hoveredCard))
        {
            m_CardsToAllow.remove(hoveredCard);
            return SpireReturn.Continue();
        }

        int price = hoveredCard.price;
        DefaultMod.cardReplacement.ReplaceCard(hoveredCard,c ->
        {
            try {
                m_CardsToAllow.add(c);

                c.price = price;

                Method purchaseCard =
                ShopScreen.class
                        .getDeclaredMethod("purchaseCard", AbstractCard.class);

                purchaseCard
                    .setAccessible(true);

                c.current_x = hoveredCard.current_x;
                c.current_y = hoveredCard.current_y;
                c.target_x = c.current_x;
                c.target_y = c.current_y;
                if(hoveredCard.color == AbstractCard.CardColor.COLORLESS)
                {
                    __Instance.colorlessCards.set(__Instance.colorlessCards.indexOf(hoveredCard), c);
                } else {
                    __Instance.coloredCards.set(__Instance.coloredCards.indexOf(hoveredCard), c);
                }

                purchaseCard.invoke(__Instance,c);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
        return SpireReturn.Return(null);
    }
}
