package kaleidoskop;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    private static final int CONTROL_PANEL_WIDTH = 300;
    private Pane graphicPane;
    private TextField strokeWidthField;
    private Button clearButton;
    private List<Circle> circles;
    private ColorPicker colorPicker;
    private Slider transparencySlider;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        graphicPane = new Pane();
        graphicPane.setBackground(null);
        updateBackground();

        VBox controlPanel = new VBox(10);
        controlPanel.setPadding(new Insets(10));
        controlPanel.setPrefWidth(CONTROL_PANEL_WIDTH);
        controlPanel.setStyle("-fx-background-color: #8080FF; -fx-border-color: #FFFF80; -fx-border-width: 3; -fx-border-radius: 10;");

        Label fillColorLabel = new Label("Barva výplně");
        colorPicker = new ColorPicker(Color.RED);
        Label transparencyLabel = new Label("Průhlednost");
        transparencySlider = new Slider(10, 90, 20);
        transparencySlider.setShowTickMarks(true);
        transparencySlider.setShowTickLabels(true);
        transparencySlider.setMajorTickUnit(10);
        Label strokeWidthLabel = new Label("Šířka obrysu");
        strokeWidthField = new TextField("3");
        clearButton = new Button("Vymazat");
        clearButton.setDisable(true);
        clearButton.setAlignment(Pos.BOTTOM_CENTER);
        clearButton.setOnAction(e -> clearCanvas());

        VBox controls = new VBox(10, fillColorLabel, colorPicker, transparencyLabel, transparencySlider, strokeWidthLabel, strokeWidthField);
        HBox buttonsBox = new HBox(clearButton);
        VBox.setVgrow(controls, javafx.scene.layout.Priority.ALWAYS);
        VBox.setVgrow(buttonsBox, javafx.scene.layout.Priority.ALWAYS);
        controls.setAlignment(Pos.TOP_CENTER);
        buttonsBox.setAlignment(Pos.BOTTOM_CENTER);

        controlPanel.getChildren().addAll(controls, buttonsBox);
        root.setLeft(controlPanel);
        root.setCenter(graphicPane);

        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.titleProperty().setValue("Kaleidoskop");
        primaryStage.show();

        circles = new ArrayList<>();
        drawCircle();

        graphicPane.widthProperty().addListener((obs, oldVal, newVal) -> clearCanvas());
        graphicPane.heightProperty().addListener((obs, oldVal, newVal) -> clearCanvas());
    }

    private void clearCanvas() {
        graphicPane.getChildren().clear();
        circles.clear();
        clearButton.setDisable(true);
        updateBackground();
    }

    private void drawCircle() {
        graphicPane.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown()) {
                Circle circleToDraw = new Circle();
                circleToDraw.setStrokeWidth(getValidStrokeWidth());
                circleToDraw.setFill(colorPicker.getValue().deriveColor(0, 1, 1, 1 - transparencySlider.getValue() / 100));
                circleToDraw.setStroke(Color.WHITE.deriveColor(0, 1, 1, 1 - transparencySlider.getValue() / 100));
                circleToDraw.setStrokeType(StrokeType.OUTSIDE);
                circleToDraw.setCenterX(event.getX());
                circleToDraw.setCenterY(event.getY());
                graphicPane.getChildren().add(circleToDraw);
                circles.add(circleToDraw);
                clearButton.setDisable(false);
            }
        });

        graphicPane.setOnMouseDragged(event -> {
            if (event.isPrimaryButtonDown() && !circles.isEmpty()) {
                Circle circleToDraw = circles.get(circles.size() - 1);
                double radius = Math.hypot(event.getX() - circleToDraw.getCenterX(), event.getY() - circleToDraw.getCenterY());
                circleToDraw.setRadius(radius);
            }
        });

        graphicPane.setOnMouseReleased(event -> {
            if (!circles.isEmpty()) {
                Circle circleToDraw = circles.get(circles.size() - 1);
                double radius = Math.hypot(event.getX() - circleToDraw.getCenterX(), event.getY() - circleToDraw.getCenterY());
                circleToDraw.setRadius(radius);
                drawSymmetricCircles(circleToDraw);
                clearButton.setDisable(false);
            }
        });
    }

    private void drawSymmetricCircles(Circle circle) {
        double paneWidth = graphicPane.getWidth();
        double paneHeight = graphicPane.getHeight();

        Circle circleX = new Circle(paneWidth - circle.getCenterX(), circle.getCenterY(), circle.getRadius());
        circleX.setFill(circle.getFill());
        circleX.setStroke(circle.getStroke());
        circleX.setStrokeType(circle.getStrokeType());
        circleX.setStrokeWidth(circle.getStrokeWidth());
        graphicPane.getChildren().add(circleX);
        circles.add(circleX);

        Circle circleY = new Circle(circle.getCenterX(), paneHeight - circle.getCenterY(), circle.getRadius());
        circleY.setFill(circle.getFill());
        circleY.setStroke(circle.getStroke());
        circleY.setStrokeType(circle.getStrokeType());
        circleY.setStrokeWidth(circle.getStrokeWidth());
        graphicPane.getChildren().add(circleY);
        circles.add(circleY);

        Circle circleXY = new Circle(paneWidth - circle.getCenterX(), paneHeight - circle.getCenterY(), circle.getRadius());
        circleXY.setFill(circle.getFill());
        circleXY.setStroke(circle.getStroke());
        circleXY.setStrokeType(circle.getStrokeType());
        circleXY.setStrokeWidth(circle.getStrokeWidth());
        graphicPane.getChildren().add(circleXY);
        circles.add(circleXY);
    }

    private double getValidStrokeWidth() {
        try {
            return Double.parseDouble(strokeWidthField.getText());
        } catch (NumberFormatException e) {
            showErrorMessage("Neplatná hodnota", "Šířka obrysu musí být číslo.");
            strokeWidthField.requestFocus();
            return 0;
        }
    }

    private void updateBackground() {
        Stop[] stops = new Stop[]{new Stop(0, Color.WHITE), new Stop(0.5, Color.CYAN), new Stop(1, Color.LIGHTGRAY)};
        RadialGradient gradient = new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE, stops);
        graphicPane.setBackground(new javafx.scene.layout.Background(new javafx.scene.layout.BackgroundFill(gradient, javafx.scene.layout.CornerRadii.EMPTY, javafx.geometry.Insets.EMPTY)));
    }

    private void showErrorMessage(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
