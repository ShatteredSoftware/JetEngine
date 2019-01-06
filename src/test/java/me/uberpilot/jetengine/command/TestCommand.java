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

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JPlugin.class)
@SuppressWarnings("WeakerAccess")
public class TestCommand
{
    @Mock
    JPlugin plugin;
    @Mock
    CommandSender root;
    @Mock
    CommandSender noPerm;
    Messenger messenger;
    private Command command, child, child2, child3;
    private static final String name = "TestPlugin";

    @Before
    public void setUp()
    {

        MessageSet messages = new MessageSet();

        messenger = new Messenger(plugin, messages);

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
        messages.addMessage(new Message(name.toLowerCase() + "_cmd.test.description", "Gives basic information about " + name + "."));
        messages.addMessage(new Message(name.toLowerCase() + "_cmd.help.description", "Gives a list of commands from " + name + "."));
        messages.addMessage(new Message(name.toLowerCase() + "_cmd.test.two.feature_name", "the magical command"));

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
        child3 = new Command(plugin, null, "two", ((sender, label, args) -> false), "too");

        command.addChildren(child2, child3);
        command.addAliases("test1", "test2");
        command.addAlias("test3");
        child.addAlias("failed");

        child3.setDescription("Something else");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCommandNulls()
    {
        new Command(null, null, "label", (a, b, c) -> false);
        new Command(plugin, null, null, (a, b, c) -> false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCommandEmpties()
    {
        new Command(plugin, null, "", (a, b, c) -> false);
    }

    @Test
    public void testCommandAliases()
    {
        Assert.assertEquals("Child has correct alias", Collections.singletonList("failed"), child.getAliases());
        Assert.assertEquals("Command has correct aliases", Arrays.asList("test1", "test2", "test3"), command.getAliases());
        Assert.assertTrue("Child alias is linked to parent", command.getChildren().containsKey("failed"));
    }

    @Test
    public void testChildData()
    {
        Assert.assertEquals("Child has correct path", "test fail", child.getPath(' '));
        Assert.assertEquals("Child has correct path (String)", "test fail", child.getPath(" "));
        Assert.assertEquals("Child has correct lineage", command, child.getParent());
        Assert.assertEquals("Child has correct feature name", "the /test one command", child2.getFeatureName());
        Assert.assertEquals("Child has correct description", "TestPlugin /test one command.", child2.getDescription());
        Assert.assertEquals("Child permission is sub-permission of parent", "testplugin.test.fail", child.getPermission());
        Assert.assertEquals("Child sets description properly", "Something else", child3.getDescription());
        Assert.assertEquals("Child sets feature name properly", "the magical command", child3.getFeatureName());
    }

    @Test
    public void testCommandData()
    {
        Assert.assertEquals("Command has correct label", "test", command.getLabel());
        Assert.assertEquals("Command has correct children", child, command.getChildren().get("fail"));
        Assert.assertEquals("Command has correct description", "Gives basic information about TestPlugin.", command.getDescription());
        Assert.assertEquals("Command has correct permission", "testplugin.test", command.getPermission());
        Assert.assertEquals("Command has correct feature name", "the /test command", command.getFeatureName());
    }

    @Test
    public void testCommandMessageData()
    {
        Assert.assertEquals("Command has correct default description", "TestPlugin /test fail command.", child.getDescription());
        Assert.assertEquals("Command has correct feature name", "the /test fail command", child.getFeatureName());
    }

    @Test
    public void testExecute()
    {
        Assert.assertTrue("Sender with perms successfully executes", command.execute(root, "test", null));
        Assert.assertTrue("Sender with perms successfully executes (onCommand)", command.onCommand(root, command, "test", null));
        Assert.assertTrue("Sender with no perms 'successfully' executes", command.execute(noPerm, "test", null));
        Assert.assertFalse("Child commands execute correctly", command.execute(root, "test", new String[]{"fail"}));
        Assert.assertTrue("Help commands execute correctly", command.execute(root, "test", new String[]{"help"}));
        Assert.assertTrue("Nonexistent commands do not execute", command.execute(root, "test", new String[]{"blah"}));
        Assert.assertTrue("Sender with no perms 'successfully' executes command that should fail", command.execute(noPerm, "test", new String[]{"fail"}));
        Assert.assertFalse("Command with no children but with args executes correctly", child2.execute(root, "one", new String[]{"here", "are", "some", "args"}));
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
