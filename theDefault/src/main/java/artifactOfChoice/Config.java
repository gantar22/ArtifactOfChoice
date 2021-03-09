package artifactOfChoice;

import artifactOfChoice.util.TextureLoader;
import basemod.BaseMod;
import basemod.ModLabeledToggleButton;
import basemod.ModPanel;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;

import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Config {

    class CheckBoxData
    {
        public String name;
        public String description;
        public Boolean defaultValue;

        public Consumer<Boolean> setter;
        public Supplier<Boolean> getter;


        public CheckBoxData(String name, String description, Boolean defaultValue, Consumer<Boolean> setter, Supplier<Boolean> getter)
        {
            this.name = name;
            this.description = description;
            this.defaultValue = defaultValue;
            this.setter = setter;
            this.getter = getter;
        }

    }

    public CheckBoxData[] data = new CheckBoxData[]
    {
            new CheckBoxData("showUnseenCards","Show cards that haven't been seen yet.",true,b -> DefaultMod.showUnseenCards = b, () -> DefaultMod.showUnseenCards),
            new CheckBoxData("showUnseenRelics","Show relics that haven't been seen yet.",true,b -> DefaultMod.showUnseenRelics = b, () -> DefaultMod.showUnseenRelics),
            new CheckBoxData("showLockedCards","Show cards that are still locked.",true, b -> DefaultMod.showLockedCards = b, () -> DefaultMod.showLockedCards),
            new CheckBoxData("cardTypeMatters","Show only cards that are of the same type as the selected card.",true, b -> DefaultMod.cardTypeMatters = b, () -> DefaultMod.cardTypeMatters),
            new CheckBoxData("allowRelicRepeats","Show relics that have already been chosen.",false, b -> DefaultMod.allowRelicRepeats = b, () -> DefaultMod.allowRelicRepeats),
            new CheckBoxData("relicColorMatters","Show only relics of the same color of the selected relic.",true, b -> DefaultMod.relicColorMatters = b, () -> DefaultMod.relicColorMatters),
            new CheckBoxData("cardColorMatters","Show only cards of the same color of the selected card.",true, b -> DefaultMod.cardColorMatters = b, () -> DefaultMod.cardColorMatters),
            new CheckBoxData("showLockedRelics","Show relics that are locked.",true, b -> DefaultMod.showLockedRelics = b,() -> DefaultMod.showLockedRelics),
            new CheckBoxData("onlySpawnableRelics","Show only relics that could spawn.",true, b -> DefaultMod.onlySpawnableRelics = b,() -> DefaultMod.onlySpawnableRelics),

    };

    public String modName = "artifactOfChoice";
    public String fileName = "artifactOfChoiceConfig";

    public SpireConfig spireConfig;
    public Properties defaultSettings;

    public Config()
    {
        defaultSettings = new Properties();

        try {
            for(int i = 0; i < data.length; i++)
            {
                defaultSettings.put(data[i].name,Boolean.toString(data[i].defaultValue));//causes null ref!
            }


            spireConfig = new SpireConfig(modName, fileName, defaultSettings); // ...right here
            // the "fileName" parameter is the name of the file MTS will create where it will save our setting.
            spireConfig.load(); // Load the setting and set the boolean to equal it


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void receivePostInitialize()
    {
        // Load the Mod Badge
        Texture badgeTexture = TextureLoader.getTexture(DefaultMod.BADGE_IMAGE);

        // Create the Mod Menu
        ModPanel settingsPanel = new ModPanel();

        float yPos = 700.0f;
        for(int i = 0; i < data.length; i++)
        {
            CheckBoxData datum = data[i];
            ModLabeledToggleButton button = new ModLabeledToggleButton(datum.description,
                    350.0f, yPos, Settings.CREAM_COLOR, FontHelper.charDescFont, // Position (trial and error it), color, font
                    datum.getter.get(), // Boolean it uses
                    settingsPanel, // The mod panel in which this button will be in
                    (label) -> {}, // thing??????? idk
                    (b) -> { // The actual button:

                        datum.setter.accept(b.enabled); // The boolean true/false will be whether the button is enabled or not
                        try {
                            // And based on that boolean, set the settings and save them

                            spireConfig.setBool(datum.name, datum.getter.get());
                            spireConfig.save();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
            yPos -= 50.0f;

            settingsPanel.addUIElement(button);
        }

        BaseMod.registerModBadge(badgeTexture, DefaultMod.MODNAME, DefaultMod.AUTHOR, DefaultMod.DESCRIPTION, settingsPanel);
    }
}

