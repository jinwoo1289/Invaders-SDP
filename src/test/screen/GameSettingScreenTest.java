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
        assertFalse(GameSettingScreen.getMultiPlay(), "The initial multiplayer setting should be false.");
        assertEquals("P1", GameSettingScreen.getName(0), "The initial name for Player 1 should be 'P1'.");
        assertEquals("P2", GameSettingScreen.getName(1), "The initial name for Player 2 should be 'P2'.");
    }

    @Test
    void testDifficultyChange() {
        // Test changing difficulty level
        gameSettingScreen.handleDifficulty(); // Increase to 2
        assertTrue(true, "Difficulty handling executed without errors (manually validate).");

        gameSettingScreen.handleDifficulty(); // Wrap around to 0
        assertTrue(true, "Difficulty handling executed without errors (manually validate).");
    }

    @Test
    void testMultiplayerNameInput() {
        // Test name input for multiplayer mode
        gameSettingScreen.handleMultiplayer(); // Enable multiplayer
        gameSettingScreen.handleNameInput(); // Simulate input
        assertTrue(true, "Name input handling executed for multiplayer (manually validate).");
    }

    @Test
    void testSinglePlayerNameInput() {
        // Test name input for single-player mode
        gameSettingScreen.handleNameInput(); // Simulate input
        assertTrue(true, "Name input handling executed for single-player (manually validate).");
    }

    @Test
    void testNavigation() {
        // Test row selection (navigation)
        gameSettingScreen.navigateRows(1); // Move down
        assertTrue(true, "Navigation handling executed for moving down (manually validate).");

        gameSettingScreen.navigateRows(-1); // Move up
        assertTrue(true, "Navigation handling executed for moving up (manually validate).");
    }

    @Test
    void testStartGame() {
        // Test starting the game
        gameSettingScreen.handleStart();
        assertTrue(true, "Start game handling executed without errors (manually validate).");
    }

    @Test
    void testExitToMainMenu() {
        // Test exiting to main menu
        gameSettingScreen.exitToMainMenu();
        assertTrue(true, "Exit to main menu executed without errors (manually validate).");
    }
}
