package me.uberpilot.jetengine.command;

import org.bukkit.command.CommandSender;

public interface JCommandExecutor
{
    boolean execute(CommandSender sender, String label, String[] args);
}
