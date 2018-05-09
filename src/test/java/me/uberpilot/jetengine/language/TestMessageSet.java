package me.uberpilot.jetengine.language;

import me.uberpilot.jetengine.JPlugin;
import org.bukkit.ChatColor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

import java.util.MissingFormatArgumentException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestMessageSet
{
    private MessageSet set;
    private MessageSet setColors;

    @Before
    public void setUp()
    {
        JPlugin plugin = Mockito.mock(JPlugin.class);


        set = new MessageSet(plugin);
        setColors = new MessageSet(plugin, ChatColor.DARK_RED.toString(), ChatColor.RED.toString(), ChatColor.DARK_GRAY.toString());

        set.addMessage(new Message("prefix",""));
        set.addMessage(new Message("greeting", "$scHello, $pc%s$sc!", "$scHello null!"));
        set.addMessage(new Message("def_msg", "", "$scWelcome to $pcjetSuite$sc!"));
        setColors.addMessage(new Message("prefix",""));
        setColors.addMessage(new Message("greeting", "$scHello, $pc%s$sc!", "$scHello null!"));
        setColors.addMessage(new Message("def_msg", "", "$scWelcome to $pcjetSuite$sc!"));
    }

    @Test
    public void testMessageSetColored()
    {
        Assert.assertEquals("Message Set: Check Default Primary Color", ChatColor.DARK_GREEN.toString(), set.getPrimaryColor());
        Assert.assertEquals("Message Set: Check Default Secondary Color", ChatColor.GRAY.toString(), set.getSecondaryColor());
        Assert.assertEquals("Message Set: Check Default Tertiary Color", ChatColor.DARK_GRAY.toString(), set.getTertiaryColor());
    }

    @Test
    public void testMessageSetReplace()
    {
        Assert.assertEquals("Message Set: Replace", "\u00a77Hello, \u00a72UberPilot\u00a77!",
                set.getMessage("greeting", "UberPilot"));
    }

    @Test(expected = MissingFormatArgumentException.class)
    public void testMessageSetNoReplace()
    {
        Assert.assertEquals("Message Set: Fails Without Args", "\u00a77Hello, \u00a72null\u00a77!", set.getMessage("greeting"));
    }

    @Test
    public void testMessageSetDefault()
    {
        Assert.assertEquals("Message Set: Get Default", "\u00a77Welcome to \u00a72jetSuite\u00a77!", set.getMessage("def_msg"));
    }

    @Test
    public void testMessageSetColorsColored()
    {
        Assert.assertEquals("Message Set Colored: Check Primary Color", ChatColor.DARK_RED.toString(), setColors.getPrimaryColor());
        Assert.assertEquals("Message Set Colored: Check Secondary Color", ChatColor.RED.toString(), setColors.getSecondaryColor());
        Assert.assertEquals("Message Set Colored: Check Tertiary Color", ChatColor.DARK_GRAY.toString(), setColors.getTertiaryColor());
    }

    @Test
    public void testMessageSetColorsWithReplace()
    {
        Assert.assertEquals("Message Set Colored: Replace", "\u00a7cHello, \u00a74UberPilot\u00a7c!", setColors.getMessage("greeting", "UberPilot"));
    }

    @Test(expected = MissingFormatArgumentException.class)
    public void testMessageSetColorsNoReplace()
    {
        Assert.assertEquals("Message Set Colored: Fails Without Args", "\u00a7cHello, \u00a74null\u00a7c!", setColors.getMessage("greeting"));
    }

    @Test
    public void testMessageSetColorsDefault()
    {
        Assert.assertEquals("Message Set Colored: Get Default", "\u00a7cWelcome to \u00a74jetSuite\u00a7c!", setColors.getMessage("def_msg"));
    }

}
