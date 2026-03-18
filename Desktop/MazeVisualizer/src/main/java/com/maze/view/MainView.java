package com.maze.view;

import com.maze.controller.MazeController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

/**
 * Root view — a {@link BorderPane} with a styled sidebar on the left and
 * the resizable {@link MazeCanvas} filling the centre.
 */
public class MainView extends BorderPane {

    // ── Sub-components ─────────────────────────────────────────────────
    private final MazeCanvas      canvas     = new MazeCanvas();
    private final MazeController  controller = new MazeController(canvas);

    // ── Controls (fields so updateUI() can reach them) ─────────────────
    private ComboBox<String> algoCombo;
    private ComboBox<String> sizeCombo;
    private Slider           speedSlider;
    private Label            speedLabel;
    private Button           generateBtn;
    private Button           stepBtn;
    private Button           stopBtn;
    private Button           solveBtn;
    private Button           resetBtn;
    private Label            statusLabel;

    // ── Constructor ────────────────────────────────────────────────────

    public MainView() {
        getStyleClass().add("root-pane");
        setLeft(buildSidebar());
        setCenter(buildCanvasPane());

        controller.setOnStateChange(this::updateUI);
        updateUI();
    }

    // ══ Sidebar ════════════════════════════════════════════════════════

    private VBox buildSidebar() {
        VBox box = new VBox();
        box.getStyleClass().add("sidebar");
        box.setPrefWidth(248);

        // ── Title ──────────────────────────────────────────────────────
        VBox titleBlock = new VBox(3);
        titleBlock.setPadding(new Insets(26, 20, 18, 20));
        Label title  = new Label("Maze Algorithm Visualizer");
        title.getStyleClass().add("title-main");
        Label sub    = new Label("VISUALIZER");
        sub.getStyleClass().add("title-sub");
        titleBlock.getChildren().addAll(title);

        // ── Body (scrollable if window is tiny) ────────────────────────
        VBox body = new VBox(14);
        body.setPadding(new Insets(0, 20, 20, 20));
        VBox.setVgrow(body, Priority.ALWAYS);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        body.getChildren().addAll(
            buildAlgorithmSection(),
            buildSizeSection(),
            buildSpeedSection(),
            makeSep(),
            buildGenerationButtons(),
            makeSep(),
            buildSolveSection(),
            makeSep(),
            buildStatusBlock(),
            spacer,
            buildLegend()
        );
        VBox.setVgrow(body, Priority.ALWAYS);

        box.getChildren().addAll(titleBlock, makeSep(), body);
        return box;
    }

    // ── Section: algorithm ─────────────────────────────────────────────

    private VBox buildAlgorithmSection() {
        algoCombo = new ComboBox<>();
        algoCombo.getItems().addAll(controller.getAlgorithmNames());
        algoCombo.getSelectionModel().selectFirst();
        algoCombo.setMaxWidth(Double.MAX_VALUE);
        algoCombo.getStyleClass().add("combo-dark");

        return labelledSection("ALGORITHM", algoCombo);
    }

    // ── Section: maze size ─────────────────────────────────────────────

    private VBox buildSizeSection() {
        sizeCombo = new ComboBox<>();
        sizeCombo.getItems().addAll("10 × 10","15 × 15","20 × 20",
                                    "25 × 25","30 × 30","40 × 40","50 × 50");
        sizeCombo.getSelectionModel().select(2);   // default 20×20
        sizeCombo.setMaxWidth(Double.MAX_VALUE);
        sizeCombo.getStyleClass().add("combo-dark");
        sizeCombo.setOnAction(e -> applySize());

        return labelledSection("MAZE SIZE", sizeCombo);
    }

    // ── Section: speed ─────────────────────────────────────────────────

    private VBox buildSpeedSection() {
        speedSlider = new Slider(1, 200, 30);
        speedSlider.getStyleClass().add("speed-slider");

        speedLabel = new Label("30");
        speedLabel.getStyleClass().add("speed-value");
        speedLabel.setMinWidth(32);

        speedSlider.valueProperty().addListener((obs, o, n) -> {
            int v = n.intValue();
            speedLabel.setText(String.valueOf(v));
            controller.setSpeed(v);
        });

        HBox row = new HBox(8, speedSlider, speedLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(speedSlider, Priority.ALWAYS);

        return labelledSection("SPEED  (steps/sec)", row);
    }

    // ── Section: generation buttons ────────────────────────────────────

    private VBox buildGenerationButtons() {
        generateBtn = makeButton("  GENERATE",  "btn-primary");
        stepBtn     = makeButton("  NEXT STEP",       "btn-secondary");
        stopBtn     = makeButton("■  STOP",      "btn-warning");

        generateBtn.setOnAction(e -> controller.generate(algoCombo.getValue()));
        stepBtn    .setOnAction(e -> controller.step(algoCombo.getValue()));
        stopBtn    .setOnAction(e -> controller.stopAnimation());

        VBox vb = new VBox(8, generateBtn, stepBtn, stopBtn);
        vb.setFillWidth(true);
        return vb;
    }

    // ── Section: solve ─────────────────────────────────────────────────

    private VBox buildSolveSection() {
        solveBtn = makeButton("SOLVE WITH A*", "btn-solve");
        resetBtn = makeButton("↺  RESET",         "btn-reset");

        solveBtn.setOnAction(e -> controller.solve());
        resetBtn.setOnAction(e -> { applySize(); controller.reset(); });

        VBox vb = new VBox(8, solveBtn, resetBtn);
        vb.setFillWidth(true);
        return vb;
    }

    // ── Section: status ────────────────────────────────────────────────

    private VBox buildStatusBlock() {
        statusLabel = new Label("Click Generate to create a maze.");
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        return new VBox(statusLabel);
    }

    // ── Section: legend ────────────────────────────────────────────────

    private VBox buildLegend() {
        VBox box = new VBox(6);
        box.getStyleClass().add("legend-box");

        Label title = new Label("LEGEND");
        title.getStyleClass().add("section-label");
        box.getChildren().add(title);

        box.getChildren().addAll(
            legendRow("#10b981",    "Start cell"),
            legendRow("#f43f5e",    "End cell"),
            legendRow("#c084fc",    "Current cell"),
            legendRow("#6d28d9aa", "Frontier"),
            legendRow("#0ea5e9aa", "A* open set"),
            legendRow("#1e3a8acc", "A* closed set"),
            legendRow("#4ade80",    "Solution path")
        );

        return box;
    }

    private HBox legendRow(String hex, String label) {
        Rectangle swatch = new Rectangle(11, 11);
        swatch.setFill(Color.web(hex));
        swatch.setArcWidth(3); swatch.setArcHeight(3);

        Label lbl = new Label(label);
        lbl.getStyleClass().add("legend-label");

        HBox row = new HBox(8, swatch, lbl);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    // ══ Canvas pane ════════════════════════════════════════════════════

    private StackPane buildCanvasPane() {
        StackPane pane = new StackPane(canvas);
        pane.getStyleClass().add("canvas-pane");

        canvas.widthProperty() .bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());

        // Re-render on resize
        canvas.widthProperty() .addListener((obs, o, n) -> controller.renderOnce());
        canvas.heightProperty().addListener((obs, o, n) -> controller.renderOnce());

        return pane;
    }

    // ══ State synchronisation ══════════════════════════════════════════

    private void updateUI() {
        boolean busy     = controller.isGenerating() || controller.isSolving();
        boolean genDone  = controller.isGenerationDone();

        generateBtn.setDisable(busy);
        stepBtn    .setDisable(busy);
        algoCombo  .setDisable(busy);
        sizeCombo  .setDisable(busy);
        stopBtn    .setDisable(!busy);
        solveBtn   .setDisable(busy || !genDone);
        resetBtn   .setDisable(busy);

        if (controller.isGenerating()) {
            statusLabel.setText("Generating maze…");
        } else if (controller.isSolving()) {
            statusLabel.setText("Solving with A*…");
        } else if (genDone) {
            statusLabel.setText("Maze ready!  Click  ✦ Solve  to find the shortest path.");
        } else {
            statusLabel.setText("Click  ⚡ Generate  or step through manually.");
        }
    }

    // ══ Helpers ════════════════════════════════════════════════════════

    private void applySize() {
        String sel = sizeCombo.getValue();       // e.g. "20 × 20"
        int    n   = Integer.parseInt(sel.split(" ")[0]);
        controller.setSize(n, n);
    }

    private Button makeButton(String text, String styleClass) {
        Button b = new Button(text);
        b.getStyleClass().addAll("maze-btn", styleClass);
        b.setMaxWidth(Double.MAX_VALUE);
        return b;
    }

    private VBox labelledSection(String label, javafx.scene.Node control) {
        Label lbl = new Label(label);
        lbl.getStyleClass().add("section-label");
        VBox vb = new VBox(6, lbl, control);
        return vb;
    }

    private Separator makeSep() {
        Separator s = new Separator();
        s.getStyleClass().add("sidebar-sep");
        return s;
    }
}
