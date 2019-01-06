package me.uberpilot.jetengine.command;

import me.uberpilot.jetengine.JPlugin;
import me.uberpilot.jetengine.language.Message;
import me.uberpilot.jetengine.util.JUtilities;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a command and information attached to it.
 */
@SuppressWarnings("WeakerAccess UnusedDeclaration")
public class Command extends BukkitCommand implements CommandExecutor
{
    /** Link to the plugin this belongs to. */
    private final JPlugin plugin;
    /** Child commands. */
    private final HashMap<String, Command> children;
    /** Name of the command. */
    private final String label;
    /** Parent command. */
    private Command parent;
    /** Permission to be checked before viewing or executing the command */
    private String permission;

    private String messagePath;

    private String descriptionPath;

    private String featurePath;

    private String featureName;

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
        if(plugin == null)
            throw new IllegalArgumentException("Commands: Parent plugin cannot be null.");
        if(JUtilities.isEmptyOrNull(label))
            throw new IllegalArgumentException("Commands: Label cannot be empty or null.");


        //Basic value-setting.
        this.plugin = plugin;
        this.parent = parent;
        this.label = label;
        this.executor = executor;
        this.children = new HashMap<>();

        this.setAliases(aliases);

        //Hook this to the parent.
        if(parent != null)
        {
            this.parent.addChild(this);
            this.parent.createHelpCommand();
            aliases.forEach(e -> parent.addChild(e, this));
        }

        init();
    }

    private void init()
    {
        this.permission = defaultPermission();
        this.messagePath = defaultMessagePath();
        this.descriptionPath = messagePath + ".description";
        this.featurePath = messagePath + ".feature_name";
        this.featureName = defaultFeatureName();
        this.description = defaultDescription();
    }

    /**
     * Initializes the path to messages for this command recursively.<br>
     *
     * <b>Example:</b> For a plugin named Example, with a command labeled parent, and a child command labeled <br>
     * child, the message path for the child would be <code>"example_cmd.parent.child"</code>.
     *
     * @return The path to messages for this command.
     */
    private String defaultMessagePath()
    {
        if (this.parent != null)
        {
            return this.parent.messagePath + "." + this.label.toLowerCase();
        }
        else
        {
            return this.plugin.getName().toLowerCase() + "_cmd." + this.label.toLowerCase();
        }
    }

    /**
     * Default permission handling.<br><br>
     *
     * <b>Example: </b> For a plugin named Example, with a command labeled parent, and a child command labeled <br>
     * child, the permission for the child would be <code>"example.parent.child"</code>.
     *
     * @return The default permission.
     */
    private String defaultPermission()
    {
        if (parent != null)
        {
            return parent.permission + '.' + label.toLowerCase();
        }
        else
        {
            return plugin.getName().toLowerCase() + '.' + label.toLowerCase();
        }
    }


    /**
     * Default handling for creating a feature name.<br><br>
     *
     * <b>Example: </b> For a plugin named Example, with a command labeled parent, and a child command labeled <br>
     * child, the default feature name for the child would be <code>"the /parent child command"</code>.
     *
     * @return Either a default feature name or the one loaded from the attached
     * {@link me.uberpilot.jetengine.language.MessageSet MessageSet}.
     */
    private String defaultFeatureName()
    {
        if (!plugin.getMessages().hasMessage(featurePath))
        {
            if (parent != null)
            {
                this.plugin.getMessages().addMessage(new Message(featurePath, "the /" + (parent.getPath(' ') + " ") + this.getLabel() + " command"));
            }
            else
            {
                this.plugin.getMessages().addMessage(new Message(featurePath, "the /"  + this.getLabel() + " command"));
            }
        }
        return plugin.getMessages().getMessage(featurePath);
    }

    /**
     * Default handling for creating a description.<br><br>
     *
     * <b>Example: </b> For a plugin named Example, with a command labeled parent, and a child command labeled <br>
     * child, the default description for the child would be <code>"Example /parent child command."</code>.
     *
     * @return Either a default description or the one loaded from the attached
     * {@link me.uberpilot.jetengine.language.MessageSet MessageSet}.
     */
    private String defaultDescription()
    {
        if (!plugin.getMessages().hasMessage(descriptionPath))
        {
            this.plugin.getMessages().addMessage(new Message(descriptionPath,
                    plugin.getName() + " " + this.featureName.substring(4) + "."));
        }
        return plugin.getMessages().getMessage(descriptionPath);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, org.bukkit.command.Command command,
                             String label, String[] args)
    {
        return execute(commandSender, label, args);
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
    public String getPermission()
    {
        return permission;
    }

    @Override
    public String getLabel()
    {
        return label;
    }

    @Override
    public String getDescription()
    {
        return this.plugin.getMessages().getMessage(descriptionPath);
    }

    @Override
    public org.bukkit.command.Command setDescription(String description)
    {
        this.description = description;
        plugin.getMessages().set(descriptionPath, description);
        return this;
    }

    private void createHelpCommand()
    {
        //Add help automatically if this is a child.
        if (!this.children.containsKey("help"))
        {
            this.plugin.getMessages().addMessage(new Message(messagePath + ".help.description",
                    "Help for " + this.featureName + "."), true);
            this.children.put("help", new Command(this.plugin, this, "help",
                    (sender, unused1, unused2) -> sendCommandHelp(sender), "?"));
        }
    }

    private void sendNoPermissionMessage(CommandSender sender)
    {
        plugin.getMessenger().sendErrorMessage(sender, "core.no_permission", this.featureName);
    }

    private boolean sendCommandHelp(CommandSender sender)
    {
        plugin.getMessenger().sendCommandHelp(sender, this);
        return true;
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
            command.setParent(this);
        }
    }

    public void addAlias(String name)
    {
        if(this.parent != null)
        {
            this.parent.addChild(name, this);
        }
        this.getAliases().add(name);
    }

    public void addChild(String label, Command command)
    {
        this.children.put(label, command);
    }

    public void addAliases(String... aliases)
    {
        Arrays.stream(aliases).forEach(this::addAlias);
    }

    public Command getParent()
    {
        return parent;
    }

    private void setParent(Command command)
    {
        this.parent = command;
        this.init();
        this.parent.createHelpCommand();
        getAliases().forEach(e -> parent.addChild(e, this));
    }

    public Map<String, Command> getChildren()
    {
        return children;
    }

    public String getFeatureName()
    {
        return plugin.getMessages().getMessage(featurePath);
    }

    public String getPath(String separator)
    {
        return (this.parent != null ? this.parent.getPath(separator) + separator : "") + this.getLabel();
    }

    public String getPath(char separator)
    {
        return (this.parent != null ? this.parent.getPath(separator) + separator : "") + this.getLabel();
    }
}
