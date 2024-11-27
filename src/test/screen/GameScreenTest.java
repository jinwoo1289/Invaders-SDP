package screen;
import engine.DrawManager;
import engine.GameSettings;
import engine.GameState;
import entity.*;
import screen.GameScreen;
import org.junit.jupiter.api.*;


import java.util.HashSet;
import java.util.Set;

import static engine.Core.BASE_SHIP;
import static engine.Core.MAX_LIVES;
import static org.junit.jupiter.api.Assertions.*;



class GameScreenTest {
    private static GameScreen screen;
    private static GameState state;
    private static EnemyShipFormation formation;

    @BeforeAll
    static void Setting() {
        state = new GameState(1, 0, BASE_SHIP, MAX_LIVES, 0, 0, 0, "", 0, 0, 0, 0, 0);
        GameSettings settings = new GameSettings(4, 4, 60, 2500);
        Wallet wallet = new Wallet();
        formation = new EnemyShipFormation(settings, state);
        screen = new GameScreen(state, settings, false, 600, 650, 60, wallet);


        screen.initialize();
    }

    @Test
    void testInitialization() {
        assertEquals(screen.getWidth(), 600);
        assertEquals(screen.getHeight(), 650);
        assertNotNull(screen.ship);

    }

    @Test
    void testUpdate() {
        float initialEnemyX = formation.getEnemyShip().getPositionX();
        screen.updateEnemiesAndBullets();

        float updatedEnemyX = formation.getEnemyShip().getPositionX();
        assertEquals(initialEnemyX, updatedEnemyX);

        while (!screen.enemyShipSpecialCooldown.checkFinished()) {}

        screen.handleEnemyShipSpecial();
        assertNotNull(screen.enemyShipSpecial);

    }

    @Test
    void testCollision() {
        int initialLives = state.getLivesRemaining();
        Set<Bullet> recyclable = new HashSet<>();
        Bullet bullet = new Bullet(screen.getWidth() / 2, screen.getHeight() - 70,5);
        screen.shipCollision(recyclable, bullet);
        EnemyShip enemyShip = new EnemyShip();

        int updatedLives = state.getLivesRemaining();
        assertEquals(initialLives, updatedLives);
    }

    @Test
    void testCountDown() {
        while (!screen.inputDelay.checkFinished())
            screen.handleCountDown();
        assertEquals(state.getMaxCombo(), 0);

    }

    @Test
    void testCheckLevelCompletion() {
        formation.shipCount = 0;
        screen.checkLevelCompletion();
        assertTrue(screen.levelFinished);

    }
}