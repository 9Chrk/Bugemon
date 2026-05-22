package ulb.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import ulb.controller.MainController;
import ulb.dto.SkillTreeNodeDTO;
import ulb.dto.SkillTreeStateDTO;
import javafx.scene.layout.Priority;
import javafx.animation.Animation;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

/**
 * Vue de l'arbre de competences permanent du joueur.
 */
public class SkillTreeView extends BorderPane {
    private static final double NODE_RADIUS = 38;
    private static final double X_SPACING = 170;
    private static final double Y_SPACING = 120;
    private static final double PANE_WIDTH = 900;
    private static final double PANE_HEIGHT = 520;

    private final MainController controller;
    private final Pane treePane;
    private final Label pointsLabel;
    private final Label statusLabel;

    /**
     * Construit la vue de l'arbre de compétences.
     *
     * @param width      largeur souhaitée de la vue.
     * @param height     hauteur souhaitée de la vue.
     * @param controller contrôleur principal appliquant les actions sur l'arbre.
     */
    public SkillTreeView(double width, double height, MainController controller) {
        this.controller = controller;
        this.setPrefSize(width, height);
        this.setPadding(new Insets(20));
        this.setStyle("-fx-background-color: rgba(8, 15, 24, 0.92);");

        Button backButton = new Button("Retour au menu");
        backButton.getStyleClass().add("backBtn");
        backButton.setOnAction(e -> controller.showMainMenu());

        Label title = new Label("Arbre de competences");
        title.getStyleClass().add("title");

        pointsLabel = new Label();
        pointsLabel.getStyleClass().add("miniTitle");

        HBox topBar = new HBox(20,title);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 16, 0));

        treePane = new Pane();
        treePane.setPrefSize(PANE_WIDTH, PANE_HEIGHT);
        treePane.setMaxSize(PANE_WIDTH, PANE_HEIGHT);
        treePane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.35); -fx-background-radius: 14; -fx-border-color: rgba(241, 196, 15, 0.35); -fx-border-radius: 14;");

        statusLabel = new Label("Clic gauche: ajouter un point. Clic droit: retirer un point.");
        statusLabel.getStyleClass().add("statsText");
        statusLabel.setWrapText(true);

        VBox centerBox = new VBox(16, treePane, statusLabel);
        centerBox.setAlignment(Pos.TOP_CENTER);

        Pane bottomBarSpacer = new Pane();
        HBox.setHgrow(bottomBarSpacer, Priority.ALWAYS);
        HBox bottomBar = new HBox(20,backButton,bottomBarSpacer, pointsLabel);
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        bottomBar.setPadding(new Insets(0, 0, 16, 0));

        this.setTop(topBar);
        this.setCenter(centerBox);
        this.setBottom(bottomBar);

        refresh();
    }

    private void refresh() {
        SkillTreeStateDTO state = controller.getSkillTreeState();
        pointsLabel.setText("Points disponibles : " + state.availablePoints());
        treePane.getChildren().clear();

        Map<String, SkillTreeNodeDTO> nodesById = new HashMap<>();
        Map<String, double[]> positions = new HashMap<>();

        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (SkillTreeNodeDTO node : state.nodes()) {
            nodesById.put(node.id(), node);
            minX = Math.min(minX, node.x());
            maxX = Math.max(maxX, node.x());
            minY = Math.min(minY, node.y());
            maxY = Math.max(maxY, node.y());
        }

        double offsetX = (PANE_WIDTH - (maxX - minX) * X_SPACING) / 2 - minX * X_SPACING;
        double offsetY = (PANE_HEIGHT - (maxY - minY) * Y_SPACING) / 2 - minY * Y_SPACING;

        for (SkillTreeNodeDTO node : state.nodes()) {
            positions.put(node.id(), new double[] {
                    node.x() * X_SPACING + offsetX,
                    node.y() * Y_SPACING + offsetY
                });
        }

        drawConnections(state, nodesById, positions);
        for (SkillTreeNodeDTO node : state.nodes()) {
            double[] pos = positions.get(node.id());
            boolean available = state.availablePoints() - node.cost() >= 0;
            drawNode(node, pos[0], pos[1], available);
        }
    }

    private void drawConnections(SkillTreeStateDTO state, Map<String, SkillTreeNodeDTO> nodesById, Map<String, double[]> positions) {
        for (SkillTreeNodeDTO node : state.nodes()) {
            for (String prerequisiteId : node.prerequisites()) {
                double[] from = positions.get(prerequisiteId);
                double[] to = positions.get(node.id());
                SkillTreeNodeDTO prerequisite = nodesById.get(prerequisiteId);
                if (from == null || to == null || prerequisite == null) {
                    continue;
                }

                Line line = new Line(from[0], from[1], to[0], to[1]);
                // La connexion s'éclaire dès qu'elle mène à une branche utilisable.
                boolean highlighted = prerequisite.active() || node.active() || node.available();
                line.setStroke(highlighted ? Color.web("#F1C40F") : Color.web("#5D6D7E"));
                line.setOpacity(highlighted ? 0.55 : 0.30);
                line.setStrokeWidth(highlighted ? 3 : 2);
                treePane.getChildren().add(line);
            }
        }
    }

    private void drawNode(SkillTreeNodeDTO node, double centerX, double centerY, boolean available) {
        StackPane visual = new StackPane();
        visual.setLayoutX(centerX - 80);
        visual.setLayoutY(centerY - 54);
        visual.setPrefSize(160, 108);
        Circle circle = new Circle(NODE_RADIUS);
        Label levelLabel = new Label(node.currentLevel() + "/" + node.maxLevel());
        levelLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        StackPane nodeGraphic = new StackPane(circle, levelLabel);
        // Un noeud actif mais non maximal peut afficher un badge d'amélioration.
        boolean upgradeable =
                node.currentLevel() > 0 &&
                node.currentLevel() < node.maxLevel();
        if (upgradeable) {
            Label upgradeBadge = new Label("+");
            if(available){
                upgradeBadge.setStyle("""
                    -fx-background-color: gold;
                    -fx-text-fill: black;
                    -fx-font-weight: bold;
                    -fx-background-radius: 12;
                    -fx-border-color: white;
                    -fx-border-radius: 12;
                    -fx-padding: 2 6 2 6;
                """);
                StackPane.setAlignment(upgradeBadge, Pos.BOTTOM_RIGHT);
                StackPane.setMargin(upgradeBadge, new Insets(4));
                // Le badge clignote uniquement si le joueur a assez de points.
                ScaleTransition pulse = new ScaleTransition(Duration.seconds(0.8), upgradeBadge);
                pulse.setFromX(1.0);
                pulse.setFromY(1.0);
                pulse.setToX(1.15);
                pulse.setToY(1.15);
                pulse.setCycleCount(Animation.INDEFINITE);
                pulse.setAutoReverse(true);
                pulse.play();
            }else{
                upgradeBadge.setStyle("""
                    -fx-background-color: grey;
                    -fx-text-fill: black;
                    -fx-font-weight: bold;
                    -fx-background-radius: 12;
                    -fx-border-color: white;
                    -fx-border-radius: 12;
                    -fx-padding: 2 6 2 6;
                """);
                StackPane.setAlignment(upgradeBadge, Pos.BOTTOM_RIGHT);
                StackPane.setMargin(upgradeBadge, new Insets(4));
            }
            nodeGraphic.getChildren().add(upgradeBadge);
        }
        Label nameLabel = new Label(node.name());
        nameLabel.getStyleClass().add("statsText");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(150);
        nameLabel.setAlignment(Pos.CENTER);
        Label costLabel = new Label(node.startNode() ? "Gratuit" : "Cout " + node.cost());
        costLabel.getStyleClass().add("statsText");
        VBox content = new VBox(6, nodeGraphic, nameLabel, costLabel);
        content.setAlignment(Pos.CENTER);
        visual.getChildren().add(content);
        applyNodeStyle(node, circle, content, available);
        Tooltip.install(visual, new Tooltip(node.description()));
        visual.setOnMouseClicked(event -> {
            boolean updated = false;
            if (event.getButton() == MouseButton.PRIMARY) {
                // Clic gauche : investissement d'un point.
                updated = controller.allocateSkillPoint(node.id());
                statusLabel.setText(updated
                        ? node.name() + " ameliore."
                        : "Ajout impossible sur " + node.name() + ".");
            } else if (event.getButton() == MouseButton.SECONDARY) {
                // Clic droit : remboursement d'un point si la branche reste valide.
                updated = controller.removeSkillPoint(node.id());
                statusLabel.setText(updated
                        ? "Point retire de " + node.name() + "."
                        : "Retrait impossible sur " + node.name() + ".");
            }
            if (updated) {
                refresh();
            }
        });
        treePane.getChildren().add(visual);
    }

    private void applyNodeStyle(SkillTreeNodeDTO node, Circle circle, VBox content, boolean available) {
        if (node.startNode() || node.active()) {
            circle.setFill(Color.web("#27AE60"));
            circle.setStroke(Color.web("#F1C40F"));
            circle.setStrokeWidth(4);
            content.setOpacity(1.0);
            return;
        }

        if (node.available() && available) {
            circle.setFill(Color.web("#2980B9"));
            circle.setStroke(Color.web("#F1C40F"));
            circle.setStrokeWidth(3);
            content.setOpacity(0.75);
            return;
        }

        circle.setFill(Color.web("#566573"));
        circle.setStroke(Color.web("#2C3E50"));
        circle.setStrokeWidth(2);
        content.setOpacity(0.45);
    }
}
