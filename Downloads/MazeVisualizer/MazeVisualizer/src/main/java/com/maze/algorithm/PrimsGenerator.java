package com.maze.algorithm;

import com.maze.model.Cell;
import com.maze.model.Maze;

import java.util.*;

/**
 * Randomised Prim's maze generator.
 *
 * <p>Grows the maze outward from a random seed cell by repeatedly selecting
 * a random wall from the frontier list.  Produces mazes with many short
 * dead-ends that fan out evenly from the centre — a noticeably different
 * texture from DFS.
 */
public class PrimsGenerator implements MazeGenerator {

    private Maze             maze;
    /** Each entry: [row-of-in-cell, col-of-in-cell, row-of-out-cell, col-of-out-cell] */
    private final List<int[]> wallList      = new ArrayList<>();
    private final Set<Cell>  currentCells  = new HashSet<>();
    private final Set<Cell>  frontierCells = new HashSet<>();
    private boolean          complete      = false;
    private final Random     rng           = new Random();

    @Override
    public void initialize(Maze maze) {
        this.maze = maze;
        wallList.clear();
        currentCells.clear();
        frontierCells.clear();
        complete = false;

        // Seed from a random cell
        Cell seed = maze.getCell(rng.nextInt(maze.getRows()), rng.nextInt(maze.getCols()));
        seed.setVisited(true);
        addWalls(seed);
    }

    /** Adds all walls from {@code cell} that lead to an unvisited neighbour. */
    private void addWalls(Cell cell) {
        int r = cell.getRow(), c = cell.getCol();
        int[][] dirs = {{-1,0},{0,1},{1,0},{0,-1}};
        for (int[] d : dirs) {
            Cell n = maze.getCell(r + d[0], c + d[1]);
            if (n != null && !n.isVisited()) {
                wallList.add(new int[]{r, c, r + d[0], c + d[1]});
                frontierCells.add(n);
            }
        }
    }

    @Override
    public boolean step() {
        if (complete || wallList.isEmpty()) {
            complete = true;
            currentCells.clear();
            frontierCells.clear();
            return false;
        }

        currentCells.clear();

        int   idx  = rng.nextInt(wallList.size());
        int[] wall = wallList.remove(idx);

        Cell in  = maze.getCell(wall[0], wall[1]);
        Cell out = maze.getCell(wall[2], wall[3]);

        if (out != null && !out.isVisited()) {
            maze.removeWallBetween(in, out);
            out.setVisited(true);
            currentCells.add(in);
            currentCells.add(out);
            addWalls(out);
        }

        // Rebuild frontier view (cells still in the wall list but not visited)
        frontierCells.clear();
        for (int[] w : wallList) {
            Cell c = maze.getCell(w[2], w[3]);
            if (c != null && !c.isVisited()) frontierCells.add(c);
        }

        if (wallList.isEmpty()) complete = true;
        return !complete;
    }

    @Override public boolean  isComplete()       { return complete;      }
    @Override public Set<Cell> getCurrentCells()  { return currentCells;  }
    @Override public Set<Cell> getFrontierCells() { return frontierCells; }
}
