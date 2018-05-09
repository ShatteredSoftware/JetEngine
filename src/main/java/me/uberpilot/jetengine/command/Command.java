package me.uberpilot.jetengine.command;

import me.uberpilot.jetengine.JPlugin;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Represents a command and information attached to it.
 */
public class Command extends BukkitCommand implements CommandExecutor
{
    /** Link to the plugin this belongs to. */
    private JPlugin plugin;

    /** Parent command. */
    private Command parent;

    /** Child commands. */
    private HashMap<String, Command> children;

    /** Name of the command. */
    private String label;

    /** Permission to be checked before viewing or executing the command */
    private String permission;

    private String feature_name;

    /** Description of the command and its functionality. Taken from the plugin's
     * {@link me.uberpilot.jetengine.language.MessageSet MessageSet} from {@link JPlugin#messages}
     */
    private String description;

    private JCommandExecutor executor;

    public Command(JPlugin plugin, Command parent, String label, JCommandExecutor executor, Command... children)
    {
        super(label);
        if(plugin == null) throw new IllegalArgumentException("Commands: Parent plugin cannot be null.");
        if(label == null) throw new IllegalArgumentException("Commands: Label cannot be null.");
        this.plugin = plugin;
        this.parent = parent;
        this.label = label;
        this.permission = plugin.getName().toLowerCase() + '.' + (parent != null ? (parent.getPermission() + '.') : "") + label.toLowerCase();
        this.description = plugin.getMessages().getMessage(plugin.getName() + "_cmd." + label + ".desc");
        this.feature_name = plugin.getMessages().getMessage(plugin.getName() + "_cmd." + label + ".feature_name");
        this.executor = executor;
        this.children = new HashMap<>();
        for(Command child : children)
        {
            this.children.put(child.getLabel(), child);
        }
    }

    /* _cmd:
     *   ping:
     *     desc: 'Sends a ''Pong!'' message.'
     *     # You don't have permission to use %feature_name%
     *     # You are not allowed to use %feature_name%
     *     # You are allowed to use %feature_name%
     *     # etc.
     *     feature_name: '/ping'
     *
     */

    public void sendNoPermissionMessage(CommandSender sender)
    {
        plugin.getMessenger().sendErrorMessage(sender, "no_permission", this.feature_name);
    }

    public void sendCommandHelp(CommandSender sender)
    {
        plugin.getMessenger().sendCommandHelp(sender, this);
    }

    public JPlugin getPlugin()
    {
        return plugin;
    }

    public Command getParent()
    {
        return parent;
    }

    public HashMap<String, Command> getChildren()
    {
        return children;
    }

    public String getLabel()
    {
        return label;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args)
    {if(args.length > 0)
    {
        if(children.containsKey(args[0]))
        {
            //Pass handling down to children.
            return children.get(args[0]).executor.execute(sender, label, Arrays.copyOfRange(args, 1, args.length));
        }
        else
        {
            sendCommandHelp(sender);
        }
    }
        if(sender.hasPermission(permission))
            return executor.execute(sender, label, args);

        sendNoPermissionMessage(sender);
        return true;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, org.bukkit.command.Command command, String label, String[] args)
    {
        return execute(commandSender, label, args);
    }

    public String getPermission()
    {
        return permission;
    }

    public String getDescription()
    {
        return description;
    }
}
