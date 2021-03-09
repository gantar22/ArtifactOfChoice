package artifactOfChoice.patches;
import artifactOfChoice.DefaultMod;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.red.Strike_Red;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.screens.CardRewardScreen;
import com.megacrit.cardcrawl.vfx.FastCardObtainEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import javassist.CtBehavior;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.smartcardio.Card;
import java.util.List;


@SpirePatch(clz = CardRewardScreen.class,method="acquireCard",paramtypez = {AbstractCard.class})
public class CardRewardScreenPatch {
    public static AbstractCard gotCard = null;

    public static SpireReturn Prefix(CardRewardScreen __Instance, AbstractCard hoveredCard)
    {
        InputHelper.justClickedLeft = false;
        DefaultMod.cardReplacement.ReplaceCard(hoveredCard, c ->
                AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(c,
                    (float) Settings.WIDTH / 2.0F, (float) Settings.HEIGHT / 2.0F))
        );

        return SpireReturn.Return(null);
    }
}
