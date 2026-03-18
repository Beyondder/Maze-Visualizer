package com.maze.controller;

import com.maze.algorithm.*;
import com.maze.model.Maze;
import com.maze.view.MazeCanvas;
import javafx.animation.AnimationTimer;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Central controller that owns the {@link Maze}, the active
 * {@link MazeGenerator}, and the {@link AStarSolver}.
 *
 * <p>An {@link AnimationTimer} drives frame updates.  The target
 * steps-per-second is a live field so the speed slider can change it
 * at any time without restarting the animation loop.
 */
public class MazeController {

    // ── Algorithm registry ─────────────────────────────────────────────
    private static final Map<String, MazeGenerator> GENERATORS = new LinkedHashMap<>();
    static {
        GENERATORS.put("DFS — Recursive Backtracker", new DFSGenerator());
        GENERATORS.put("Prim's",                      new PrimsGenerator());
        GENERATORS.put("Kruskal's",                   new KruskalsGenerator());
        GENERATORS.put("Recursive Division",           new RecursiveDivisionGenerator());
    }

    // ── Core state ─────────────────────────────────────────────────────
    private Maze           maze;
    private int            rows = 20, cols = 20;
    private MazeGenerator  activeGenerator = null;
    private final AStarSolver solver        = new AStarSolver();

    // ── Flags ──────────────────────────────────────────────────────────
    private boolean generating       = false;
    private boolean solving          = false;
    private boolean generationDone   = false; // true after *any* complete generation
    private boolean stepperDirty     = false; // true once a manual-step session started

    // ── Animation ──────────────────────────────────────────────────────
    private AnimationTimer animationTimer;
    private long           lastStepNs   = 0;
    private double         stepsPerSec  = 30.0;   // changed live by speed slider

    // ── View ───────────────────────────────────────────────────────────
    private final MazeCanvas canvas;
    private Runnable          onStateChange = null;

    // ── Constructor ────────────────────────────────────────────────────

    public MazeController(MazeCanvas canvas) {
        this.canvas = canvas;
        buildFreshMaze();
    }

    // ── Public commands ────────────────────────────────────────────────

    /** Start animated generation for the named algorithm. */
    public void generate(String algoName) {
        stopAnimation();
        buildFreshMaze();
        activeGenerator = GENERATORS.get(algoName);
        if (activeGenerator == null) return;
        activeGenerator.initialize(maze);
        generating     = true;
        generationDone = false;
        stepperDirty   = false;

        startAnimationTimer(() -> {
            boolean cont = activeGenerator.step();
            canvas.setGeneratorHighlights(
                activeGenerator.getCurrentCells(),
                activeGenerator.getFrontierCells()
            );
            if (!cont) {
                generating     = false;
                generationDone = true;
                canvas.clearGeneratorHighlights();
                stopAnimation();
                notifyStateChange();
            }
            canvas.render();
        });

        notifyStateChange();
    }

    /**
     * Manually advance one step.  A new maze is initialised automatically
     * if none is in progress.
     */
    public void step(String algoName) {
        if (generating || solving) return;

        // Start a fresh step-through session whenever needed
        if (!stepperDirty || activeGenerator == null || activeGenerator.isComplete()) {
            buildFreshMaze();
            activeGenerator = GENERATORS.get(algoName);
            if (activeGenerator == null) return;
            activeGenerator.initialize(maze);
            generationDone = false;
            stepperDirty   = true;
        }

        boolean cont = activeGenerator.step();
        if (cont) {
            canvas.setGeneratorHighlights(
                activeGenerator.getCurrentCells(),
                activeGenerator.getFrontierCells()
            );
        } else {
            generationDone = true;
            canvas.clearGeneratorHighlights();
        }
        canvas.render();
        notifyStateChange();
    }

    /** Run A* solver with step-by-step animation. */
    public void solve() {
        if (generating || solving || !generationDone) return;

        maze.resetPathfinding();
        solver.initialize(maze);
        canvas.clearSolverState();
        solving = true;

        startAnimationTimer(() -> {
            boolean cont = solver.step();
            canvas.setSolverState(solver.getOpenSet(), solver.getClosedSet(), solver.getPath());
            if (!cont) {
                solving = false;
                stopAnimation();
                notifyStateChange();
            }
            canvas.render();
        });

        notifyStateChange();
    }

    /** Stop the animation timer (does not reset the maze). */
    public void stopAnimation() {
        if (animationTimer != null) animationTimer.stop();
        animationTimer = null;
        generating = false;
        solving    = false;
        notifyStateChange();
    }

    /** Full reset — clears maze, solver state, all flags. */
    public void reset() {
        stopAnimation();
        activeGenerator = null;
        generationDone  = false;
        stepperDirty    = false;
        buildFreshMaze();
        notifyStateChange();
    }

    /** Change maze dimensions; takes effect on next generate/reset. */
    public void setSize(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
    }

    /** Update live speed (steps/sec).  Effective immediately — no restart needed. */
    public void setSpeed(double stepsPerSec) {
        this.stepsPerSec = Math.max(0.5, stepsPerSec);
    }

    /** Force a single render pass (e.g. after canvas resize). */
    public void renderOnce() { canvas.render(); }

    // ── Queries ────────────────────────────────────────────────────────

    public boolean isGenerating()      { return generating;     }
    public boolean isSolving()         { return solving;        }
    public boolean isGenerationDone()  { return generationDone; }

    public java.util.Set<String> getAlgorithmNames() {
        return GENERATORS.keySet();
    }

    public void setOnStateChange(Runnable r) { this.onStateChange = r; }

    // ── Internal helpers ───────────────────────────────────────────────

    private void buildFreshMaze() {
        maze = new Maze(rows, cols);
        canvas.setMaze(maze);
        canvas.clearGeneratorHighlights();
        canvas.clearSolverState();
        canvas.render();
    }

    /**
     * Starts the {@link AnimationTimer} that calls {@code stepFn} at the
     * rate governed by {@link #stepsPerSec}.  For high speeds, multiple
     * logical steps are batched into a single rendered frame.
     */
    private void startAnimationTimer(Runnable stepFn) {
        if (animationTimer != null) animationTimer.stop();
        lastStepNs = 0;

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastStepNs == 0) { lastStepNs = now; return; }

                double sps         = stepsPerSec;
                long   nsPerStep   = (long)(1_000_000_000.0 / sps);
                long   elapsed     = now - lastStepNs;

                // Clamp batch size to avoid spiral-of-death at very high speeds
                int steps = (int) Math.min(elapsed / nsPerStep, 60);
                if (steps <= 0) return;

                lastStepNs += steps * nsPerStep;
                for (int i = 0; i < steps; i++) {
                    stepFn.run();
                    // stepFn stops the timer when done → check
                    if (animationTimer == null) return;
                }
            }
        };
        animationTimer.start();
    }

    private void notifyStateChange() {
        if (onStateChange != null) onStateChange.run();
    }
}
