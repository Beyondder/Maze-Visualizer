package com.maze.algorithm;

import com.maze.model.Cell;
import com.maze.model.Maze;

import java.util.*;

/**
 * Depth-First Search / Recursive Backtracker maze generator.
 *
 * <p>Carves passages by randomly walking depth-first through the grid.
 * Produces mazes with long, winding corridors and few dead-ends relative
 * to their length — often considered the most "maze-like" result.
 */
public class DFSGenerator implements MazeGenerator {

    private Maze maze;
    private final Deque<Cell> stack        = new ArrayDeque<>();
    private final Set<Cell>   currentCells = new HashSet<>();
    private final Set<Cell>   frontierCells= new HashSet<>();
    private boolean           complete     = false;
    private final Random      rng          = new Random();

    @Override
    public void initialize(Maze maze) {
        this.maze = maze;
        stack.clear();
        currentCells.clear();
        frontierCells.clear();
        complete = false;

        Cell start = maze.getCell(0, 0);
        start.setVisited(true);
        stack.push(start);
    }

    @Override
    public boolean step() {
        if (complete || stack.isEmpty()) {
            complete = true;
            currentCells.clear();
            frontierCells.clear();
            return false;
        }

        currentCells.clear();
        Cell current = stack.peek();
        currentCells.add(current);

        // Collect unvisited neighbours
        List<Cell> unvisited = new ArrayList<>();
        for (Cell n : maze.getNeighbours(current)) {
            if (n != null && !n.isVisited()) unvisited.add(n);
        }

        if (unvisited.isEmpty()) {
            // Back-track
            stack.pop();
            if (!stack.isEmpty()) currentCells.add(stack.peek());
        } else {
            // Carve passage to a random unvisited neighbour
            Cell next = unvisited.get(rng.nextInt(unvisited.size()));
            maze.removeWallBetween(current, next);
            next.setVisited(true);
            stack.push(next);
            currentCells.add(next);
        }

        // Update frontier highlight
        frontierCells.clear();
        if (!stack.isEmpty()) {
            for (Cell n : maze.getNeighbours(stack.peek())) {
                if (n != null && !n.isVisited()) frontierCells.add(n);
            }
        }

        if (stack.isEmpty()) complete = true;
        return !complete;
    }

    @Override public boolean  isComplete()      { return complete;      }
    @Override public Set<Cell> getCurrentCells() { return currentCells;  }
    @Override public Set<Cell> getFrontierCells(){ return frontierCells; }
}
