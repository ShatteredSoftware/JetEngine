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
@SuppressWarnings("WeakerAccess UnusedDeclaration")
public class Command extends BukkitCommand implements CommandExecutor
{
    private final String messagePath;
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

    public Command(JPlugin plugin, Command parent, String label, JCommandExecutor executor)
    {
        this(plugin, parent, label, executor, new ArrayList<>());
    }

    public Command(JPlugin plugin, Command parent, String label, JCommandExecutor executor, String... aliases)
    {
        this(plugin, parent, label, executor, Arrays.asList(aliases));
    }

    public Command(JPlugin plugin, Command parent, String label, JCommandExecutor executor, List<String> aliases)
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

        this.setAliases(aliases);

        this.permission = (parent != null ? (parent.getPermission() + '.') :
                (plugin.getName().toLowerCase() + '.')) + label.toLowerCase();
        messagePath = this.getMessagePath();


        //Default handling for feature name.
        if (!plugin.getMessages().hasMessage(messagePath + ".feature_name"))
        {
            this.plugin.getMessages().addMessage(new Message(messagePath + ".feature_name",
                    "the /" + (parent != null ? parent.getPath(' ') + " " : "") +
                            this.getLabel() + " command"));
        }
        this.feature_name = plugin.getMessages().getMessage(messagePath + ".feature_name");

        //Default handling for description.
        if (!plugin.getMessages().hasMessage(messagePath + ".description"))
        {
            this.plugin.getMessages().addMessage(new Message(messagePath + ".description",
                    plugin.getName() + " " + this.feature_name.substring(4) + "."));
        }
        this.description = plugin.getMessages().getMessage(messagePath + ".description");

        //Hook this to the parent.
        if(parent != null)
        {
            this.parent.addChild(this);
            this.parent.createHelpCommand();
            aliases.forEach(e -> parent.addChild(e, this));
        }
    }

    private String getMessagePath()
    {
        return (this.parent != null ? this.parent.getMessagePath() + "." :
                this.plugin.getName().toLowerCase() + "_cmd.") + this.label.toLowerCase();
    }

    public void addAlias(String name)
    {
        if(this.parent != null)
        {
            this.parent.addChild(name, this);
        }
        this.getAliases().add(name);
    }

    public void addAliases(String... aliases)
    {
        Arrays.stream(aliases).forEach(this::addAlias);
    }

    private void createHelpCommand()
    {
        //Add help automatically if this is a child.
        if (!this.children.containsKey("help"))
        {
            this.plugin.getMessages().addMessage(new Message(messagePath + ".help.description",
                    "Help for " + this.feature_name + "."), true);
            this.children.put("help", new Command(this.plugin, this, "help",
                    (sender, unused1, unused2) -> sendCommandHelp(sender), "?"));
        }
    }

    public String getPath(String separator)
    {
        return (this.parent != null ? this.parent.getPath(separator) + separator : "") + this.getLabel();
    }

    public String getPath(char separator)
    {
        return (this.parent != null ? this.parent.getPath(separator) + separator : "") + this.getLabel();
    }

    private void sendNoPermissionMessage(CommandSender sender)
    {
        plugin.getMessenger().sendErrorMessage(sender, "core.no_permission", this.feature_name);
    }

    private boolean sendCommandHelp(CommandSender sender)
    {
        plugin.getMessenger().sendCommandHelp(sender, this);
        return true;
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

    public String getFeatureName()
    {
        return feature_name;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args)
    {
        if(label == null)
            throw new IllegalArgumentException("Command " + this.label + " cannot be executed with null label.");

        if(sender == null)
            throw new IllegalArgumentException("Command " + this.label + " cannot be executed with null sender.");

        if(args == null)
            args = new String[0];

        if(sender.hasPermission(permission))
        {
            if (args.length > 0 && children.size() > 0)
            {
                if (children.containsKey(args[0]))
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
            return executor.execute(sender, label, args);
        }

        sendNoPermissionMessage(sender);
        return true;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, org.bukkit.command.Command command,
                             String label, String[] args)
    {
        return execute(commandSender, label, args);
    }

    public String getPermission()
    {
        return permission;
    }

    public String getDescription()
    {
        return this.plugin.getMessages().getMessage(this.messagePath + ".description");
    }

    public void addChild(String label, Command command)
    {
        this.children.put(label, command);
    }

    public void addChild(Command command)
    {
        this.children.put(command.getLabel(), command);
    }

    public void addChildren(Command... commands)
    {
        for(Command command : commands)
        {
            addChild(command);
        }
    }

    @Override
    public org.bukkit.command.Command setDescription(String description)
    {
        this.description = description;
        plugin.getMessages().set(messagePath + ".description", description);
        return this;
    }
}
