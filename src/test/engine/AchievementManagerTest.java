package engine;

import entity.Achievement;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class AchievementManagerTest {

    @Test
    public void testGetAchievementReward() throws IOException {
        Achievement mockAchievement = mock(Achievement.class);
        when(mockAchievement.getPerfectStage()).thenReturn(1);
        when(mockAchievement.getHighmaxCombo()).thenReturn(1);
        when(mockAchievement.getFlawlessFailure()).thenReturn(false);

        AchievementManager achievementManager = new AchievementManager();
        achievementManager.achievement = mockAchievement;

        assertEquals(0, achievementManager.getAchievementReward());
    }

    @Test
    public void testUpdateTotalPlayTime() throws IOException {
        Achievement mockAchievement = mock(Achievement.class);
        when(mockAchievement.getTotalPlayTime()).thenReturn(500);
        when(mockAchievement.getPerfectStage()).thenReturn(1);
        when(mockAchievement.getHighmaxCombo()).thenReturn(1);
        when(mockAchievement.getFlawlessFailure()).thenReturn(false);

        AchievementManager achievementManager = new AchievementManager();
        achievementManager.achievement = mockAchievement;
        achievementManager.updateTotalPlayTime(200);

        assertEquals(1000, achievementManager.getAchievementReward());
    }

    @Test
    public void testUpdateMaxCombo() throws IOException {
        Achievement mockAchievement = mock(Achievement.class);
        when(mockAchievement.getTotalPlayTime()).thenReturn(500);
        when(mockAchievement.getPerfectStage()).thenReturn(1);
        when(mockAchievement.getHighmaxCombo()).thenReturn(5);
        when(mockAchievement.getFlawlessFailure()).thenReturn(false);

        AchievementManager achievementManager = new AchievementManager();
        achievementManager.achievement = mockAchievement;
        achievementManager.updateMaxCombo(20);

        assertEquals(4000, achievementManager.getAchievementReward());
    }

    @Test
    public void testUpdatePerfect() throws IOException {
        Achievement mockAchievement = mock(Achievement.class);
        when(mockAchievement.getFlawlessFailure()).thenReturn(false);

        AchievementManager achievementManager = new AchievementManager();
        achievementManager.achievement = mockAchievement;
        achievementManager.updatePerfect(3, 3, 2);

        assertEquals(200, achievementManager.getAchievementReward());
    }

    @Test
    public void testUpdateFlawlessFailure() throws IOException {
        Achievement mockAchievement = mock(Achievement.class);
        when(mockAchievement.getFlawlessFailure()).thenReturn(false);

        AchievementManager achievementManager = new AchievementManager();
        achievementManager.achievement = mockAchievement;
        achievementManager.updateFlawlessFailure(0);

        assertEquals(1000, achievementManager.getAchievementReward());
    }
}