package artifactOfChoice;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.PotionHelper;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Queue;
import java.util.function.Consumer;

public class PotionReplacement
{
    private void Initialize()
    {
        initialized = true;
        ArrayList<String> potionIds = PotionHelper.getPotions((AbstractPlayer.PlayerClass)null,true);
        for(String s : potionIds)
            allPotions.add(PotionHelper.getPotion(s));
    }

    private class Job
    {
        public Consumer<AbstractPotion> continuation;
        public Runnable startup;

        public Job(Consumer<AbstractPotion> continuation, Runnable startup)
        {
            this.continuation = continuation;
            this.startup = startup;
        }
    }

    private enum State { WaitingForRelic, PickingReplacement}

    private Queue<Job> workList = new java.util.LinkedList<Job>();
    private State currentState = State.WaitingForRelic;
    private ArrayList<AbstractPotion> allPotions = new ArrayList<>();

    private boolean relicSelected = false;
    private PotionSelectScreen potionselectScreen;
    private ArrayList<AbstractRelic> relicsToLose = new ArrayList<>();
    private boolean initialized = false;
    private Logger logger = LogManager.getLogger(RelicReplacement.class.getName());

    public void getPotion(AbstractPotion inPotion, Consumer<AbstractPotion> continuation)
    {
        if(!AbstractDungeon.isPlayerInDungeon() && AbstractDungeon.getCurrRoom() != null)
            return;
        ArrayList<AbstractPotion> potions = new ArrayList<AbstractPotion>();

        if(!initialized)
            Initialize();

        potions.addAll(allPotions);


        if(DefaultMod.relicColorMatters)
        {
            switch (AbstractDungeon.player.getCardColor())
            {
                case RED:
                    potions.removeIf(r -> RelicLibrary.greenList.contains(r));
                    potions.removeIf(r -> RelicLibrary.blueList.contains(r));
                    potions.removeIf(r -> RelicLibrary.whiteList.contains(r));
                    break;
                case GREEN:
                    potions.removeIf(r -> RelicLibrary.redList.contains(r));
                    potions.removeIf(r -> RelicLibrary.blueList.contains(r));
                    potions.removeIf(r -> RelicLibrary.whiteList.contains(r));
                    break;
                case BLUE:
                    potions.removeIf(r -> RelicLibrary.greenList.contains(r));
                    potions.removeIf(r -> RelicLibrary.redList.contains(r));
                    potions.removeIf(r -> RelicLibrary.whiteList.contains(r));
                    break;
                case PURPLE:
                    potions.removeIf(r -> RelicLibrary.greenList.contains(r));
                    potions.removeIf(r -> RelicLibrary.blueList.contains(r));
                    potions.removeIf(r -> RelicLibrary.redList.contains(r));
                    break;
            }
        }


        ArrayList<AbstractPotion> finalPotionList = new ArrayList<>();
        for(AbstractPotion potion : potions)
        {
            if(potion != null  && !finalPotionList.contains(potion))
            {
                if(potion.rarity == inPotion.rarity)
                finalPotionList.add(potion);
            }
        }


        if(finalPotionList.size() > 0)
        {
            if (AbstractDungeon.isScreenUp) {
                AbstractDungeon.dynamicBanner.hide();
                AbstractDungeon.overlayMenu.cancelButton.hide();
                AbstractDungeon.previousScreen = AbstractDungeon.screen;
            }
            AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.INCOMPLETE;
            relicSelected = false;
            AbstractDungeon.closeCurrentScreen();

            workList.add(new Job(continuation,() -> {
                potionselectScreen = new PotionSelectScreen();
                potionselectScreen.open(finalPotionList);
            }));
        }
    }

    public void receivePostUpdate()
    {
        for (int i = 0, relicsToLoseSize = relicsToLose.size(); i < relicsToLoseSize; i++) {
            AbstractRelic naturalRelic = relicsToLose.get(i);
            if (naturalRelic != null && naturalRelic.isObtained) {
                if (AbstractDungeon.isPlayerInDungeon() && AbstractDungeon.player != null)
                    AbstractDungeon.player.loseRelic(naturalRelic.relicId);
                switch (naturalRelic.tier) {
                    case COMMON:
                        AbstractDungeon.commonRelicPool.add(naturalRelic.relicId);
                        break;
                    case UNCOMMON:
                        AbstractDungeon.uncommonRelicPool.add(naturalRelic.relicId);
                        break;
                    case RARE:
                        AbstractDungeon.rareRelicPool.add(naturalRelic.relicId);
                        break;
                    case SHOP:
                        AbstractDungeon.shopRelicPool.add(naturalRelic.relicId);
                        break;
                    case BOSS:
                        AbstractDungeon.bossRelicPool.add(naturalRelic.relicId);
                        break;
                }
            }
        }
        relicsToLose.clear();

        switch (currentState)
        {
            case WaitingForRelic:
                if(!workList.isEmpty())
                {
                    workList.peek().startup.run();
                    currentState = State.PickingReplacement;
                }

                break;
            case PickingReplacement:
                if (potionselectScreen.doneSelecting())
                {
                    currentState = State.WaitingForRelic;

                    AbstractPotion relic = potionselectScreen.getSelectedRelics().get(0).makeCopy();


                    workList.poll().continuation.accept(relic);

                    AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMPLETE;
                    potionselectScreen = null;
                } else {
                    potionselectScreen.update();
                }
                break;
        }

        if (!relicSelected && potionselectScreen != null)
        {
            //uniqueness
            //breaks on events?
        }

    }

    public void render(SpriteBatch sb) {
        if(potionselectScreen != null)
            potionselectScreen.render(sb);
    }

    public void onShutdown()
    {
        if(potionselectScreen != null)
        {
            potionselectScreen.close();
            potionselectScreen = null;

            workList.clear();
            currentState = State.WaitingForRelic;
        }
    }
}
