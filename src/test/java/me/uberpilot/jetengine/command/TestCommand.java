package me.uberpilot.jetengine.command;

import me.uberpilot.jetengine.JPlugin;
import me.uberpilot.jetengine.language.Message;
import me.uberpilot.jetengine.language.MessageSet;
import me.uberpilot.jetengine.messenger.Messenger;
import org.bukkit.command.CommandSender;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JPlugin.class)
public class TestCommand
{
    @Mock
    JPlugin plugin;
    @Mock
    CommandSender root;
    @Mock
    CommandSender noPerm;
    private Command command, child, child2, child3;
    private static final String name = "TestPlugin";

    @Before
    public void setUp()
    {

        MessageSet messages = new MessageSet(plugin);

        Messenger messenger = new Messenger(plugin, messages);

        messages.addMessage(new Message("core.prefix", "$sc[$pc" + name + "$sc]"));
        messages.addMessage(new Message("core.no_permission", "$pre &cYou don't have permission to use %s"));
        messages.addMessage(new Message("core.invalid_args", "$pre &cInvalid argument."));
        messages.addMessage(new Message("core.help_line", "  $pc/%s $tc- $sc%s"));
        messages.addMessage(new Message("core.help_header", "$scHelp for $pc%s$sc:"));
        messages.addMessage(new Message("core.cmd_help_header", "$sc%s Help$tc - $pc/%s"));
        messages.addMessage(new Message("core.info",
                "$tc&m--------------------------------------\n" +
                        "$pc%s-%s $scby $pc%s\n" +
                        "$tc&m--------------------------------------\n" +
                        "$pcWebsite$tc: $sc%s\n" +
                        "$tcUse $sc/" + name.toLowerCase() + " help $tcfor a list of commands."));
        messages.addMessage(new Message("core.list_separator", ","));
        messages.addMessage(new Message("core.list_and", "and"));
        messages.addMessage(new Message(name.toLowerCase() + "_cmd.test", "Gives basic information about " + name + "."));
        messages.addMessage(new Message(name.toLowerCase() + "_cmd.help", "Gives a list of commands from " + name + "."));

        plugin = PowerMockito.mock(JPlugin.class);
        when(plugin.getMessages()).thenReturn(messages);
        when(plugin.getMessenger()).thenReturn(messenger);
        when(plugin.getName())
                .thenReturn(name);

        root = Mockito.mock(CommandSender.class);
        when(root.hasPermission(anyString())).thenReturn(true);

        noPerm = Mockito.mock(CommandSender.class);
        when(noPerm.hasPermission(anyString())).thenReturn(false);

        command = new Command(plugin, null, "test", ((sender, label, args) -> true));

        child = new Command(plugin, command, "fail", ((sender, label, args) -> false));

        child2 = new Command(plugin, null, "one", ((sender, label, args) -> false));
        child3 = new Command(plugin, null, "two", ((sender, label, args) -> false));

        command.addChildren(child2, child3);

        child3.setDescription("Something else");
    }

    @Test
    public void testCommandData()
    {
        Assert.assertEquals("Implicit Tests", "", "");
        Assert.assertEquals("Command has correct label", "test", command.getLabel());
        Assert.assertEquals("Command has correct children", child, command.getChildren().get("fail"));
        Assert.assertEquals("Command has correct description", "Gives basic information about TestPlugin.", command.getDescription());
        Assert.assertEquals("Command has correct permission", "testplugin.test", command.getPermission());
        Assert.assertEquals("Child has correct path", "test fail", child.getPath(' '));
        Assert.assertEquals("Child has correct path (String)", "test fail", child.getPath(" "));
        Assert.assertEquals("Child has correct lineage", command, child.getParent());
        Assert.assertEquals("Child permission is sub-permission of parent", "testplugin.test.fail", child.getPermission());
        Assert.assertEquals("SetDescription", "Something else", child3.getDescription());
    }

    @Test
    public void testExecute()
    {
        Assert.assertTrue("Sender with perms successfully executes", command.execute(root, "test", null));
        Assert.assertTrue("Sender with no perms 'successfully' executes", command.execute(noPerm, "test", null));
        Assert.assertFalse("Child commands execute correctly", command.execute(root, "test", new String[]{"fail"}));
        Assert.assertTrue("Sender with no perms 'successfully' executes command that should fail", command.execute(noPerm, "test", new String[]{"fail"}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCommandNullLabel()
    {
        Assert.assertTrue("Null label throws an error.", command.execute(root, null, null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCommandNullSender()
    {
        Assert.assertFalse("Null sender throws an error.", command.execute(null, "test", null));
    }
}
