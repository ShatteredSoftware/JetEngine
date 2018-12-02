package me.uberpilot.jetengine.messenger;

import me.uberpilot.jetengine.JPlugin;
import me.uberpilot.jetengine.command.Command;
import me.uberpilot.jetengine.language.MessageSet;
import me.uberpilot.jetengine.util.Sound;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class Messenger
{
    private transient JPlugin plugin;
    private MessageSet messages;

    public Messenger(JPlugin plugin, MessageSet messages)
    {
        this.plugin = plugin;
        this.messages = messages;
    }

    public void sendMessage(CommandSender sender, String id, Object... vars)
    {
        if(sender != null)
        {
            String message = messages.getMessage(id, vars);
            if(message != null && message.length() != 0)
            {
                sender.sendMessage(message);
            }
        }
    }

    public void sendErrorMessage(CommandSender sender, String id, Object... vars)
    {
        sendMessage(sender, id, vars);
        if(sender instanceof Player)
        {
            Player player = (Player) sender;
            player.playSound(player.getLocation(), Sound.NOTE_BASS_GUITAR.bukkitSound(), 1, .8f);
        }
    }

    public void sendImportantMessage(CommandSender sender, String id, Object... vars)
    {
        sendMessage(sender, id, vars);
        if (sender instanceof Player) {
            final Player player = (Player) sender;
            player.playSound(player.getLocation(), Sound.ORB_PICKUP.bukkitSound(), 1, .5F);
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
                    player.playSound(player.getLocation(), Sound.ORB_PICKUP.bukkitSound(), 1, .5F), 4L);
        }
    }

    public void sendCommandHelp(CommandSender sender, Command command)
    {
        sendMessage(sender, "core.cmd_help_header", plugin.getName(), command.getPath(' '));
        for(HashMap.Entry<String, Command> entry : command.getChildren().entrySet())
        {
            if(sender.hasPermission(entry.getValue().getPermission()))
            {
                sendMessage(sender, "core.help_line", command.getPath(' ') + " " + entry.getValue().getLabel(), entry.getValue().getDescription());
            }
        }
    }
}
