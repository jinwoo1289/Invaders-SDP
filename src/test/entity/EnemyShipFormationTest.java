package entity;

import engine.GameSettings;
import engine.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EnemyShipFormationTest {

    private GameSettings gameSettings;
    private EnemyShipFormation formation;
    private GameState gameState;

    @BeforeEach
    public void setUp() {
        // 초기화 작업: 기본 설정 및 상태로 적 우주선 대형 생성
        gameSettings = new GameSettings(5, 3, -110, 300); // 5개의 열과 3개의 행
        gameState = new GameState(1, 0, Ship.ShipType.StarDefender, 3, 0, 0, 0, "", 0, 0, 0, 0, 0);
        formation = new EnemyShipFormation(gameSettings, gameState);
    }

    @Test
    public void testInitializeEnemyShips() {
        // 초기화된 적 우주선의 개수를 확인
        List<List<EnemyShip>> enemyShips = formation.getEnemyShips();
        assertEquals(5, enemyShips.size(), "열의 개수가 5개여야 합니다."); // 열 5개 확인
        for (List<EnemyShip> column : enemyShips) {
            assertEquals(3, column.size(), "각 열의 행 개수가 3개여야 합니다."); // 각 열에 3개의 적
        }

        // 모든 적 우주선이 올바르게 생성되었는지 확인
        for (List<EnemyShip> column : enemyShips) {
            for (EnemyShip ship : column) {
                assertNotNull(ship, "적 우주선이 null이면 안 됩니다.");
            }
        }
    }


    @Test
    public void testDestroy() {
        // 첫 번째 적 우주선을 가져옴
        EnemyShip destroyedShip = formation.getEnemyShips().get(0).get(0);

        // Null 체크 추가
        assertNotNull(destroyedShip, "파괴될 적 우주선이 null이면 안 됩니다.");

        // 적 우주선 파괴
        formation.destroy(destroyedShip, 0.0f);

        // 적 우주선의 상태가 파괴 상태인지 확인
        assertTrue(destroyedShip.isDestroyed(), "적 우주선이 파괴 상태여야 합니다."); // 상태가 파괴되었는지 확인

        // 남아있는 적 수 감소 확인
        assertEquals(14, formation.shipCount, "남은 적 우주선의 수가 14여야 합니다."); // shipCount 감소 확인
    }


    @Test
    public void testIsEmpty() {
        // shipCount 확인 전, 초기 값이 양수인지 확인
        assertTrue(formation.shipCount > 0, "초기 적 우주선의 개수가 0보다 커야 합니다.");

        // 모든 적 우주선을 파괴하여 shipCount 감소
        for (List<EnemyShip> column : formation.getEnemyShips()) {
            for (EnemyShip ship : column) {
                if (ship != null) {
                    formation.destroy(ship, 0.0f); // destroy 메서드를 호출해 적 우주선 파괴
                }
            }
        }

        // 모든 적이 파괴되었는지 확인
        assertTrue(formation.isEmpty(), "적 우주선 대형이 비어 있어야 합니다.");
    }


    @Test
    public void testGetNextShooter() {
        // 첫 번째 열에서 다음 슈터 확인
        List<EnemyShip> column = formation.getEnemyShips().get(0);
        EnemyShip nextShooter = formation.getNextShooter(column);

        // 남아있는 적 중 마지막 적이 슈터여야 함
        assertEquals(column.get(column.size() - 1), nextShooter, "다음 슈터는 마지막 적이어야 합니다.");

        // 모든 적이 파괴된 경우 null이어야 함
        column.clear(); // 열 비우기
        assertNull(formation.getNextShooter(column), "적이 없으면 null이어야 합니다.");
    }
}
