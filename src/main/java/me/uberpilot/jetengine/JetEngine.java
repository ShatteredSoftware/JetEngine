package me.uberpilot.jetengine;

import me.uberpilot.jetengine.command.Command;
import me.uberpilot.jetengine.language.Message;

public class JetEngine extends JPlugin
{
    public JetEngine()
    {
        super("JetEngine");
        super.debug = false;
    }

    @Override
    protected void preEnable()
    {
        messages.addMessage(new Message("message_list_item", "&f%s $tc- $sc%s"));

        Command list = new Command(this, baseCommand, "list", ((sender, label, args) ->
        {
            for(Message m : messages)
            {
                messenger.sendMessage(sender, "message_list_item", m.getId(), m.get());
            }
            return true;
        }));
    }
}
