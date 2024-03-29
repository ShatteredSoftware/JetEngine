package me.uberpilot.jetengine;

import me.uberpilot.jetengine.command.Command;
import me.uberpilot.jetengine.command.JCommandExecutor;
import me.uberpilot.jetengine.language.Message;
import me.uberpilot.jetengine.language.MessageSet;
import me.uberpilot.jetengine.messenger.Messenger;
import me.uberpilot.jetengine.util.JUtilities;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

/**
 * JPlugin. The core of the JetEngine plugin layer. Linked to message sets and includes utilities to translate messages.
 * <br><br>
 *
 * Plugins extending this class should take the following form:<br><br>
 *
 * <pre>
 * public class MyPlugin extends JPlugin
 * {
 *     public MyPlugin()
 *     {
 *         super("MyPlugin");
 *     }
 * }
 * </pre>
 * <br>
 * Passing nulls to this class will result in an IllegalArgumentException unless otherwise stated.<br><br>
 *
 * JPlugin provides multiple methods to be overridden in order to do things in the proper order. Do not override
 * {@link #onEnable()} or {@link #onDisable()}, but instead use the provided hooks.<br><br>
 *
 * {@link #preEnable()} is called before JPlugin handles registration and reading so that user-defined settings can be
 * loaded and overridden.<br><br>
 *
 * {@link #postDisable()} is called after JPlugin handles deregistration and cleanup.<br><br>
 *
 * {@link #periodicSave()} is called every {@link #periodicSavePeriod user-defined period} if {@link #doesPeriodicSave} is
 * enabled. <br><br>
 * @see me.uberpilot.jetengine.command.Command
 * @see me.uberpilot.jetengine.messenger.Messenger
 * @see me.uberpilot.jetengine.language.MessageSet
 * @author UberPilot
 * @since 1.0
 */
@SuppressWarnings("WeakerAccess UnusedDeclaration")
public abstract class JPlugin extends JavaPlugin
{
    /**
     * MessageSet linked to the plugin. Used to store and translate messages.
     */
    protected MessageSet messages;

    /**
     * Messenger linked to the plugin. Used to send messages.
     */
    protected Messenger messenger;

    /**
     * Our own command map.
     */
    protected HashMap<String, Command> commands;

    /**
     * Bukkit's command map.
     */
    private CommandMap commandMap;
    /**
     * <b>User feature toggle:</b> Debug logging. <br>
     * <i>If debug mesages should be logged.</i>
     * */
    protected boolean debug = false;
    /**
     * <b>User feature toggle:</b> Reload command. <br>
     * <i>If the reload command should be enabled.</i>
     * */
    protected boolean reload = false;
    /**
     * <b>User feature toggle:</b> Periodic saving. <br>
     * <i>If periodic saving should be run at all.</i>
     * */
    protected boolean doesPeriodicSave = false;
    /**
     * <b>User feature setting:</b> Periodic saving period. <br>
     * <i>How often the save task should be run.</i>
     * */
    protected long periodicSavePeriod = (long) 10 * 60 * 20;

    private String authors;
    private final String name;

    /**
     * Base command, following the format "<code>/{@link JPlugin#name name}</code>".
     */
    protected Command baseCommand;

    /**
     * Help command, following the format "<code>/{@link JPlugin#name name} help</code>".
     */
    protected Command helpCommand;

    /**
     * Reload command, following the format "<code>/{@link JPlugin#name name} reload</code>".<br>
     * Only enabled if {@link JPlugin#reload reload} is enabled, otherwise <code>null</code>.
     * @see JPlugin#reload
     */
    protected Command reloadCommand;

    /**
     * Constructor. Kicks off initialization, should be called with super() by any extending class.
     * @param name The name of the plugin.
     */
    protected JPlugin(String name)
    {
        super();
        if(JUtilities.isEmptyOrNull(name)) throw new IllegalArgumentException("Plugin name cannot be null or empty.");
        this.messages = new MessageSet();
        this.messenger = new Messenger(this, messages);
        this.commands = new HashMap<>();
        this.name = name;
    }

    /**
     * User-defined enable tasks.<br>
     * Run before registration.
     */
    protected void preEnable() {}

    /**
     * Creates default messages and commands.
     * @see JPlugin#onEnable()
     */
    private void init()
    {
        initMessages();
        initCommands();
    }

    private void initCommands()
    {
        //Create and register the Info command.
        baseCommand = new Command(this, null, name.toLowerCase(), (sender, label, args) ->
        {
            messenger.sendMessage(sender, "core.info", name, this.getDescription().getVersion(), this.authors,
                    this.getDescription().getWebsite());
            return true;
        });

        helpCommand = new Command(this, baseCommand, "help", (sender, label, args) ->
        {
            messenger.sendMessage(sender, "core.help_header", name);
            for(Command command : commands.values())
            {
                messenger.sendMessage(sender, "core.help_line", command.getLabel(), command.getDescription());
            }
            return true;
        });

        if(this.reload)
        {
            reloadCommand = new Command(this, baseCommand, "reload", ((sender, label, args) ->
            {
                this.onDisable();
                this.onEnable();
                return true;
            }));
        }
        commands.put(name.toLowerCase(), baseCommand);
    }

    private void initMessages()
    {
        //Create messages and defaults.
        this.messages.addMessage(new Message("core.prefix", "$sc[$pc" + name + "$sc]"));
        this.messages.addMessage(new Message("core.no_permission",
                "$pre &cYou don't have permission to use %s."));
        this.messages.addMessage(new Message("core.invalid_args", "$pre &cInvalid argument."));
        this.messages.addMessage(new Message("core.help_line", "  $pc/%s $tc- $sc%s"));
        this.messages.addMessage(new Message("core.help_header", "$scHelp for $pc%s$sc:"));
        this.messages.addMessage(new Message("core.cmd_help_header", "$sc%s Help$tc - $pc/%s"));
        this.messages.addMessage(new Message("core.info",
                "$tc&m--------------------------------------\n" +
                        "$pc%s-%s $scby $pc%s\n" +
                        "$tc&m--------------------------------------\n" +
                        "$pcWebsite$tc: $sc%s\n" +
                        "$tcUse $sc/" + name.toLowerCase() + " help $tcfor a list of commands."));
        this.messages.addMessage(new Message("core.list_separator", ","));
        this.messages.addMessage(new Message("core.list_and", "and"));
        this.messages.addMessage(new Message(name.toLowerCase() + "_cmd." + name.toLowerCase() + ".description",
                "Gives basic information about " + name + "."));
        this.messages.addMessage(new Message(name.toLowerCase() + "_cmd." + name.toLowerCase() + ".help.description",
                "Gives a list of commands from " + name + "."));
    }

    /**
     * Bukkit overridden onEnable method.<br>
     * <b>Note: Uses Reflection</b>
     * @see JavaPlugin#onEnable()
     * @see JPlugin#preEnable()
     */
    @Override
    public void onEnable()
    {
        //Add commands, read messages, etc.
        init();

        preEnable();

        //Load Messages from files, in cascading order.
        loadMessages();
        //Register commands. Uses reflection.
        registerCommands();

        //Prettify the Authors String for the Info Command, but only do it once.
        this.authors = JUtilities.punctuateList(this.getDescription().getAuthors(),
                messages.getMessage("core.list_separator"), messages.getMessage("core.list_and"));

        //Schedule saving.
        if (this.doesPeriodicSave)
        {
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::periodicSave, periodicSavePeriod,
                    periodicSavePeriod);
        }
    }

    /**
     * Registers commands to the bukkit command map.
     * <b>Note: Uses Reflection</b>
     */
    private void registerCommands()
    {
        //Reflection shenanigans to register commands.
        try
        {
            final Field cmdMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

            boolean isAccessible = cmdMap.isAccessible();
            cmdMap.setAccessible(true);
            commandMap = (CommandMap) cmdMap.get(Bukkit.getServer());

            for (Command command : commands.values())
            {
                if (debug)
                {
                    //Build child commands.
                    StringBuilder children = new StringBuilder(" {");
                    for (Command child : command.getChildren().values())
                        children.append(child.getLabel()).append(", ");
                    children.delete(children.length() - 2, children.length()).append("}");
                    //Show this and children.
                    getLogger().info(() -> "Registering command /" + command.getLabel() + children.toString());
                }
                commandMap.register(command.getLabel(), command);
            }
            cmdMap.setAccessible(isAccessible);
        }
        catch (NoSuchFieldException | IllegalAccessException e)
        {
            getLogger().severe(e.getMessage());
        }
    }

    /**
     * Cascading-message reading to load strings.
     */
    private void loadMessages()
    {
        //Load internal messages.
        YamlConfiguration internal;
        internal = YamlConfiguration.loadConfiguration(
                new InputStreamReader(JPlugin.class.getResourceAsStream("/messages.yml")));
        for (Message m : messages)
        {
            if (internal.contains(m.getId())) m.set(internal.getString(m.getId(), m.get()));
        }

        //Load external messages.
        YamlConfiguration cfg;
        File ext = new File(getDataFolder(), "messages.yml");
        if (ext.exists())
        {
            cfg = YamlConfiguration.loadConfiguration(ext);
            for (Message m : messages)
            {
                if (cfg.contains(m.getId())) m.set(cfg.getString(m.getId(), m.get()));
            }
        }
    }

    /**
     * Register a command.<br>
     * <b>Note: Uses Reflection</b>
     * @param command The command to be registered.
     * @see Command#Command(JPlugin, Command, String, JCommandExecutor, List)
     * @throws IllegalArgumentException Null commands are not allowed.
     */
    protected void registerCommand(Command command)
    {
        if(command == null)
        {
            throw new IllegalArgumentException("Command cannot be null.");
        }

        try
        {
            final Field cmdMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

            boolean isAccessible = cmdMap.isAccessible();
            cmdMap.setAccessible(true);
            commandMap = (CommandMap) cmdMap.get(Bukkit.getServer());

            commandMap.register(command.getLabel(), command);
            cmdMap.setAccessible(isAccessible);
        }
        catch (NoSuchFieldException | IllegalAccessException e)
        {
            getLogger().severe(e.getMessage());
        }
    }

    /**
     * User-defined save task. Used for saving data to mitigate crashes.<br>
     * Run after a user-defined period, if enabled.
     */
    protected void periodicSave() {}

    /**
     * User-defined disable tasks.<br>
     * Run after JPlugin finishes its disable tasks.
     */
    protected void postDisable() {}

    /**
     * Bukkit overridden onEnable method.
     * @see JavaPlugin#onDisable()
     * @see JPlugin#postDisable()
     */
    @Override
    public void onDisable()
    {
        //Clear commands and messages.
        commands.clear();
        messages.clear();

        //Cancel tasks we've made.
        Bukkit.getScheduler().cancelTasks(this);

        //Call user-defined disable.
        postDisable();
    }

    /**
     * @return The MessageSet linked to this plugin.
     */
    public MessageSet getMessages()
    {
        return messages;
    }

    /**
     * @return The Messenger linked to this plugin.
     */
    public Messenger getMessenger()
    {
        return messenger;
    }
}
