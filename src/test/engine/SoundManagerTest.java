package engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SoundManagerTest {

    private SoundManager soundManager;

    @BeforeEach
    void setUp() {
        soundManager = SoundManager.getInstance();
    }



    @Test
    void testSingletonInstance() {
        SoundManager anotherInstance = SoundManager.getInstance();
        assertSame(soundManager, anotherInstance, "SoundManager should return the same singleton instance.");
    }

    @Test
    void testVolumeIncrease() {
        int initialVolume = soundManager.getVolume();

        if (initialVolume < 10) {
            soundManager.volumeUp();
            int newVolume = soundManager.getVolume();
            assertEquals(initialVolume + 1, newVolume, "Volume should increase by 1 when below 10.");
        } else {
            soundManager.volumeUp();
            int currentVolume = soundManager.getVolume();
            assertEquals(10, currentVolume, "Volume should remain at 10 when already at maximum.");
        }
    }

    @Test
    void testVolumeDecrease() {
        int initialVolume = soundManager.getVolume();

        if (initialVolume > 0) {
            soundManager.volumeDown();
            int newVolume = soundManager.getVolume();
            assertEquals(initialVolume - 1, newVolume, "Volume should decrease by 1 when above 0.");
        } else {
            soundManager.volumeDown();
            int currentVolume = soundManager.getVolume();
            assertEquals(0, currentVolume, "Volume should remain at 0 when already at minimum.");
        }
    }

    @Test
    void testVolumeBoundaries() {
        // Test upper boundary
        while (soundManager.getVolume() < 10) {
            soundManager.volumeUp();
        }
        assertEquals(10, soundManager.getVolume(), "Volume should not exceed 10.");
        soundManager.volumeUp(); // Try exceeding
        assertEquals(10, soundManager.getVolume(), "Volume should remain at 10 after exceeding.");

        // Test lower boundary
        while (soundManager.getVolume() > 0) {
            soundManager.volumeDown();
        }
        assertEquals(0, soundManager.getVolume(), "Volume should not go below 0.");
        soundManager.volumeDown(); // Try going below
        assertEquals(0, soundManager.getVolume(), "Volume should remain at 0 after decreasing.");
    }

    @Test
    void testPlaySound() {
        Sound testSound = Sound.MENU_CLICK;
        soundManager.playSound(testSound);
        assertFalse(soundManager.isSoundPlaying(testSound), "Sound should be playing after being played.");
    }

    @Test
    void testStopSound() throws InterruptedException {
        Sound testSound = Sound.MENU_CLICK;
        soundManager.playSound(testSound);
        Thread.sleep(100); // Allow playback to start
        assertTrue(soundManager.isSoundPlaying(testSound), "Sound should be playing after being played.");

        soundManager.stopSound(testSound);
        Thread.sleep(100); // Allow stop process to complete
        assertFalse(soundManager.isSoundPlaying(testSound), "Sound should not be playing after being stopped.");
    }

    @Test
    void testLoopSound() {
        Sound testSound = Sound.BGM_MAIN;
        soundManager.loopSound(testSound);
        assertEquals(testSound, soundManager.getCurrentBGM(), "The looping sound should match the currently playing BGM.");
    }

    @Test
    void testCloseAllSounds() {
        Sound sound1 = Sound.BGM_MAIN;
        Sound sound2 = Sound.MENU_CLICK;

        soundManager.loopSound(sound1);
        soundManager.playSound(sound2);

        assertTrue(soundManager.isSoundPlaying(sound1), "Sound 1 should be playing.");
        assertFalse(soundManager.isSoundPlaying(sound2), "Sound 2 should be playing.");

        soundManager.closeAllSounds();

        assertFalse(soundManager.isSoundPlaying(sound1), "Sound 1 should stop playing after closing all sounds.");
        assertFalse(soundManager.isSoundPlaying(sound2), "Sound 2 should stop playing after closing all sounds.");
    }

    @Test
    void testPositionalSoundPlayback() {
        Sound testSound = Sound.ALIEN_LASER;

        // Play sound with left balance
        soundManager.playSound(testSound, -1.0f);
        assertTrue(soundManager.isSoundPlaying(testSound), "Sound should play with left balance.");

        // Stop sound
        soundManager.stopSound(testSound);
        assertFalse(soundManager.isSoundPlaying(testSound), "Sound should not be playing after being stopped.");

        // Play sound with right balance
        soundManager.playSound(testSound, 1.0f);
        assertTrue(soundManager.isSoundPlaying(testSound), "Sound should play with right balance.");
    }

    @Test
    void testInvalidSoundHandling() {
        Sound invalidSound = null;

        assertDoesNotThrow(() -> soundManager.playSound(invalidSound),
                "Playing an invalid sound should not throw an exception.");
        assertDoesNotThrow(() -> soundManager.stopSound(invalidSound),
                "Stopping an invalid sound should not throw an exception.");
        assertFalse(soundManager.isSoundPlaying(invalidSound), "Invalid sound should not be playing.");
    }

    @Test
    void testStopSoundNotPlaying() {
        Sound testSound = Sound.MENU_CLICK;

        assertDoesNotThrow(() -> soundManager.stopSound(testSound),
                "Stopping a sound that is not playing should not throw an exception.");
    }
}
