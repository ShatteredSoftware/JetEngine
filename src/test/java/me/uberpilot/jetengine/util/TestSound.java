package me.uberpilot.jetengine.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class TestSound
{
    //NOTE: Perhaps there is a more elegant way to do this.
    private org.bukkit.Sound sound = Arrays.stream(org.bukkit.Sound.values()).filter(sound1 ->
            sound1.name().contains("NOTE_BASS")).findAny().orElse(null);

    @Before
    public void setUp()
    {

    }

    @Test(expected = IllegalArgumentException.class)
    public void testError()
    {
        Sound.ITEM_TRIDENT_THROW.bukkitSound();
    }

    @Test
    public void testSound()
    {
        Assert.assertEquals("Enum sound equal to current version sound", sound, Sound.NOTE_BASS.bukkitSound());
        Assert.assertEquals("Cached enum sound equal to current version sound", sound, Sound.NOTE_BASS.bukkitSound());
        Assert.assertEquals("ValueOf Test", Sound.NOTE_BASS, Sound.valueOf("NOTE_BASS"));
    }
}
