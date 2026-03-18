package com.maze.algorithm;

import com.maze.model.Cell;
import com.maze.model.Maze;

import java.util.Set;

/**
 * Common contract for all step-based maze generation algorithms.
 *
 * <p>Usage pattern:
 * <pre>
 *   generator.initialize(maze);
 *   while (generator.step()) {
 *       render(generator.getCurrentCells(), generator.getFrontierCells());
 *   }
 * </pre>
 */
public interface MazeGenerator {

    /** Prepares the algorithm to run on {@code maze} (maze must be reset). */
    void initialize(Maze maze);

    /**
     * Executes one logical step of the algorithm.
     *
     * @return {@code true} if the algorithm is still running,
     *         {@code false} when it has finished.
     */
    boolean step();

    /** Whether generation is fully complete. */
    boolean isComplete();

    /**
     * The cell(s) being actively processed this step — rendered with
     * the "current" highlight colour.
     */
    Set<Cell> getCurrentCells();

    /**
     * Candidate cells on the frontier (not yet carved) — rendered with
     * a dimmer highlight colour to show algorithm progress.
     */
    Set<Cell> getFrontierCells();
}
