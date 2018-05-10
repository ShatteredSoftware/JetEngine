package me.uberpilot.jetengine;

import me.uberpilot.jetengine.command.Command;
import me.uberpilot.jetengine.language.Message;
import me.uberpilot.jetengine.language.MessageSet;
import me.uberpilot.jetengine.messenger.Messenger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.HashMap;

public abstract class JPlugin extends JavaPlugin
{
    protected MessageSet messages;
    protected Messenger messenger;
    protected HashMap<String, Command> commands;
    private CommandMap commandMap;
    protected boolean debug = false;

    private final String authors;
    private final String name;

    protected Command baseCommand;
    protected Command helpCommand;
    protected Command reloadCommand;

    protected JPlugin(String name)
    {
        super();
        this.messages = new MessageSet(this);
        this.messenger = new Messenger(this, messages);
        this.commands = new HashMap<>();

        //Prettify the Authors String for the Info Command, but only do it once.

        this.authors = JUtilities.punctuateList(this.getDescription().getAuthors());

        this.name = name;
    }

    protected void preEnable() {}

    private void init(String name)
    {
        //Create messages and defaults.
        this.messages.addMessage(new Message("core.prefix", "$sc[$pc" + name + "$sc]"));
        this.messages.addMessage(new Message("core.no_permission", "$pre &cYou don't have permission to use %s"));
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
        this.messages.addMessage(new Message(name.toLowerCase() + "_cmd." + name.toLowerCase() + ".desc", "Gives basic information about " + name + "."));
        this.messages.addMessage(new Message(name.toLowerCase() + "_cmd." + name.toLowerCase() + "_help.desc", "Gives a list of commands from " + name + "."));

        //Load messages from files.
        YamlConfiguration cfg;
        File ext = new File(getDataFolder(), "messages.yml");
        if(ext.exists())
            cfg = YamlConfiguration.loadConfiguration(ext);
        else
            cfg = YamlConfiguration.loadConfiguration(new InputStreamReader(JPlugin.class.getResourceAsStream("/messages.yml")));

        for(Message m : messages)
            if(cfg.contains(m.getId()))
                m.set(cfg.getString(m.getId(), m.get()));

        //Create and register the Info command.
        baseCommand = new Command(this, null, name.toLowerCase(), (sender, label, args) ->
        {
            messenger.sendMessage(sender, "core.info", name, this.getDescription().getVersion(), this.authors, this.getDescription().getWebsite());
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

        reloadCommand = new Command(this, baseCommand, "reload", ((sender, label, args) ->
        {
            this.onDisable();
            this.onEnable();
            return true;
        }));
        commands.put(name.toLowerCase(), baseCommand);
    }

    @Override
    public void onEnable()
    {
        //Add commands, read messages, etc.
        init(this.name);

        preEnable();
        //Read Messages from the messages.yml internal file
        //FIXME: Broke.

        //Reflection shenanigans to register commands.
        try
        {
            final Field cmdMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

            cmdMap.setAccessible(true);
            commandMap = (CommandMap) cmdMap.get(Bukkit.getServer());

            for(Command command : commands.values())
            {
                if(debug)
                {
                    //Build child commands.
                    StringBuilder children = new StringBuilder(" {");
                    for (Command child : command.getChildren().values())
                        children.append(child.getLabel()).append(", ");
                    children.delete(children.length() - 2, children.length()).append("}");
                    //Show this and children.
                    getLogger().info("Registering command /" + command.getLabel() + children.toString());
                }
                commandMap.register(command.getLabel(), command);
            }
        }
        catch (NoSuchFieldException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    protected void postDisable() {}

    @Override
    public void onDisable()
    {
        commands.clear();
        messages.clear();
        postDisable();
    }

    protected void registerCommand(Command command)
    {
        commandMap.register(command.getLabel(), command);
    }

    public MessageSet getMessages()
    {
        return messages;
    }

    public Messenger getMessenger()
    {
        return messenger;
    }
}
