package artifactOfChoice;


import artifactOfChoice.patches.CardRewardScreenPatch;
import basemod.interfaces.PostUpdateSubscriber;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;

import java.util.*;
import java.util.function.Consumer;

public class CardReplacement implements PostUpdateSubscriber {
    private enum State {WaitingForCard, PickingReplacement }
    private class Job
    {
        public AbstractCard cardToReplace;
        public Consumer<AbstractCard> continueWithNewCard;

        public Job(AbstractCard cardToReplace, Consumer<AbstractCard> continueWithNewCard)
        {
            this.cardToReplace = cardToReplace;
            this.continueWithNewCard = continueWithNewCard;
        }
    };

    private State state = State.WaitingForCard;

    private Queue<Job> m_TaskList = new java.util.LinkedList<Job>();

    public void ReplaceCard(AbstractCard cardToReplace, Consumer<AbstractCard> continueWithNewCard)
    {
        m_TaskList.add(new Job(cardToReplace,continueWithNewCard));
    }


    @Override
    public void receivePostUpdate()
    {
        switch (state)
        {
            case WaitingForCard:
                if(!m_TaskList.isEmpty())
                {
                    AbstractCard card = m_TaskList.peek().cardToReplace;

                    CardGroup group = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);

                    for(AbstractCard c : CardLibrary.cards.values())
                    {
                        if(c.rarity == card.rarity)
                        {
                            if(c.color == card.color || !DefaultMod.cardColorMatters)
                            {
                                if(c.type == card.type || !DefaultMod.cardTypeMatters)
                                {
                                    if(DefaultMod.showUnseenCards || !UnlockTracker.isCardSeen(c.cardID))
                                    {
                                        if(DefaultMod.showLockedCards || !UnlockTracker.isCardLocked(c.cardID))
                                        {
                                            if(!UnlockTracker.isCardSeen(c.cardID))
                                                UnlockTracker.markCardAsSeen(c.cardID);
                                            if(!UnlockTracker.isCardLocked(c.cardID));
                                            UnlockTracker.unlockCard(c.cardID);

                                            if(card.upgraded)
                                                c.upgrade();
                                            group.addToBottom(c);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    AbstractDungeon.previousScreen = AbstractDungeon.screen;
                    AbstractDungeon.gridSelectScreen.open(group, 1, "Pick a card!", false, false, false, false);

                    state = State.PickingReplacement;

                    AbstractDungeon.dynamicBanner.hide();
                }
                break;
            case PickingReplacement:
                if (!AbstractDungeon.gridSelectScreen.selectedCards.isEmpty()) {
                    AbstractCard c = ((AbstractCard)AbstractDungeon.gridSelectScreen.selectedCards.get(0)).makeStatEquivalentCopy();

                    c.inBottleFlame = false;
                    c.inBottleLightning = false;
                    c.inBottleTornado = false;
                    Consumer<AbstractCard> k = m_TaskList.poll().continueWithNewCard;
                    k.accept(c);
                    //AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(c, (float)Settings.WIDTH / 2.0F, (float) Settings.HEIGHT / 2.0F));
                    AbstractDungeon.gridSelectScreen.selectedCards.clear();
                    state = State.WaitingForCard;
                }
                break;
        }
    }
}
