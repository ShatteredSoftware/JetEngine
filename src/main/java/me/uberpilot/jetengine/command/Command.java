package me.uberpilot.jetengine.command;

import me.uberpilot.jetengine.JPlugin;
import me.uberpilot.jetengine.language.Message;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Represents a command and information attached to it.
 */
//TODO: Make this not extend BukkitCommand, but make it create a BukkitCommand as needed.
public class Command extends BukkitCommand implements CommandExecutor
{
    /** Link to the plugin this belongs to. */
    private JPlugin plugin;

    /** Parent command. */
    private Command parent;

    private BukkitCommand bukkit;

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
    private List<String> aliases;

    public Command(JPlugin plugin, Command parent, String label, JCommandExecutor executor)
    {
        super(label);

        //Check for nulls for plugin and for label.
        if(plugin == null) throw new IllegalArgumentException("Commands: Parent plugin cannot be null.");
        if(label == null) throw new IllegalArgumentException("Commands: Label cannot be null.");

        //Basic value-setting.
        this.plugin = plugin;
        this.parent = parent;
        this.label = label;
        this.executor = executor;
        this.children = new HashMap<>();

        this.permission = plugin.getName().toLowerCase() + '.' + (parent != null ? (parent.getPermission() + '.') : "") + label.toLowerCase();
        String messagePath = plugin.getName().toLowerCase() + "_cmd." + (parent != null ? parent.getPath('_') : "") + label.toLowerCase();

        //Default handling for description.
        if (!plugin.getMessages().hasMessage(messagePath + ".desc"))
        {
            this.plugin.getMessages().addMessage(new Message(messagePath + ".desc", plugin.getName() + " /" + getLabel() + " command."));
        }
        this.description = plugin.getMessages().getMessage(messagePath + ".desc");

        //Default handling for feature name.
        if (!plugin.getMessages().hasMessage(messagePath + ".feature_name"))
        {
            this.plugin.getMessages().addMessage(new Message(messagePath + ".feature_name", '/' + (parent != null ? parent.getPath(' ') : "") + this.getLabel()));
        }
        this.feature_name = plugin.getMessages().getMessage(messagePath + ".feature_name");

        //Hook this to the parent.
        if(parent != null)
            this.getParent().addChild(this);
    }

    private String getPath(String separator)
    {
        return (this.parent != null ? this.parent.getPath(separator) + separator : "") + this.getLabel();
    }

    public String getPath(char separator)
    {
        return (this.parent != null ? this.parent.getPath(separator) + separator : "") + this.getLabel();
    }



    public void sendNoPermissionMessage(CommandSender sender)
    {
        plugin.getMessenger().sendErrorMessage(sender, "core.no_permission", this.feature_name);
    }

    public void sendCommandHelp(CommandSender sender)
    {
        plugin.getMessenger().sendCommandHelp(sender, this);
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
    {
        if(args.length > 0)
        {
            if(children.containsKey(args[0]))
            {
                //Pass handling down to children.
                return children.get(args[0]).execute(sender, label, Arrays.copyOfRange(args, 1, args.length));
            }
            else
            {
                plugin.getMessenger().sendErrorMessage(sender, "core.invalid_args");
                sendCommandHelp(sender);
                return true;
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

    public void addChild(Command command)
    {
        this.children.put(command.getLabel(), command);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException
    {
        //Don't parse empty args.
        if(args.length < 1)
            return null;

        //Pass down to children that the sender has access to.
        if(children.containsKey(args[0].toLowerCase()) && sender.hasPermission(children.get(args[0].toLowerCase()).getPermission()))
            return children.get(args[0].toLowerCase()).tabComplete(sender, alias, Arrays.copyOfRange(args, 1, args.length));

        //Handle this here.
        ArrayList<String> completions = new ArrayList<>();
        for(Command child : children.values())
        {
            if(sender.hasPermission(child.getPermission()))
            {
                completions.add(child.getLabel());
            }
        }
        return completions;
    }

    public void addChildren(Command... commands)
    {
        for(Command command : commands)
        {
            addChild(command);
        }
    }
}
