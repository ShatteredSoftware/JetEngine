package me.uberpilot.jetengine.language;

import me.uberpilot.jetengine.util.JUtilities;

@SuppressWarnings("WeakerAccess")
public class Message
{
    private String id;
    private String value;
    private String def;

    /**
     * Create a message.
     * @param id ID of the message in a MessageSet.
     * @param def Default message, with placeholders.
     */
    public Message(String id, String def)
    {
        this.id = id;
        this.value = null;
        this.def = def;
    }

    /**
     * Create a message with a current value.
     * @param id ID of the message in a MessageSet.
     * @param def Default message, with placeholders.
     * @param value Value of the message.
     */
    public Message(String id, String value, String def)
    {
        this.id = id;
        this.value = value;
        this.def = def;
    }

    /**
     * @return A human-readable version of this object. Use {@link #get()} to get the value or default.
     */
    @Override
    public String toString()
    {
        return "Message{" + "id='" + id + '\'' + ", value='" + value + '\'' + ", def='" + def + '\'' + '}';
    }

    /**
     * @return The value or the default if no value exists.
     */
    public String get()
    {
        return (!JUtilities.isEmptyOrNull(value)) ? value : def;
    }

    /**
     * @param def The default to supply if no value exists.
     * @return The value or the supplied default if no value exists.
     */
    public String getOrDef(String def)
    {
        return (!JUtilities.isEmptyOrNull(value)) ? value : def;
    }

    public String getId()
    {
        return id;
    }

    public void set(String value)
    {
        this.value = value;
    }

    /**
     * @return The default value of the message.
     */
    public String getDefault()
    {
        return def;
    }
}
