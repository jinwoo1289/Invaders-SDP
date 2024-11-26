package engine;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class GameSettingsTest {
    private engine.GameSettings gameSettings;

    @Test
    void testLevelSettings() {
        // Difficulty 0, level 3
        gameSettings = new engine.GameSettings(10, 10, -110, 300);
        engine.GameSettings expected = gameSettings.LevelSettings(10, 10, -110, 300, 3, 0);

        assertEquals(11, expected.getFormationWidth());
        assertEquals(10, expected.getFormationHeight());
        assertEquals(-120, expected.getBaseSpeed());
        assertEquals(200, expected.getShootingFrecuency());

        // Difficulty 0, level 8
        gameSettings = new engine.GameSettings(10, 10, -130, 300);
        expected = gameSettings.LevelSettings(10, 10, -130, 300, 8, 0);

        assertEquals(11, expected.getFormationWidth());
        assertEquals(10, expected.getFormationHeight());
        assertEquals(-140, expected.getBaseSpeed());
        assertEquals(200, expected.getShootingFrecuency());

        // Difficulty 1, level 4
        gameSettings = new engine.GameSettings(10, 9, -140, 500);
        expected = gameSettings.LevelSettings(10, 9, -140, 500, 4, 1);

        assertEquals(10, expected.getFormationWidth());
        assertEquals(9, expected.getFormationHeight());
        assertEquals(-150, expected.getBaseSpeed());
        assertEquals(300, expected.getShootingFrecuency());

        // Difficulty 1, level 7
        gameSettings = new engine.GameSettings(11, 9, -150, 600);
        expected = gameSettings.LevelSettings(11, 9, -150, 600,7, 1);

        assertEquals(11, expected.getFormationWidth());
        assertEquals(9, expected.getFormationHeight());
        assertEquals(-150, expected.getBaseSpeed());
        assertEquals(300, expected.getShootingFrecuency());

        // Difficulty 2, level 2
        gameSettings = new engine.GameSettings(10, 10, -110, 300);
        expected = gameSettings.LevelSettings(10, 10, -110, 300, 2, 2);

        assertEquals(10, expected.getFormationWidth());
        assertEquals(10, expected.getFormationHeight());
        assertEquals(-130, expected.getBaseSpeed());
        assertEquals(100, expected.getShootingFrecuency());

        // Difficulty 2, level 9
        gameSettings = new engine.GameSettings(12, 10, -140, 500);
        expected = gameSettings.LevelSettings(12, 10, -140, 500, 9, 2);

        assertEquals(12, expected.getFormationWidth());
        assertEquals(10, expected.getFormationHeight());
        assertEquals(-150, expected.getBaseSpeed());
        assertEquals(100, expected.getShootingFrecuency());

    }

}