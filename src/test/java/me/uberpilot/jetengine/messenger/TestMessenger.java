package me.uberpilot.jetengine.messenger;

import me.uberpilot.jetengine.JPlugin;
import me.uberpilot.jetengine.command.Command;
import me.uberpilot.jetengine.language.Message;
import me.uberpilot.jetengine.language.MessageSet;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JPlugin.class, org.bukkit.Bukkit.class})
@SuppressWarnings("WeakerAccess")
public class TestMessenger
{
    @Mock
    ConsoleCommandSender console;
    @Mock
    Player player;
    @Mock
    JPlugin plugin;
    @Mock
    BukkitScheduler scheduler;
    Messenger msgr;
    Command cmd;
    Command chld;
    private static final String name = "TestPlugin";

    @Before
    public void setUp()
    {
        console = Mockito.mock(ConsoleCommandSender.class);
        player = Mockito.mock(Player.class);
        when(player.hasPermission(anyString())).thenReturn(false);
        when(console.hasPermission(anyString())).thenReturn(true);
        plugin = PowerMockito.mock(JPlugin.class);
        scheduler = PowerMockito.mock(BukkitScheduler.class);
        PowerMockito.mockStatic(Bukkit.class, invocation -> scheduler);
        when(scheduler.scheduleSyncDelayedTask(any(Plugin.class), any(Runnable.class), anyLong())).then(invocation ->
        {
            ((Runnable) invocation.getArgument(1)).run();
            return 0;
        });

        MessageSet set = new MessageSet();
        set.addMessage(new Message("m", "x"));
        set.addMessage(new Message("n", ""));
        set.addMessage(new Message("core.help_line", "  $pc/%s $tc- $sc%s"));
        set.addMessage(new Message("core.help_header", "$scHelp for $pc%s$sc:"));
        set.addMessage(new Message("core.cmd_help_header", "$sc%s Help$tc - $pc/%s"));

        when(plugin.getMessages())
                .thenReturn(set);
        when(plugin.getName())
                .thenReturn(name);

        cmd = new Command(plugin, null, "cmd", (a, b, c) -> true, "c");
        chld = new Command(plugin, cmd, "chld", (a, b, c) -> true, "ch");
        msgr = new Messenger(plugin, set);
    }

    @Test
    public void testRuns()
    {
        msgr.sendImportantMessage(player, "m");
        msgr.sendImportantMessage(console, "m");
        msgr.sendMessage(player, "m");
        msgr.sendMessage(console, "m");
        msgr.sendErrorMessage(player, "m");
        msgr.sendErrorMessage(console, "m");
        msgr.sendCommandHelp(player, cmd);
        msgr.sendCommandHelp(console, cmd);
        msgr.sendMessage(player, "n");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testErrors()
    {
        msgr.sendMessage(null, "n");
        msgr.sendMessage(null, "m");
    }
}
