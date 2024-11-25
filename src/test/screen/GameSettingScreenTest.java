package screen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameSettingScreenTest {
    private GameSettingScreen gameSettingScreen;

    @BeforeEach
    void setUp() {
        // Create an instance of GameSettingScreen
        gameSettingScreen = new GameSettingScreen(800, 600, 60);
    }

    @Test
    void testInitialSettings() {
        // Test initial settings
        assertFalse((boolean) gameSettingScreen.getField("multiplayer"), "The initial multiplayer setting should be false.");
        assertEquals("P1", GameSettingScreen.getName(0), "The initial name for Player 1 should be 'P1'.");
        assertEquals("P2", GameSettingScreen.getName(1), "The initial name for Player 2 should be 'P2'.");
        assertEquals(1, gameSettingScreen.getField("difficulty"), "The initial difficulty level should be 1 (normal).");
        assertEquals(0, gameSettingScreen.getField("row"), "The initial selected row should be 0.");
    }

    @Test
    void testToggleMultiplayer() {
        // Test toggling multiplayer mode
        gameSettingScreen.setMultiplayer(true);
        assertTrue((boolean) gameSettingScreen.getField("multiplayer"), "The multiplayer setting should be true after enabling.");

        gameSettingScreen.setMultiplayer(false);
        assertFalse((boolean) gameSettingScreen.getField("multiplayer"), "The multiplayer setting should be false after disabling.");
    }

    @Test
    void testDifficultyChange() {
        // Test changing difficulty level
        gameSettingScreen.setDifficultyLevel(0);
        assertEquals(0, gameSettingScreen.getField("difficulty"), "The difficulty level should be set to 0 (easy).");

        gameSettingScreen.setDifficultyLevel(2);
        assertEquals(2, gameSettingScreen.getField("difficulty"), "The difficulty level should be set to 2 (hard).");
    }

    @Test
    void testSelectionCooldown() {
        // Test SelectionCooldown
        assertTrue(gameSettingScreen.getSelectionCooldown().checkFinished(), "The initial selectionCooldown should be finished.");
        gameSettingScreen.getSelectionCooldown().reset();
        assertFalse(gameSettingScreen.getSelectionCooldown().checkFinished(), "The selectionCooldown should not be finished after reset.");
    }

    @Test
    void testNameInput() {
        // Test changing player names
        gameSettingScreen.setMultiplayer(false); // Single-player mode
        assertEquals("P1", GameSettingScreen.getName(0), "The initial name for Player 1 should be 'P1'.");
        gameSettingScreen.setName(0, "ABCD");
        assertEquals("ABCD", GameSettingScreen.getName(0), "The name for Player 1 should be updated to 'ABCD'.");

        gameSettingScreen.setMultiplayer(true); // Multiplayer mode
        assertEquals("P2", GameSettingScreen.getName(1), "The initial name for Player 2 should be 'P2'.");
        gameSettingScreen.setName(1, "WXYZ");
        assertEquals("WXYZ", GameSettingScreen.getName(1), "The name for Player 2 should be updated to 'WXYZ'.");
    }

    @Test
    void testNavigation() {
        // Test row selection (navigation)
        assertEquals(0, gameSettingScreen.getField("row"), "The initially selected row should be 0.");

        // Change row (move down)
        gameSettingScreen.setSelectedRow(1);
        assertEquals(1, gameSettingScreen.getField("row"), "The selected row should change to 1.");

        // Change row (move up)
        gameSettingScreen.setSelectedRow(0);
        assertEquals(0, gameSettingScreen.getField("row"), "The selected row should change back to 0.");
    }
}
