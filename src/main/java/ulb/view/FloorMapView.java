package ulb.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import ulb.controller.MainController;
import ulb.dto.FloorMapDTO;
import ulb.dto.FloorRoomDTO;
import ulb.models.game.RoomType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Vue de la carte d'étage pour la navigation H14 (Histoire 14).
 * <p>
 * Le joueur clique sur une salle adjacente pour s'y déplacer. L'action
 * déclenchée dépend du type de salle atteinte (combat, bonus ou boss).
 * </p>
 * 
 */
public class FloorMapView extends StackPane {

    /** Espacement en pixels entre deux salles adjacentes. */
    private static final double CELL_SIZE = 160;

    private final MainController controller;
    private final Pane mapPane;
    private final Label floorLabel;

    // Effet de halo doré lorsque que on survol sur une salle

    /**
     * Construit la vue de carte d'étage.
     *
     * @param width      largeur de la vue.
     * @param height     hauteur de la vue.
     * @param controller contrôleur principal du jeu.
     */
    public FloorMapView(double width, double height, MainController controller) {
        this.controller = controller;
        this.setPrefSize(width, height);

        // --- Arrière-plan dynamique selon l'étage ---
        int currentFloor = controller.getCurrentFloorMap().currentFloor();
        String bgPath = String.format(ViewConfig.FLOOR_BG_PATH_FORMAT, currentFloor);
        String cssUrl = Objects.requireNonNull(getClass().getResource(bgPath)).toExternalForm();
        this.setStyle(
            "-fx-background-image: url('" + cssUrl + "'); " +
            "-fx-background-size: cover; " +
            "-fx-background-position: center;"
        );

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: rgba(0, 0, 0, 0.40);");

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPrefWidth(800);
        topBar.setMaxWidth(800);
        topBar.setPadding(new Insets(0, 8, 0, 8));
        Button menuButton = new Button("Retour au menu");
        menuButton.setId("floorMapBackToMenu");
        menuButton.getStyleClass().add("backBtn");
        menuButton.setMinWidth(160);
        menuButton.setOnAction(e -> controller.showMainMenu());
        topBar.getChildren().add(menuButton);

        floorLabel = new Label();
        floorLabel.getStyleClass().add("miniTitle");

        mapPane = new Pane();
        mapPane.setPrefSize(800, 500);
        mapPane.setMaxSize(800, 500);

        StackPane mapContainer = new StackPane(mapPane);
        mapContainer.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(topBar, floorLabel, mapContainer);
        this.getChildren().add(layout);
        this.setAlignment(Pos.CENTER);

        refresh();
    }

    /**
     * Reconstruit la carte à partir du DTO courant.
     */
    public void refresh() {
        mapPane.getChildren().clear();
        FloorMapDTO map = controller.getCurrentFloorMap();
        floorLabel.setText("Étage " + map.currentFloor() + " : " + getFloorName(map.currentFloor()));

        // Le décalage centre la carte même si ses coordonnées sont négatives.
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        for (FloorRoomDTO room : map.rooms()) {
            minX = Math.min(minX, room.x());
            maxX = Math.max(maxX, room.x());
            minY = Math.min(minY, room.y());
            maxY = Math.max(maxY, room.y());
        }
        double offsetX = (800 - (maxX - minX) * CELL_SIZE) / 2 - minX * CELL_SIZE;
        double offsetY = (500 - (maxY - minY) * CELL_SIZE) / 2 - minY * CELL_SIZE;

        // Index des positions pour tracer les liens
        Map<String, double[]> positions = new HashMap<>();
        for (FloorRoomDTO room : map.rooms()) {
            double cx = room.x() * CELL_SIZE + offsetX;
            double cy = room.y() * CELL_SIZE + offsetY;
            positions.put(room.id(), new double[] { cx, cy });
        }

        // Tracer les liens entre salles adjacentes
        drawConnections(map, positions);

        // Dessiner chaque salle
        for (FloorRoomDTO room : map.rooms()) {
            double[] pos = positions.get(room.id());
            drawRoom(room, pos[0], pos[1]);
        }
    }

    /**
     * Trace les lignes de connexion entre salles voisines à partir des
     * connexions fournies par le DTO (issues du modèle).
     *
     * @param map       DTO de la carte d'étage courante.
     * @param positions coordonnées pixel (cx, cy) indexées par identifiant de
     *                  salle.
     */
    private void drawConnections(FloorMapDTO map, Map<String, double[]> positions) {
        for (ulb.dto.FloorConnectionDTO conn : map.connections()) {
            double[] from = positions.get(conn.fromId());
            double[] to = positions.get(conn.toId());
            if (from == null || to == null)
                continue;

            Line line = new Line(from[0], from[1], to[0], to[1]);
            line.setStroke(Color.web("#BDC3C7"));
            line.setStrokeWidth(3);
            line.setOpacity(0.4);
            mapPane.getChildren().add(line);
        }
    }

    /**
     * Dessine une salle sur la carte sous forme de cercle coloré avec un label.
     * Les salles accessibles sont cliquables et déclenchent un hover doré.
     *
     * @param room DTO de la salle à dessiner.
     * @param cx   coordonnée pixel X du centre de la salle.
     * @param cy   coordonnée pixel Y du centre de la salle.
     */
    private void drawRoom(FloorRoomDTO room, double cx, double cy) {
        RoomView roomNode = new RoomView(room);

        // On demande au node sa taille calculée (qui sera 140px de haut grâce au
        // setMinHeight)
        double width = roomNode.prefWidth(-1);
        double height = roomNode.prefHeight(-1);

        // Placement : Centre théorique (cx, cy) - Moitié de la taille du node
        roomNode.setLayoutX(cx - width / 2);
        roomNode.setLayoutY(cy - height / 2);

        roomNode.setOnRoomAction(() -> {
            boolean alreadyVisited = room.visited();
            RoomType reached = controller.enterRoom(room.id());
            if (reached == null) return;

            if (alreadyVisited) {
                // Une salle déjà visitée se contente de rafraîchir l'état visuel.
                refresh();
            } else {
                // Une nouvelle salle déclenche son comportement métier.
                controller.handleMapRoom(reached);
            }
        });

        mapPane.getChildren().add(roomNode);
    }

    private String getFloorName(int floor) {
        return switch (floor) {
            case 2  -> "Accueil";
            case 3  -> "Cafétéria";
            case 4  -> "Centre de Soin";
            case 5  -> "Open Space";
            case 6  -> "Laboratoire";
            case 7  -> "Bureau du Professeur";
            case 8  -> "Entrepôt";
            case 9  -> "Labo Secret";
            case 10 -> "Salle de Surveillance";
            default -> "Étage " + floor;
        };
    }

}
