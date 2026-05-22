package ulb.controller.service;

import java.util.ArrayList;
import java.util.List;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import ulb.models.battle.BattleAction;
import ulb.models.battle.AttackAction;
import ulb.models.battle.EnemyAttackAction;
import ulb.controller.BattleController;
import ulb.models.game.BugemonInstance;
import ulb.view.BattleView;

/**
 * Orchestration du flux de combat : détermine l'ordre des tours, exécute les actions
 * et gère les transitions entre étapes de combat.
 */
public class BattleOrchestrationService {
    private final BattleController battleController;
    private final BattleView battleView;

    /**
     * Construit le service d'orchestration de combat.
     *
     * @param battleController contrôleur de combat.
     * @param battleView vue de combat à mettre à jour.
     */
    public BattleOrchestrationService(BattleController battleController, BattleView battleView) {
        this.battleController = battleController;
        this.battleView = battleView;
    }

    /**
     * Détermine l'ordre du tour : objets et changements d'abord, puis attaques selon l'initiative.
     *
     * @param playerAction action choisie par le joueur.
     * @return séquence d'actions à exécuter.
     */
    public List<BattleAction> determineTurnSequence(BattleAction playerAction) {
        List<BattleAction> sequence = new ArrayList<>();

        // Les objets et changements ont toujours la priorité sur les attaques ennemies.
        if (!(playerAction instanceof AttackAction)) {
            sequence.add(playerAction);
            if (!battleController.isBattleOver()) {
                sequence.add(new EnemyAttackAction());
            }
        } else {
            // L'ordre entre deux attaques dépend de l'initiative.
            if (battleController.playerActsFirst()) {
                sequence.add(playerAction);
                sequence.add(new EnemyAttackAction());
            } else {
                sequence.add(new EnemyAttackAction());
                sequence.add(playerAction);
            }
        }
        return sequence;
    }

    /**
     * Méthode récursive qui traite chaque action de la séquence avec un délai visuel.
     *
     * @param sequence séquence d'actions à jouer.
     * @param index index de l'action courante.
     * @param onBattleEnd callback appelé quand le combat est terminé.
     * @param enemyActiveAtBeginning ennemi actif au début de la séquence.
     */
    public void executeNextActionInSequence(List<BattleAction> sequence, int index, Runnable onBattleEnd, BugemonInstance enemyActiveAtBeginning) {
        // Condition d'arrêt : fin de liste ou combat terminé en plein tour.
        if (index >= sequence.size() || battleController.isBattleOver()) {
            if (battleController.isBattleOver()) {
                onBattleEnd.run();
            } else {
                battleView.getActionMenu().setDisable(false);
                battleView.resetActionMenu();
            }
            return;
        }

        try {
            PauseTransition pause;
            // Si l'ennemi initial est déjà K.O., on saute sa riposte prévue.
            if (!battleController.isEnemyBugemonDead(enemyActiveAtBeginning)) {
                BattleAction currentAction = sequence.get(index);
                String log = battleController.executePlayerAction(currentAction);
                battleView.updateLog(log);
                battleView.updateUI();
                pause = new PauseTransition(Duration.seconds(1.5));
            }
            else{
                pause = new PauseTransition(Duration.seconds(0));
            }
            pause.setOnFinished(e -> executeNextActionInSequence(sequence, index + 1, onBattleEnd, enemyActiveAtBeginning));
            pause.play();
        } catch (RuntimeException e) {
            System.err.println("Erreur lors de l'exécution d'une action de combat : " + e.getMessage());
            e.printStackTrace();
            processActionSequenceEnd(onBattleEnd);
        }
    }

    /**
     * Gère le nettoyage de l'interface et la transition lorsqu'une séquence de tour
     * est interrompue (par exemple lorsqu'un Bugémon tombe K.O.).
     */
    public void processActionSequenceEnd(Runnable onBattleEndCallback) {
        PauseTransition pause = new PauseTransition(Duration.seconds(1.0));

        pause.setOnFinished(e -> {
            if (battleController.isBattleOver()) {
                // Si toute l'équipe est vaincue, déclenche la logique de fin de combat.
                if (onBattleEndCallback != null) {
                    onBattleEndCallback.run();
                }
            } else {
                // Sinon, redonne le contrôle au joueur pour le prochain tour.
                battleView.getActionMenu().setDisable(false);
                battleView.resetActionMenu();
                battleView.updateUI();
            }
        });

        pause.play();
    }
}


