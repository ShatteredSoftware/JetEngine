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

    private final String authors;

    public JPlugin()
    {
        super();
        this.messages = new MessageSet(this);
        this.messenger = new Messenger(this, messages);
        this.commands = new HashMap<>();

        //Prettify the Authors String for the Info Command, but only do it once.

        this.authors = JUtilities.punctuateList(this.getDescription().getAuthors());

        this.messages.addMessage(new Message("prefix", "$sc[$pc" + getName().toLowerCase() + "$sc]"));
        this.messages.addMessage(new Message("no_permission", "&cYou don't have permission to use %s"));
        this.messages.addMessage(new Message("help_line", "$pc%s $sc- $pc%s"));
        this.messages.addMessage(new Message("help_header", "$scHelp for $pc%s$sc:"));
        this.messages.addMessage(new Message("info", "$pc%s $scby $pc%s\n$scVersion: %s\n$scWebsite: %s"));
        this.messages.addMessage(new Message(getName() + "_cmd." + this.getName().toLowerCase() + ".desc", "Gives information about the plugin."));
        this.messages.addMessage(new Message(getName() + "_cmd." + this.getName().toLowerCase() + ".feature_name", "/" + getName().toLowerCase()));

        //Create and register the Info command.
        Command info = new Command
                (
                        this, null, this.getName().toLowerCase(),
                        (sender, label, args) ->
                        {
                            messenger.sendMessage
                                    (
                                            sender, "info",
                                            this.getName(), this.authors,
                                            this.getDescription().getVersion(),
                                            this.getDescription().getWebsite()
                                    );
                            return true;
                        }
                );
        commands.put(this.getName().toLowerCase(), info);
    }

    protected void preEnable() {}

    @Override
    public void onEnable()
    {
        preEnable();
        //Reflection shenanigans to register commands.
        try
        {
            final Field cmdMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

            cmdMap.setAccessible(true);
            CommandMap map = (CommandMap) cmdMap.get(Bukkit.getServer());

            for(HashMap.Entry<String, Command> entry : commands.entrySet())
            {
                map.register(entry.getKey(), entry.getValue());
            }
        }
        catch (NoSuchFieldException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
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
