package me.uberpilot.jetengine;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

public class TestJUtilities {

    private String emptyString;
    private String nullString;
    private String fullString;
    private ArrayList<String> nullList;
    private ArrayList<String> emptyStrings;
    private ArrayList<String> strings;

    @Before
    public void setUp()
    {
        emptyString = "";
        nullString = null;
        fullString = "Hello World!";
        nullList = null;
        emptyStrings = new ArrayList<>();
        strings = new ArrayList<>(Arrays.asList("Thing 1", "Thing 2", "Thing 3"));
    }

    @Test
    public void testPunctuateListNormal()
    {
        Assert.assertEquals("Expect empty string for null list to punctuate.",  "", JUtilities.punctuateList(nullList));
        Assert.assertEquals("Expect empty string for empty list to punctuate.",  "", JUtilities.punctuateList(emptyStrings));
        Assert.assertEquals("Expect properly punctuated list.", "Thing 1, Thing 2, and Thing 3", JUtilities.punctuateList(strings));
    }

    @Test
    public void testIsEmptyOrNull()
    {
        Assert.assertTrue("Expect NULL String to be empty or null", JUtilities.isEmptyOrNull(nullString));
        Assert.assertTrue("Expect Empty String to be empty or null", JUtilities.isEmptyOrNull(emptyString));
        Assert.assertFalse("Expect Full String to not be empty or null", JUtilities.isEmptyOrNull(fullString));
    }
}
