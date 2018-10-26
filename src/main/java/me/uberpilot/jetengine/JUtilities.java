package me.uberpilot.jetengine;

import java.util.List;

/**
 * A class of general utilities for use in JetEngine plugins. 
 * @author Hunter Henrichsen
 * @since 1.1
 */
@SuppressWarnings("WeakerAccess")
public final class JUtilities
{
    protected JUtilities() {}

    public static String punctuateList(List<? extends String> list)
    {
        return punctuateList(list, ",", "and");
    }

    /**
     * Punctuates a list of strings with commas and 'and.'
     * @param list The list of strings to punctuate.
     * @return A single punctuated string.
     */
    public static String punctuateList(List<? extends String> list, String separator, String and)
    {
        if(list == null)
            return "";

        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < list.size(); i++)
        {
            builder.append(list.get(i));
            if(i != list.size() - 1 && list.size() > 1)
            {
                if(i != list.size() - 2)
                {
                    builder.append(separator).append(" ");
                }
                else
                {
                    builder.append(separator).append(" ").append(and).append(" ");
                }
            }
        }
        return builder.toString();
    }

    /**
     * Utility method to check if a string is empty or null.
     * @param string String to be checked.
     * @return Boolean. Whether the string is empty or null.
     */
    public static boolean isEmptyOrNull(String string)
    {
        return string == null || string.isEmpty();
    }
}
