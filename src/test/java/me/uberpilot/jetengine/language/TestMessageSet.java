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
    private MessageSet clearSet;
    private MessageSet setColors;
    private MessageSet preSet;

    @Before
    public void setUp()
    {
        set = new MessageSet();
        setColors = new MessageSet(ChatColor.DARK_RED.toString(), ChatColor.RED.toString(), ChatColor.DARK_GRAY.toString());
        clearSet = new MessageSet();
        preSet = new MessageSet(ChatColor.DARK_RED.toString(), ChatColor.RED.toString(), ChatColor.DARK_GRAY.toString(),
                new Message("tm", "Test Message"), new Message("pref_test", "$pre"));

        set.addMessage(new Message("core.prefix",""));
        set.addMessage(new Message("greeting", "$scHello, $pc%s$sc!", "$scHello null!"));
        set.addMessage(new Message("def_msg", "", "$scWelcome to $pcjetSuite$sc!"));
        clearSet.addMessage(new Message("greeting", "$scHello, $pc%s$sc!", "$scHello null!"));
        clearSet.addMessage(new Message("def_msg", "", "$scWelcome to $pcjetSuite$sc!"));
        setColors.addMessage(new Message("core.prefix",""));
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
    public void testPrefixCase()
    {
        Assert.assertEquals("Test Message", preSet.getMessage("tm"));
        Assert.assertEquals("$pre", preSet.getMessage("pref_test"));
    }

    @Test
    public void testAddMessage()
    {
        set.addMessage(new Message("add_test", "Add", "Add"));
        set.addMessage(new Message("add_test", "Nope", "Nope"), false);
        Assert.assertEquals("Test Add-false Does Not Replace", "Add", set.getMessage("add_test"));
        set.addMessage(new Message("add_test", "Yes", "Yes"), true);
        Assert.assertEquals("Test Add-false Does Not Replace", "Yes", set.getMessage("add_test"));
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
    public void testMessageSetRaw()
    {
        Assert.assertEquals("Message Set: Get Raw", "$scWelcome to $pcjetSuite$sc!", set.getRawMessage("def_msg"));
        Assert.assertEquals("Message Set: Get Raw Default", set.getRawDefault("greeting"), "$scHello null!");
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

    @Test
    public void testMessageSetIterator()
    {
        Assert.assertNotNull(set.iterator());
    }

    @Test
    public void testMessageSetToString()
    {
        Assert.assertEquals("To String", "MessageSet{primaryColor='\u00a72', secondaryColor='\u00a77', tertiaryColor='\u00a78', messages={def_msg=Message{id='def_msg', value='', def='$scWelcome to $pcjetSuite$sc!'}, greeting=Message{id='greeting', value='$scHello, $pc%s$sc!', def='$scHello null!'}, core.prefix=Message{id='core.prefix', value='null', def=''}}}", set.toString());
    }

    @Test
    public void testMessageSetClear()
    {
        clearSet.clear();
        Assert.assertFalse("Clear",  clearSet.iterator().hasNext());
    }

    @Test
    public void testSetMessageColors()
    {
        String primary = setColors.getPrimaryColor();
        String secondary = setColors.getSecondaryColor();
        String tertiary = setColors.getTertiaryColor();
        setColors.setPrimaryColor(ChatColor.GOLD.toString());
        setColors.setSecondaryColor(ChatColor.YELLOW.toString());
        setColors.setTertiaryColor(ChatColor.GRAY.toString());
        Assert.assertEquals("Message Set Color Change: Check Primary Color", ChatColor.GOLD.toString(), setColors.getPrimaryColor());
        Assert.assertEquals("Message Set Color Change: Check Secondary Color", ChatColor.YELLOW.toString(), setColors.getSecondaryColor());
        Assert.assertEquals("Message Set Color Change: Check Tertiary Color", ChatColor.GRAY.toString(), setColors.getTertiaryColor());
        setColors.setPrimaryColor(primary);
        setColors.setSecondaryColor(secondary);
        setColors.setTertiaryColor(tertiary);
    }

}
