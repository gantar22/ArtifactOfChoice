package artifactOfChoice;

import artifactOfChoice.relics.ObtainRelicLater;
import basemod.BaseMod;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.BlackBlood;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import com.megacrit.cardcrawl.unlock.UnlockTracker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

public class RelicReplacement
{
    private class Job
    {
    public Consumer<AbstractRelic> continuation;
    public Runnable startup;

    public Job(Consumer<AbstractRelic> continuation, Runnable startup)
    {
        this.continuation = continuation;
        this.startup = startup;
    }
    }

    private enum State { WaitingForRelic, PickingReplacement}

    private Queue<Job> workList = new java.util.LinkedList<Job>();
    private State currentState = State.WaitingForRelic;

    private boolean relicSelected = false;
    private RelicSelectScreen relicSelectScreen;
    private ArrayList<AbstractRelic> relicsToLose = new ArrayList<>();
    private Logger logger = LogManager.getLogger(RelicReplacement.class.getName());

    public void receiveRelicGet(AbstractRelic inRelic, Consumer<AbstractRelic> continuation)
    {
        if(!AbstractDungeon.isPlayerInDungeon() && AbstractDungeon.getCurrRoom() != null)
            return;
        ArrayList<AbstractRelic> relics = new ArrayList<AbstractRelic>();

        switch (inRelic.tier)
        {
            case STARTER:
                relics.addAll(RelicLibrary.starterList);
                break;
            case COMMON:
                relics.addAll(RelicLibrary.commonList);
                break;
            case UNCOMMON:
                relics.addAll(RelicLibrary.uncommonList);
                break;
            case RARE:
                relics.addAll(RelicLibrary.rareList);
                break;
            case SPECIAL:
                relics.addAll(RelicLibrary.specialList);
                break;
            case BOSS:
                relics.addAll(RelicLibrary.bossList);
                break;
            case SHOP:
                relics.addAll(RelicLibrary.shopList);
                break;
        }

        if(DefaultMod.relicColorMatters)
        {
            switch (AbstractDungeon.player.getCardColor())
            {
                case RED:
                    relics.removeIf(r -> RelicLibrary.greenList.contains(r));
                    relics.removeIf(r -> RelicLibrary.blueList.contains(r));
                    relics.removeIf(r -> RelicLibrary.whiteList.contains(r));
                    break;
                case GREEN:
                    relics.removeIf(r -> RelicLibrary.redList.contains(r));
                    relics.removeIf(r -> RelicLibrary.blueList.contains(r));
                    relics.removeIf(r -> RelicLibrary.whiteList.contains(r));
                    break;
                case BLUE:
                    relics.removeIf(r -> RelicLibrary.greenList.contains(r));
                    relics.removeIf(r -> RelicLibrary.redList.contains(r));
                    relics.removeIf(r -> RelicLibrary.whiteList.contains(r));
                    break;
                case PURPLE:
                    relics.removeIf(r -> RelicLibrary.greenList.contains(r));
                    relics.removeIf(r -> RelicLibrary.blueList.contains(r));
                    relics.removeIf(r -> RelicLibrary.redList.contains(r));
                    break;
            }
        }


        ArrayList<AbstractRelic> finalRelicList = new ArrayList<>();
        for(AbstractRelic relic : relics)
        {
            if(relic != null  && !finalRelicList.contains(relic))
            {
                if(DefaultMod.allowRelicRepeats || !AbstractDungeon.player.hasRelic(relic.relicId))
                {
                    if(DefaultMod.showUnseenRelics || UnlockTracker.isRelicSeen(relic.relicId))
                    {
                        if(DefaultMod.showLockedRelics || UnlockTracker.isRelicLocked(relic.relicId))
                        {
                            if(!DefaultMod.onlySpawnableRelics || relic.canSpawn())
                            {
                                if(!UnlockTracker.isRelicSeen(relic.relicId))
                                    UnlockTracker.markRelicAsSeen(relic.relicId);
                                finalRelicList.add(relic);
                            }
                        }
                    }
                }
            }
        }


        if(finalRelicList.size() > 0)
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
                relicSelectScreen = new RelicSelectScreen();
                relicSelectScreen.open(finalRelicList);
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
                if (relicSelectScreen.doneSelecting())
                {

                currentState = State.WaitingForRelic;

                AbstractRelic relic = relicSelectScreen.getSelectedRelics().get(0).makeCopy();
                switch (relic.tier) {
                    case COMMON:
                        AbstractDungeon.commonRelicPool.removeIf(id -> id.equals(relic.relicId));
                        break;
                    case UNCOMMON:
                        AbstractDungeon.uncommonRelicPool.removeIf(id -> id.equals(relic.relicId));
                        break;
                    case RARE:
                        AbstractDungeon.rareRelicPool.removeIf(id -> id.equals(relic.relicId));
                        break;
                    case SHOP:
                        AbstractDungeon.shopRelicPool.removeIf(id -> id.equals(relic.relicId));
                        break;
                    case BOSS:
                        AbstractDungeon.bossRelicPool.removeIf(id -> id.equals(relic.relicId));
                        break;
                }

                if (relic.relicId.equals("HolyWater") || relic.relicId.equals("Black Blood") ||relic.relicId.equals("Ring of the Serpent") || relic.equals("FrozenCore"))
                {
                    {
                        AbstractRelic relicToRemove = null;
                        for (AbstractRelic r : AbstractDungeon.player.relics) {
                            if (r.tier == AbstractRelic.RelicTier.STARTER) {
                                relicToRemove = r;
                            }
                        }
                        if (relicToRemove != null)
                            AbstractDungeon.player.loseRelic(relicToRemove.relicId);
                    }
                }

                workList.poll().continuation.accept(relic);
                //AbstractDungeon.effectsQueue.add(0, new ObtainRelicLater(relic));

                AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMPLETE;
                relicSelectScreen = null;
            } else {
                relicSelectScreen.update();
            }
                break;
        }

        if (!relicSelected && relicSelectScreen != null)
        {
            //uniqueness
            //breaks on events?
        }

    }

    public void render(SpriteBatch sb) {
        if(relicSelectScreen != null)
            relicSelectScreen.render(sb);
    }

    public void onShutdown()
    {
        if(relicSelectScreen != null)
        {
            relicSelectScreen.close();
            relicSelectScreen = null;

            workList.clear();
            currentState = State.WaitingForRelic;
        }
    }
}
