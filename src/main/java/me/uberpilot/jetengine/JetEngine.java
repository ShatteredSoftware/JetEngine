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
        messages.addMessage(new Message("jetengine_cmd.jetengine.list", "Lists the messages loaded in the JetEngine registry."));
        messages.addMessage(new Message("jetengine_cmd.jetengine.message", "Displays a raw message in the JetEngine registry."));
        messages.addMessage(new Message("jetengine.message_list_item", "&f%s $tc- $sc%s"));
        messages.addMessage(new Message("jetengine.message_message_item", "&f%s $tc- %s $tc($scDefault: &r%s$tc)"));
        messages.addMessage(new Message("jetengine.message_not_found", "$pre &cMessage '&f%s&c' Not Found."));

        Command list = new Command(this, baseCommand, "list", ((sender, label, args) ->
        {
            for(Message m : messages)
            {
                messenger.sendMessage(sender, "jetengine.message_list_item", m.getId(), m.get());
            }
            return true;
        }));

        Command message = new Command(this, baseCommand, "message", ((sender, label, args) ->
        {
            String param = String.join(" ", args);
            if(messages.hasMessage(param))
            {
                messenger.sendMessage(sender, "jetengine.message_message_item", param, messages.getRawMessage(param), messages.getRawDefault(param));
            }
            else
            {
                messenger.sendErrorMessage(sender, "jetengine.message_not_found", param);
            }
            return true;
        }));
    }
}
