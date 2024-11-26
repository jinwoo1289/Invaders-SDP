package entity;

import java.awt.Color;


import engine.Core;
import engine.DrawManager.SpriteType;
import engine.GameState;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EnemyShipTest {

    private EnemyShip enemyShip;
    private GameState gameState;

    @BeforeEach
    void setUp() {
        // GameState 객체 생성
        gameState = new GameState(1, 0, Ship.ShipType.CosmicCruiser, 1, 0, 1, 1, "Player", 0, 0, 0, 0, 0);

        // EnemyShip 생성
        enemyShip = new EnemyShip(10, 20, SpriteType.EnemyShipA1, gameState);
    }

    @Test
    void testCalculatePointValue() {
        // 레벨에 따른 점수 계산 확인
        int expectedPoints = 10 + (int) (3 * 0.1) + Core.getLevelSetting();
        assertEquals(expectedPoints, enemyShip.getPointValue());
    }

    @Test
    void testMove() {
        // 초기 위치 확인
        assertEquals(10, enemyShip.getPositionX());
        assertEquals(20, enemyShip.getPositionY());

        // 이동 후 위치 확인
        enemyShip.move(10, -10);
        assertEquals(20, enemyShip.getPositionX());
        assertEquals(10, enemyShip.getPositionY());
    }

    @Test
    void testDestroy() {
        // 파괴 메서드 테스트
        enemyShip.destroy(0.5f);
        assertTrue(enemyShip.isDestroyed());
        assertEquals(SpriteType.Explosion, enemyShip.getSpriteType());
    }

    @Test
    void testHealthManageDestroy() {
        // 최초 목숨 설정 확인
        assertEquals(1, enemyShip.getHealth());

        // 목숨 감소
        enemyShip.HealthManageDestroy(0);
        assertEquals(0, enemyShip.getHealth());
        assertFalse(enemyShip.isDestroyed());

        // 목숨이 0일 때 파괴 확인
        enemyShip.HealthManageDestroy(0);
        assertTrue(enemyShip.isDestroyed());
        assertEquals(SpriteType.Explosion, enemyShip.getSpriteType());
    }

    @Test
    void testGetAndSetHealth() {
        // 목숨 값 설정 및 확인
        enemyShip.setHealth(2);
        assertEquals(2, enemyShip.getHealth());

        // 목숨을 음수로 설정 시 최소값 유지
        enemyShip.setHealth(-1);
        assertEquals(-1, enemyShip.getHealth());
    }

    @Test
    void testDefaultColor() {
        // 기본 색상 확인
        assertEquals(Color.RED, EnemyShip.getDefaultColor(SpriteType.EnemyShipA1));
        assertEquals(Color.GREEN, EnemyShip.getDefaultColor(SpriteType.EnemyShipB1));
        assertEquals(Color.BLUE, EnemyShip.getDefaultColor(SpriteType.EnemyShipC1));
        assertEquals(Color.YELLOW, EnemyShip.getDefaultColor(SpriteType.EnemyShipD1));
        assertEquals(Color.ORANGE, EnemyShip.getDefaultColor(SpriteType.EnemyShipE1));
        assertEquals(Color.WHITE, EnemyShip.getDefaultColor(SpriteType.EnemyShipSpecial));
    }
}