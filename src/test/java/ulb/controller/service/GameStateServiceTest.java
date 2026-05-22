package ulb.controller.service;

import org.junit.Test;
import ulb.models.game.RoomType;
import ulb.models.game.TowerNO;

import static org.junit.Assert.*;

/**
 * Tests de régression pour la progression de la tour après les combats.
 */
public class GameStateServiceTest {

    /**
     * Vérifie qu'une victoire classique ne bloque pas la victoire suivante contre un boss.
     */
    @Test
    public void testResetForNewBattleAllowsBossVictoryAfterPreviousVictory() {
        TowerNO tower = new TowerNO(2, 0);
        GameStateService service = new GameStateService(tower);

        assertEquals(RoomType.COMBAT, tower.enterRoom("down_combat"));
        service.finalizeVictoriousBattleOutcome();
        assertEquals(2, tower.getCurrentFloor());

        assertEquals(RoomType.START, tower.enterRoom("start"));
        assertEquals(RoomType.BOSS, tower.enterRoom("boss"));
        service.resetForNewBattle();
        service.finalizeVictoriousBattleOutcome();

        assertEquals(3, tower.getCurrentFloor());
        assertTrue(service.hasFloorBossVictoryToShow());
        assertEquals(2, service.consumeFloorBossVictory());
        assertFalse(service.hasFloorBossVictoryToShow());
    }
}
