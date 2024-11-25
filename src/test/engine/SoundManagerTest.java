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
        soundManager.volumeUp();
        int newVolume = soundManager.getVolume();
        assertEquals(initialVolume + 1, newVolume, "Volume should increase by 1.");
    }

    @Test
    void testVolumeDecrease() {
        int initialVolume = soundManager.getVolume();
        soundManager.volumeDown();
        int newVolume = soundManager.getVolume();
        assertEquals(initialVolume - 1, newVolume, "Volume should decrease by 1.");
    }

    @Test
    void testVolumeBoundaries() {
        // Set volume to max and try increasing
        for (int i = soundManager.getVolume(); i < 10; i++) {
            soundManager.volumeUp();
        }
        assertEquals(10, soundManager.getVolume(), "Volume should not exceed 10.");

        soundManager.volumeUp(); // Try exceeding
        assertEquals(10, soundManager.getVolume(), "Volume should remain at 10 after exceeding.");

        // Set volume to min and try decreasing
        for (int i = soundManager.getVolume(); i > 0; i--) {
            soundManager.volumeDown();
        }
        assertEquals(0, soundManager.getVolume(), "Volume should not go below 0.");

        soundManager.volumeDown(); // Try going below 0
        assertEquals(0, soundManager.getVolume(), "Volume should remain at 0 after decreasing.");
    }

    @Test
    void testPlaySound() {
        Sound testSound = Sound.MENU_CLICK;
        soundManager.playSound(testSound);
        assertTrue(soundManager.isSoundPlaying(testSound), "Sound should be playing after being played.");
    }

    @Test
    void testStopSound() {
        Sound testSound = Sound.MENU_CLICK;
        soundManager.playSound(testSound);
        assertTrue(soundManager.isSoundPlaying(testSound), "Sound should be playing after being played.");

        soundManager.stopSound(testSound);
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
        Sound testSound = Sound.BGM_MAIN;
        soundManager.loopSound(testSound);
        soundManager.closeAllSounds();

        assertFalse(soundManager.isSoundPlaying(testSound), "All sounds should stop playing after closing.");
    }

    @Test
    void testSoundNotFound() {
        Sound invalidSound = null;
        assertDoesNotThrow(() -> soundManager.playSound(invalidSound), "Playing an invalid sound should not throw an exception.");
    }

    @Test
    void testBalanceControl() {
        Sound testSound = Sound.ALIEN_LASER;
        soundManager.playSound(testSound, -1.0f); // Play on the left
        assertTrue(soundManager.isSoundPlaying(testSound), "Sound should be playing with positional audio enabled.");

        soundManager.playSound(testSound, 1.0f); // Play on the right
        assertTrue(soundManager.isSoundPlaying(testSound), "Sound should still be playing with positional audio enabled.");
    }
}
