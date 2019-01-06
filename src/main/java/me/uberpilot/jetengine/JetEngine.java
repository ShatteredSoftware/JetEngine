package me.uberpilot.jetengine;

import me.uberpilot.jetengine.command.Command;
import me.uberpilot.jetengine.language.Message;
import org.bukkit.command.CommandSender;

@SuppressWarnings("UnusedDeclaration WeakerAccess")
public class JetEngine extends JPlugin
{
    protected Command messageList;
    protected Command messageOne;
    protected Command commandList;
    protected Command commandOne;

    public JetEngine()
    {
        super("JetEngine");
    }

    @Override
    protected void preEnable()
    {
        createMessages();
        createMessageCommands();
        createCommandCommands();
    }

    private void createMessages()
    {
        baseCommand.addAlias("je");
        messages.addMessage(new Message("jetengine_cmd.jetengine.message.description",
                "Lists the messages loaded in the JetEngine registry."));
        messages.addMessage(new Message("jetengine_cmd.jetengine.message.one.description",
                "Displays a raw message in the JetEngine registry."));
        messages.addMessage(new Message("jetengine_cmd.jetengine.command.description",
                "Displays a list of commands JetEngine registry."));
        messages.addMessage(new Message("jetengine_cmd.jetengine.command.one.description",
                "Displays a single command in the JetEngine registry."));
        messages.addMessage(new Message("jetengine.message_list_item",
                "&f%s $tc- $sc%s"));
        messages.addMessage(new Message("jetengine.command_info",
                "$pc/%s $tc- [$sc%s$tc]\n    &fDescription: $sc%s"));
        messages.addMessage(new Message("jetengine.message_message_item",
                "&f%s $tc- &r%s $tc($scDefault: &r%s$tc)"));
        messages.addMessage(new Message("jetengine.message_not_found",
                "$pre &cMessage '&f%s&c' Not Found."));
        messages.addMessage(new Message("jetengine.command_not_found",
                "$pre &cCommand '&f/%s&c' Not Found."));
    }

    private void createMessageCommands()
    {
        messageList = new Command(this, baseCommand, "message", ((sender, label, args) ->
        {
            for(Message m : messages)
            {
                messenger.sendMessage(sender, "jetengine.message_list_item", m.getId(), m.get());
            }
            return true;
        }), "msg");

        messageOne = new Command(this, messageList, "one", ((sender, label, args) ->
        {
            String param = String.join(" ", args);
            if(messages.hasMessage(param))
            {
                messenger.sendMessage(sender, "jetengine.message_message_item", param,
                        messages.getRawMessage(param),
                        messages.getRawDefault(param));
            }
            else
            {
                messenger.sendErrorMessage(sender, "jetengine.message_not_found", param);
            }
            return true;
        }));
    }

    private void createCommandCommands()
    {
        commandList = new Command(this, baseCommand, "command", (sender, label, args) -> {
            for(Command c : commands.values())
            {
                sendCommandInfo(sender, c);
            }
            return true;
        }, "cmd");

        commandOne = new Command(this, commandList, "one", ((sender, label, args) -> {
            if(args.length < 1)
            {
                return sendCommandNotFound(sender, args);
            }
            for(Command c : commands.values())
            {
                if (c.getLabel().equalsIgnoreCase(args[0]) || c.getAliases().contains(args[0]))
                {
                    Command current = consumeArgs(sender, c, args);
                    if(current != null)
                        sendCommandInfo(sender, current);
                }
            }
            return sendCommandNotFound(sender, args);
        }));
    }

    private Command consumeArgs(CommandSender sender, Command current, String[] args)
    {
        for (int i = 1; i < args.length; i++)
        {
            if (current.getChildren().containsKey(args[i]))
            {
                if (sender.hasPermission(current.getChildren().get(args[i]).getPermission()))
                {
                    current = current.getChildren().get(args[i]);
                }
                else
                {
                    sendCommandNotFound(sender, args);
                    return null;
                }
            }
            else
            {
                sendCommandNotFound(sender, args);
                return null;
            }
        }
        return current;
    }

    private boolean sendCommandNotFound(CommandSender sender, String[] args)
    {
        messenger.sendErrorMessage(sender, "jetengine.command_not_found",
                String.join(" ", args));
        return true;
    }

    private boolean sendCommandInfo(CommandSender sender, Command c)
    {
        messenger.sendMessage(sender, "jetengine.command_info", c.getLabel(), c.getPermission(),
                c.getDescription());
        return true;
    }
}
