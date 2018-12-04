package me.uberpilot.jetengine.language;

import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Iterator;
import javax.annotation.Nonnull;

/**
 * Represents a set of messages for a plugin.
 *
 * @author UberPilot
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("WeakerAccess UnusedDeclaration")
public class MessageSet implements Iterable<Message>
{

    /** Primary color, for emphasis. */
    private String primaryColor;

    /** Secondary color, for general text. */
    private String secondaryColor;

    /** Tertiary color, for misc text and characters, brackets. */
    private String tertiaryColor;

    /** List of registered messages, indexed by ID. */
    private HashMap<String, Message> messages;

    /**
     * @return A human-readable version of this object. Use {@link #getMessage(String, Object...)} ()} to get a message.
     */
    @Override
    public String toString()
    {
        return "MessageSet{" + "primaryColor='" + primaryColor + '\'' + ", secondaryColor='" + secondaryColor + '\'' + ", tertiaryColor='" + tertiaryColor + '\'' + ", messages=" + messages + '}';
    }

    /**
     * Delegated constructor, using default values for colors.
     *
     * @param messages Messages to be registered by default.
     */
    public MessageSet(Message... messages)
    {
        this(ChatColor.DARK_GREEN.toString(), ChatColor.GRAY.toString(), ChatColor.DARK_GRAY.toString(), messages);
    }

    /**
     * Constructor.
     *
     * @param primaryColor Primary color, for emphasis.
     * @param secondaryColor Secondary color, for general text.
     * @param tertiaryColor Tertiary color, for misc text.
     * @param messages Messages to be registered by default.
     */
    public MessageSet(String primaryColor, String secondaryColor, String tertiaryColor, Message... messages)
    {
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.tertiaryColor = tertiaryColor;
        this.messages = new HashMap<>();
        for(Message message : messages)
        {
            this.messages.put(message.getId(), message);
        }
    }

    /**
     * Add a message to the registered set. Does not replace existing messages.
     * @param message The message to be registered.
     */
    public void addMessage(Message message)
    {
        addMessage(message, false);
    }

    /**
     * Add a message to the registered set.
     * @param message The message to be registered.
     * @param replace Whether this should replace an existing message.
     */
    public void addMessage(Message message, boolean replace)
    {
        if(!messages.containsKey(message.getId()) || replace)
        {
            messages.put(message.getId(), message);
        }
        messages.putIfAbsent(message.getId(), message);
    }

    /**
     * Get a message, replacing placeholders with variables.
     * @param id The ID of the message to be retrieved.
     * @param vars The variables to be injected.
     * @return The message with replaced placeholders.
     */
    public String getMessage(String id, Object... vars)
    {
        Message m = messages.get(id);
        if(m == null) throw new IllegalArgumentException("Invalid message: " + id);
        String message = String.format(m.get(), vars)
            .replaceAll("\\$pc", primaryColor)
            .replaceAll("\\$sc", secondaryColor)
            .replaceAll("\\$tc", tertiaryColor);
        message = ChatColor.translateAlternateColorCodes('&', message);
        if(messages.containsKey("core.prefix") && !id.equals("core.prefix"))
        {
            message = message.replaceAll("\\$pre", getMessage("core.prefix"));
        }
        return message;
    }

    /**
     * Get a message without replacing any of the placeholders.
     * @param id The ID of the message to be retrieved.
     * @return The raw message.
     */
    public String getRawMessage(String id)
    {
        return messages.get(id).get();
    }

    /**
     * Checks whether a message exists in this set.
     * @param id The ID of the message to be checked.
     * @return True if this message exists in this set, false otherwise.
     */
    public boolean hasMessage(String id)
    {
        return messages.containsKey(id);
    }

    /**
     * Sets the value for a message.
     * @param id ID of the message.
     * @param value Value to set the message to.
     */
    public void set(String id, String value)
    {
        messages.get(id).set(value);
    }

    /**
     * @return The {@link #primaryColor Primary Color} for this MessageSet.
     */
    public String getPrimaryColor()
    {
        return primaryColor;
    }

    /**
     * @return The {@link #secondaryColor Secondary Color} for this MessageSet.
     */
    public String getSecondaryColor()
    {
        return secondaryColor;
    }

    /**
     * @return The {@link #tertiaryColor Tertiary Color} for this MessageSet.
     */
    public String getTertiaryColor()
    {
        return tertiaryColor;
    }

    /**
     * @param primaryColor Sets the {@link #primaryColor Primary Color} for this MessageSet.
     */
    public void setPrimaryColor(String primaryColor)
    {
        this.primaryColor = primaryColor;
    }

    /**
     * @param secondaryColor Sets the {@link #secondaryColor Primary Color} for this MessageSet.
     */
    public void setSecondaryColor(String secondaryColor)
    {
        this.secondaryColor = secondaryColor;
    }

    /**
     * @param tertiaryColor Sets the {@link #tertiaryColor Primary Color} for this MessageSet.
     */
    public void setTertiaryColor(String tertiaryColor)
    {
        this.tertiaryColor = tertiaryColor;
    }

    @Override
    @Nonnull public Iterator<Message> iterator()
    {
        return messages.values().iterator();
    }

    /**
     * Clear the MessageSet.
     */
    public void clear()
    {
        this.messages.clear();
    }

    /**
     * Get a message default without replacing any of the placeholders.
     * @param id The ID of the message to be retrieved.
     * @return The default message.
     */
    public String getRawDefault(String id)
    {
        return messages.get(id).getDefault();
    }
}
