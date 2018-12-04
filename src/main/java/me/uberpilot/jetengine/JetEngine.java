package me.uberpilot.jetengine;

import me.uberpilot.jetengine.command.Command;
import me.uberpilot.jetengine.language.Message;

@SuppressWarnings("UnusedDeclaration")
public class JetEngine extends JPlugin
{
    public JetEngine()
    {
        super("JetEngine");
    }

    @Override
    protected void preEnable()
    {
        baseCommand.addAlias("je");
        messages.addMessage(new Message("jetengine_cmd.jetengine.message.desc", "Lists the messages loaded in the JetEngine registry."));
        messages.addMessage(new Message("jetengine_cmd.jetengine.message.one.desc", "Displays a raw message in the JetEngine registry."));
        messages.addMessage(new Message("jetengine_cmd.jetengine.command.desc", "Displays a list of commands JetEngine registry."));
        messages.addMessage(new Message("jetengine_cmd.jetengine.command.one.desc", "Displays a single command in the JetEngine registry."));
        messages.addMessage(new Message("jetengine.message_list_item", "&f%s $tc- $sc%s"));
        messages.addMessage(new Message("jetengine.command_info", "$pc/%s $tc- [$sc%s$tc]\n    &fDescription: $sc%s"));
        messages.addMessage(new Message("jetengine.message_message_item", "&f%s $tc- &r%s $tc($scDefault: &r%s$tc)"));
        messages.addMessage(new Message("jetengine.message_not_found", "$pre &cMessage '&f%s&c' Not Found."));
        messages.addMessage(new Message("jetengine.command_not_found", "$pre &cCommand '&f/%s&c' Not Found."));

        Command message = new Command(this, baseCommand, "message", ((sender, label, args) ->
        {
            for(Message m : messages)
            {
                messenger.sendMessage(sender, "jetengine.message_list_item", m.getId(), m.get());
            }
            return true;
        }), "msg");

        Command msg_one = new Command(this, message, "one", ((sender, label, args) ->
        {
            String param = String.join(" ", args);
            if(messages.hasMessage(param))
            {
                messenger.sendMessage(sender, "jetengine.message_message_item", param, messages.getRawMessage(param),
                        messages.getRawDefault(param));
            }
            else
            {
                messenger.sendErrorMessage(sender, "jetengine.message_not_found", param);
            }
            return true;
        }));

        Command command = new Command(this, baseCommand, "command", (sender, label, args) -> {
            for(Command c : commands.values())
            {
                messenger.sendMessage(sender, "jetengine.command_info", c.getLabel(), c.getPermission(),
                        c.getDescription());
            }
            return true;
        }, "cmd");

        //TODO: Make me work with base aliases.
        Command cmd_one = new Command(this, command, "one", ((sender, label, args) -> {
            if(args.length == 0)
            {
                messenger.sendErrorMessage(sender, "jetengine.command_not_found", String.join(" ", ""));
            }
            if(commands.containsKey(args[0]))
            {
                Command current = commands.get(args[0]);
                for (int i = 1; i < args.length ; i++)
                {
                    if(current.getChildren().containsKey(args[i]))
                    {
                        if(sender.hasPermission(current.getChildren().get(args[i]).getPermission()))
                        {
                            current = current.getChildren().get(args[i]);
                        }
                        else
                        {
                            messenger.sendErrorMessage(sender, "jetengine.command_not_found", String.join(" ", args));
                        }
                    }
                    else
                    {
                        messenger.sendErrorMessage(sender, "jetengine.command_not_found", String.join(" ", args));
                    }
                }
                messenger.sendMessage(sender, "jetengine.command_info", current.getPath(' '), current.getPermission(),
                        current.getDescription());

            }
            else
            {
                messenger.sendErrorMessage(sender, "jetengine.command_not_found", String.join(" ", args));
            }
            return true;
        }));
    }
}
