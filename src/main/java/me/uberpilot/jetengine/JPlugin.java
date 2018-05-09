package me.uberpilot.jetengine;

import me.uberpilot.jetengine.command.Command;
import me.uberpilot.jetengine.language.Message;
import me.uberpilot.jetengine.language.MessageSet;
import me.uberpilot.jetengine.messenger.Messenger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

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

    protected Command baseCommand;
    protected Command helpCommand;

    protected JPlugin(String name)
    {
        super();
        this.messages = new MessageSet(this);
        this.messenger = new Messenger(this, messages);
        this.commands = new HashMap<>();

        //Prettify the Authors String for the Info Command, but only do it once.

        this.authors = JUtilities.punctuateList(this.getDescription().getAuthors());

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
        commands.put(name.toLowerCase(), baseCommand);
    }

    protected void preEnable() {}

    @Override
    public void onEnable()
    {
        preEnable();
        //Read Messages from the messages.yml internal file
        //FIXME: Broke.
//        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(new InputStreamReader(this.getClass().getResourceAsStream("messages.yml")));
//
//        for(Message m : messages)
//        {
//            if(cfg.contains(m.getId()))
//            {
//                m.set(cfg.getString(m.getId(), m.get()));
//            }
//        }

        //Reflection shenanigans to register commands.
        try
        {
            final Field cmdMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

            cmdMap.setAccessible(true);
            commandMap = (CommandMap) cmdMap.get(Bukkit.getServer());

            for(Command command : commands.values())
            {
                //Build child commands.
                StringBuilder children = new StringBuilder(" {");
                for(Command child : command.getChildren().values())
                    children.append(child.getLabel()).append(", ");
                children.delete(children.length() - 2, children.length()).append("}");
                //Show this and children.
                if(debug)
                    getLogger().info("Registerring command /" + command.getLabel() + children.toString());
                commandMap.register(command.getLabel(), command);
            }
        }
        catch (NoSuchFieldException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
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
