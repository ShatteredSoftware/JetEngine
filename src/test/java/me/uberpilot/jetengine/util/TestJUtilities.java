package me.uberpilot.jetengine.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class TestJUtilities {

    private String emptyString;
    private String nullString;
    private String fullString;
    private ArrayList<String> nullList;
    private ArrayList<String> emptyStrings;
    private ArrayList<String> strings;
    private ArrayList<String> langStrings;

    @Before
    public void setUp()
    {
        emptyString = "";
        nullString = null;
        fullString = "Hello World!";
        nullList = null;
        emptyStrings = new ArrayList<>();
        strings = new ArrayList<>(Arrays.asList("Thing 1", "Thing 2", "Thing 3"));
        langStrings = new ArrayList<>(Arrays.asList("Cosa 1", "Cosa 2", "Cosa 3"));
    }

    @Test
    public void testConstructor() throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException
    {
        Constructor<?> constructor = JUtilities.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Assert.assertNotNull(constructor.newInstance());
    }

    @Test
    public void testPunctuateListNormal()
    {
        Assert.assertEquals("Expect empty string for null list to punctuate.",  "", JUtilities.punctuateList(nullList));
        Assert.assertEquals("Expect empty string for empty list to punctuate.",  "", JUtilities.punctuateList(emptyStrings));
        Assert.assertEquals("Expect properly punctuated list.", "Thing 1, Thing 2, and Thing 3", JUtilities.punctuateList(strings));
        Assert.assertEquals("Expect properly punctuated list.", "One", JUtilities.punctuateList(Collections.singletonList("One")));
        Assert.assertEquals("Expect properly punctuated list.", "One, and Two", JUtilities.punctuateList(Arrays.asList("One", "Two")));
        Assert.assertEquals("Expect properly punctuated translated list.", "Cosa 1, Cosa 2, y Cosa 3", JUtilities.punctuateList(langStrings, ",", "y"));
    }

    @Test
    public void testIsEmptyOrNull()
    {
        Assert.assertTrue("Expect NULL String to be empty or null", JUtilities.isEmptyOrNull(nullString));
        Assert.assertTrue("Expect Empty String to be empty or null", JUtilities.isEmptyOrNull(emptyString));
        Assert.assertFalse("Expect Full String to not be empty or null", JUtilities.isEmptyOrNull(fullString));
    }
}
