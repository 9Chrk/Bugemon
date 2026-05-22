package ulb.view;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import ulb.dto.RewardChoiceDTO;
import ulb.dto.RewardKind;

import java.util.List;
import java.util.function.Consumer;

/**
 * Vue de sélection des récompenses entre deux étapes de la tour.
 */
public class RewardView extends GridPane {

    private final RewardActionHandler handler;
    private final Runnable onFinished;
    private Label titleLabel;
    private GridPane rewardContainer;
    private int selectedRewardIndex;

    /**
     * Construit l'écran de récompense.
     *
     * @param width largeur de la vue.
     * @param height hauteur de la vue.
     * @param onFinished rappel exécuté après validation d'un choix.
     */
    public RewardView(double width, double height, RewardActionHandler handler, Runnable onFinished) {
        this.handler = handler;
        this.onFinished = onFinished;
        this.setPrefSize(width, height);
        this.setPadding(new Insets(20));
        this.setAlignment(Pos.CENTER);
        setupConstraints();
        setupComponents();
    }

    /**
     * Configure les contraintes de la grille de récompense.
     */
    private void setupConstraints() {
        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(100);
        this.getColumnConstraints().add(col);

        RowConstraints rowTitle = new RowConstraints();
        rowTitle.setPercentHeight(20);
        RowConstraints rowRewards = new RowConstraints();
        rowRewards.setPercentHeight(80);
        this.getRowConstraints().addAll(rowTitle, rowRewards);
    }

    /**
     * Construit tous les composants de l'écran de récompense.
     */
    private void setupComponents() {
        titleLabel = new Label("CHOISISSEZ VOTRE RÉCOMPENSE");
        titleLabel.getStyleClass().add("miniTitle");
        GridPane.setHalignment(titleLabel, HPos.CENTER);
        this.add(titleLabel, 0, 0);
        showRewardSelection();
    }

    // ----------------- Écran 1 : choix de la récompense -----------------

    private void showRewardSelection() {
        titleLabel.setText("CHOISISSEZ VOTRE RÉCOMPENSE");
        List<RewardChoiceDTO> rewards = handler.getCurrentRewardOptions();
        rewardContainer = new GridPane();
        rewardContainer.setHgap(20);
        rewardContainer.setAlignment(Pos.CENTER);

        for (int i = 0; i < rewards.size(); i++) {
            RewardChoiceDTO reward = rewards.get(i);

            // Chaque carte garde le même index que la récompense métier.
            VBox rewardCard = new VBox(15);
            rewardCard.setAlignment(Pos.CENTER);
            rewardCard.setPadding(new Insets(20));
            rewardCard.setStyle("-fx-border-color: gold; -fx-border-width: 2; " +
                    "-fx-background-color: rgba(0,0,0,0.7); -fx-background-radius: 10; " +
                    "-fx-border-radius: 10;");

            Label nameLabel = new Label(reward.description());
            nameLabel.getStyleClass().add("statsText");
            nameLabel.setWrapText(true);
            nameLabel.setMaxWidth(200);

            Button selectBtn = createActionButton("CHOISIR", () -> handleRewardChoice(reward), true, 120);

            rewardCard.getChildren().addAll(nameLabel, selectBtn);
            rewardContainer.add(rewardCard, i, 0);

            ColumnConstraints cardCol = new ColumnConstraints();
            cardCol.setPercentWidth(100.0 / Math.max(1, rewards.size()));
            rewardContainer.getColumnConstraints().add(cardCol);
        }

        // Si aucune récompense n'a été créée, afficher un message d'erreur
        if (rewards.isEmpty()) {
            VBox errorBox = new VBox(20);
            errorBox.setAlignment(Pos.CENTER);
            errorBox.setPadding(new Insets(40));

            Label errorLabel = new Label("Aucune récompense disponible.\nVeuillez retourner à la carte.");
            errorLabel.setStyle("-fx-text-fill: #FF6B6B; -fx-font-size: 16;");
            errorLabel.setWrapText(true);

            Button backBtn = createActionButton("Retourner à la carte", onFinished, true, 180);

            errorBox.getChildren().addAll(errorLabel, backBtn);
            rewardContainer.getChildren().add(errorBox);
        }

        setContentZone(rewardContainer);
    }

    private void handleRewardChoice(RewardChoiceDTO reward) {
        this.selectedRewardIndex = reward.index();

        if (reward.kind() == RewardKind.ITEM) {
            // Les objets n'ont pas besoin de cible supplémentaire.
            handler.applyRewardChoiceItem(reward.index());
            onFinished.run();
        } else if (reward.kind() == RewardKind.STATS) {
                showBugemonSelection("Sur quel Bugémon appliquer le bonus ?",
                            this::handleStatsBugemonSelected,
                            handler.getRewardTargets(reward.index()));
        } else if (reward.kind() == RewardKind.ATTACK) {
            showBugemonSelection("Sur quel Bugémon enseigner l'attaque ?",
                    this::handleAttackBugemonSelected,
                    handler.getRewardTargets(reward.index()));
        }
    }

    // ----------------- Écran 2 : choix du Bugémon -----------------

    private void showBugemonSelection(String title, Consumer<Integer> onBugemonSelected,
            List<RewardChoiceDTO> targets) {
        titleLabel.setText(title);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        for (RewardChoiceDTO target : targets) {
            // Les cibles non compatibles sont affichées mais désactivées.
            Button btn = createActionButton(target.description(), () -> onBugemonSelected.accept(target.index()), target.selectable(), 180);
            grid.add(btn, target.index() % 3, target.index() / 3);
        }

        setContentZone(grid);
    }

    private void handleStatsBugemonSelected(int bugemonIndex) {
        handler.applyRewardChoiceStats(selectedRewardIndex, bugemonIndex);
        onFinished.run();
    }

    private void handleAttackBugemonSelected(int bugemonIndex) {
        showAttackReplaceSelection(bugemonIndex);
    }

    // ----------------- Écran 3 : choix de l'attaque à remplacer -----------------

    private void showAttackReplaceSelection(int bugemonIndex) {
        titleLabel.setText("Quelle attaque remplacer ?");
        java.util.List<String> attackLabels = handler.getRewardAttackLabels(selectedRewardIndex, bugemonIndex);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        for (int i = 0; i < attackLabels.size(); i++) {
            int attackIndex = i;
            String attackLabel = attackLabels.get(i);
            Button btn = createActionButton(attackLabel, () -> {
                handler.applyRewardChoiceReplaceAttack(selectedRewardIndex, bugemonIndex, attackIndex);
                onFinished.run();
            }, true, 180);
            grid.add(btn, i % 2, i / 2);
        }

        setContentZone(grid);
    }

    // ----------------- Fonctions d'aide -----------------

    private void setContentZone(GridPane node) {
        if (rewardContainer != null) {
            this.getChildren().remove(rewardContainer);
        }
        rewardContainer = node;
        this.add(rewardContainer, 0, 1);
    }

    /**
     * Crée un bouton d'action standardisé pour cette vue.
     *
     * @param text texte du bouton.
     * @param onClick action à exécuter au clic.
     * @param enabled indique si le bouton est actif.
     * @param prefWidth largeur préférée.
     * @return bouton configuré.
     */
    private Button createActionButton(String text, Runnable onClick, boolean enabled, double prefWidth) {
        Button btn = new Button(text);
        btn.getStyleClass().add("playBtn");
        btn.setPrefWidth(prefWidth);
        btn.setMaxWidth(prefWidth);
        btn.setWrapText(true);
        if (enabled) {
            btn.setOnAction(e -> onClick.run());
        } else {
            btn.setDisable(true);
            btn.setOpacity(0.4);
        }
        return btn;
    }
}
