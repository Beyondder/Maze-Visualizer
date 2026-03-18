package com.maze.algorithm;

import com.maze.model.Cell;
import com.maze.model.Maze;

import java.util.*;

/**
 * Step-based A* pathfinding solver.
 *
 * <p>Uses Manhattan distance as the admissible heuristic.
 * Call {@link #initialize(Maze)} once after generation completes,
 * then repeatedly call {@link #step()} until {@link #isComplete()} is true.
 * If {@link #isSolved()} is true afterwards the path can be read from
 * {@link #getPath()}.
 */
public class AStarSolver {

    private Maze               maze;
    private PriorityQueue<Cell> openQueue;  // ordered by f-cost
    private final Set<Cell>    openSet     = new HashSet<>();  // fast membership test
    private final Set<Cell>    closedSet   = new HashSet<>();
    private List<Cell>         path        = Collections.emptyList();
    private boolean            complete    = false;
    private boolean            solved      = false;

    // ── Public API ────────────────────────────────────────────

    /** Resets solver state and starts from maze.getStart() → maze.getEnd(). */
    public void initialize(Maze maze) {
        this.maze = maze;
        maze.resetPathfinding();

        openQueue = new PriorityQueue<>(Comparator.comparingDouble(Cell::getFCost));
        openSet.clear();
        closedSet.clear();
        path      = Collections.emptyList();
        complete  = false;
        solved    = false;

        Cell start = maze.getStart();
        Cell end   = maze.getEnd();

        start.setGCost(0.0);
        start.setHCost(heuristic(start, end));
        openQueue.add(start);
        openSet.add(start);
    }

    /**
     * Processes the best candidate in the open set.
     *
     * @return {@code true} if still running, {@code false} when finished.
     */
    public boolean step() {
        if (complete || openQueue.isEmpty()) {
            complete = true;
            return false;
        }

        Cell current = openQueue.poll();
        openSet.remove(current);

        // Goal reached
        if (current == maze.getEnd()) {
            path     = reconstructPath(current);
            complete = true;
            solved   = true;
            return false;
        }

        closedSet.add(current);

        for (Cell neighbour : maze.getAccessibleNeighbours(current)) {
            if (closedSet.contains(neighbour)) continue;

            double tentativeG = current.getGCost() + 1.0;

            if (tentativeG < neighbour.getGCost()) {
                neighbour.setParent(current);
                neighbour.setGCost(tentativeG);
                neighbour.setHCost(heuristic(neighbour, maze.getEnd()));

                if (!openSet.contains(neighbour)) {
                    openQueue.add(neighbour);
                    openSet.add(neighbour);
                } else {
                    // Re-add to update priority (Java's PQ doesn't support decrease-key)
                    openQueue.remove(neighbour);
                    openQueue.add(neighbour);
                }
            }
        }

        return true;
    }

    // ── Helpers ───────────────────────────────────────────────

    /** Manhattan distance — admissible for a grid with unit costs. */
    private double heuristic(Cell a, Cell b) {
        return Math.abs(a.getRow() - b.getRow()) + Math.abs(a.getCol() - b.getCol());
    }

    private List<Cell> reconstructPath(Cell end) {
        List<Cell> p = new ArrayList<>();
        Cell c = end;
        while (c != null) { p.add(c); c = c.getParent(); }
        Collections.reverse(p);
        return Collections.unmodifiableList(p);
    }

    // ── Getters ───────────────────────────────────────────────

    public boolean    isComplete()  { return complete;  }
    public boolean    isSolved()    { return solved;    }
    public Set<Cell>  getOpenSet()  { return openSet;   }
    public Set<Cell>  getClosedSet(){ return closedSet; }
    public List<Cell> getPath()     { return path;      }
}
