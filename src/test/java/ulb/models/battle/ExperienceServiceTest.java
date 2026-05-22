package ulb.models.battle;

import java.lang.reflect.Field;
import java.util.List;
import org.junit.Test;
import ulb.models.data.BugemonDefinition;
import ulb.models.data.Stats;
import ulb.models.data.Type;
import ulb.models.game.BugemonInstance;
import ulb.models.game.LevelUpBonus;
import ulb.models.game.Team;

import static org.junit.Assert.*;

public class ExperienceServiceTest {

    @Test
    public void testCalculateVictoryXpForNormalFight() {
        ExperienceService service = new ExperienceService();

        assertEquals(450, service.calculateVictoryXp(5, false, 3));
    }

    @Test
    public void testCalculateVictoryXpForBossFight() {
        ExperienceService service = new ExperienceService();

        assertEquals(900, service.calculateVictoryXp(5, true, 3));
    }

    @Test
    public void testDistributeXpOnlyToParticipants() throws Exception {
        ExperienceService service = new ExperienceService();

        BugemonInstance first = new BugemonInstance(createDefinition("a", "A"));
        BugemonInstance second = new BugemonInstance(createDefinition("b", "B"));
        first.setParticipatedInBattle(true);

        Team team = new Team("player", List.of(first, second));

        ExperienceResolution resolution = service.distributeVictoryXp(team, 120);

        assertEquals(1, resolution.gains().size());
        assertEquals(120, resolution.gains().get(0).gainedXp());
        assertEquals(2, first.getLevel());
        assertEquals(70, first.getXp());
        assertEquals(0, second.getXp());
    }

    @Test
    public void testDistributeXpSplitsFairlyBetweenParticipants() throws Exception {
        ExperienceService service = new ExperienceService();

        BugemonInstance first = new BugemonInstance(createDefinition("a", "A"));
        BugemonInstance second = new BugemonInstance(createDefinition("b", "B"));
        first.setParticipatedInBattle(true);
        second.setParticipatedInBattle(true);

        Team team = new Team("player", List.of(first, second));

        ExperienceResolution resolution = service.distributeVictoryXp(team, 121);

        assertEquals(2, resolution.gains().size());
        assertEquals(61, resolution.gains().get(0).gainedXp());
        assertEquals(60, resolution.gains().get(1).gainedXp());
        assertEquals(2, first.getLevel());
        assertEquals(11, first.getXp());
        assertEquals(2, second.getLevel());
        assertEquals(10, second.getXp());
    }

    @Test
    public void testGenerateLevelUpChoicesReturnsThreeDistinctChoices() {
        ExperienceService service = new ExperienceService();

        List<LevelUpBonus> choices = service.generateLevelUpChoices();

        assertEquals(3, choices.size());
        assertNotEquals(choices.get(0).description(), choices.get(1).description());
        assertNotEquals(choices.get(1).description(), choices.get(2).description());
    }

    @Test
    public void testBattleMarksInitialPlayerAsParticipant() throws Exception {
        Team playerTeam = new Team("player");
        Team enemyTeam = new Team("enemy");

        playerTeam.addBugemon(new BugemonInstance(createDefinition("a", "A")));
        enemyTeam.addBugemon(new BugemonInstance(createDefinition("b", "B")));

        Battle battle = new Battle(playerTeam, enemyTeam);

        assertTrue(battle.getPlayerActive().hasParticipatedInBattle());
    }

    private BugemonDefinition createDefinition(String id, String name) throws Exception {
        BugemonDefinition definition = new BugemonDefinition();

        setField(definition, "id", id);
        setField(definition, "name", name);
        setField(definition, "type", Type.Flora);
        setField(definition, "baseStats", new Stats(90, 55, 40, 50));
        setField(definition, "attackIds", List.of("a1"));
        setField(definition, "sprite", id + ".png");
        setField(definition, "starter", false);

        return definition;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
