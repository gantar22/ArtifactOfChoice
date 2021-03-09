package artifactOfChoice.patches;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.vfx.FastCardObtainEffect;
import javassist.CtBehavior;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.smartcardio.Card;
import java.util.List;


//@SpirePatch(clz = FastCardObtainEffect.class,method=SpirePatch.CONSTRUCTOR,paramtypez = {AbstractCard.class,float.class,float.class})
public class FastCardAddPatch
{
    static boolean isPickingCardReplacement = false; //will not work for obtaining multiple cards TODO rework later

    public static CardLibrary.LibraryType CardColorToLibraryType(AbstractCard.CardColor inColor)
    {
        switch (inColor)
        {
            case RED:
                return CardLibrary.LibraryType.RED;
                case GREEN:
                return CardLibrary.LibraryType.GREEN;
            case BLUE:
                return CardLibrary.LibraryType.BLUE;
            case PURPLE:
                return  CardLibrary.LibraryType.PURPLE;
            case COLORLESS:
                return CardLibrary.LibraryType.COLORLESS;
            case CURSE:
                return CardLibrary.LibraryType.CURSE;
        }
        return CardLibrary.LibraryType.COLORLESS;
    }


    public static void Prefix(FastCardObtainEffect __instance, AbstractCard card, float x, float y)
    {
        if(isPickingCardReplacement)
        {
            isPickingCardReplacement = false;
            return;
        }
        isPickingCardReplacement = true;
        CardGroup group = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
        switch (card.rarity)
        {
            case BASIC:
                List<AbstractCard> allCards = CardLibrary.getCardList(CardColorToLibraryType(card.color));
                for ( AbstractCard c : allCards) {
                    if(c.rarity == AbstractCard.CardRarity.BASIC)
                        group.addToBottom(c);
                }
                break;
            case SPECIAL:
                List<AbstractCard> allCards2 = CardLibrary.getCardList(CardColorToLibraryType(card.color));
                for ( AbstractCard c : allCards2) {
                    if(c.rarity == AbstractCard.CardRarity.SPECIAL)
                        group.addToBottom(c);
                }
                break;
            case COMMON:
                for(AbstractCard c : AbstractDungeon.commonCardPool.group)
                {
                    group.addToBottom(c); // may need some work
                }
                break;
            case UNCOMMON:
                for(AbstractCard c : AbstractDungeon.uncommonCardPool.group)
                {
                    group.addToBottom(c); // may need some work
                }
                break;
            case RARE:
                for(AbstractCard c : AbstractDungeon.rareCardPool.group)
                {
                    group.addToBottom(c); // may need some work
                }
                break;
            case CURSE:
                for(AbstractCard c : AbstractDungeon.curseCardPool.group)
                {
                    group.addToBottom(c); // may need some work
                }
                break;
        }
        AbstractDungeon.gridSelectScreen.open(group,1,false,"localize me");
    }
}
