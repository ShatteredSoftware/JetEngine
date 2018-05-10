package me.uberpilot.jetengine;

import me.uberpilot.jetengine.command.Command;
import me.uberpilot.jetengine.language.Message;

public class JetEngine extends JPlugin
{
    public JetEngine()
    {
        super("JetEngine");
        super.debug = true;
    }

    @Override
    protected void preEnable()
    {
        messages.addMessage(new Message("message_item", "&f%s $tc- $sc%s"));

        Command list = new Command(this, helpCommand, "list", ((sender, label, args) ->
        {
            for(Message m : messages)
            {
                messenger.sendMessage(sender, "message_item", m.getId(), m.get());
            }
            return true;
        }));
    }

    @Override
    protected void postDisable()
    {

    }
}
