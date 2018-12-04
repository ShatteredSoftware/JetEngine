package me.uberpilot.jetengine.language;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.JVM)
public class TestMessage
{
    private Message message;
    private Message defMessageEmpty;
    private Message defMessageNull;

    @Before
    public void setUp()
    {
        message = new Message("test_string1", "&6Hello World!", "&7Default Hello!");
        defMessageEmpty = new Message("test_string2", "", "&eHello World!");
        defMessageNull = new Message("test_string3", null, "&aHello World!");
    }

    @Test
    public void testMessageValid()
    {
        Assert.assertEquals("Get Value, Valid", "&6Hello World!", message.get());
        Assert.assertEquals("Get Value, Valid", "&7Default Hello!", message.getDefault());
        Assert.assertEquals("Get Value, Empty Default", "&6Hello World!", message.getOrDef("&8Hello!"));
    }

    @Test
    public void testMessageEmpty()
    {
        Assert.assertEquals("Get Value, Empty", "&eHello World!", defMessageEmpty.get());
        Assert.assertEquals("Get Value, Empty Default", "&8Hello!", defMessageEmpty.getOrDef("&8Hello!"));
    }

    @Test
    public void testMessageNull()
    {
        Assert.assertEquals("Get Value, Null", "&aHello World!", defMessageNull.get());
        Assert.assertEquals("Get Value, Null Default", "&8Hello!", defMessageNull.getOrDef("&8Hello!"));
    }

    @Test
    public void testMessageToString()
    {
        Assert.assertEquals("To String", "Message{id='test_string1', value='&6Hello World!', def='&7Default Hello!'}", message.toString());
    }

}
