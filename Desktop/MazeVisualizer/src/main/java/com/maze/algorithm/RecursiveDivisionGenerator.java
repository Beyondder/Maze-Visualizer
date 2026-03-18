package com.maze.algorithm;

import com.maze.model.Cell;
import com.maze.model.Maze;

import java.util.*;

/**
 * Recursive Division maze generator.
 *
 * <p>Starts with an open grid (all internal walls removed) and
 * iteratively bisects regions with a wall that has exactly one gap.
 * Produces mazes with long straight corridors and large open chambers
 * that are distinctly different from graph-traversal approaches.
 *
 * <p>The "recursion" is implemented with an explicit task stack so it
 * can be paused and resumed one step at a time.
 */
public class RecursiveDivisionGenerator implements MazeGenerator {

    /** [rowStart, rowEnd, colStart, colEnd] — region to be divided. */
    private final Deque<int[]> tasks         = new ArrayDeque<>();
    private final Set<Cell>    currentCells  = new HashSet<>();
    private final Set<Cell>    frontierCells = Collections.emptySet();
    private Maze               maze;
    private boolean            complete      = false;
    private final Random       rng           = new Random();

    @Override
    public void initialize(Maze maze) {
        this.maze = maze;
        tasks.clear();
        currentCells.clear();
        complete = false;

        int rows = maze.getRows();
        int cols = maze.getCols();

        // Open all interior walls — divide will add them back
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = maze.getCell(r, c);
                if (r > 0)       cell.setWall(Cell.TOP,    false);
                if (r < rows-1)  cell.setWall(Cell.BOTTOM, false);
                if (c > 0)       cell.setWall(Cell.LEFT,   false);
                if (c < cols-1)  cell.setWall(Cell.RIGHT,  false);
                cell.setVisited(true);
            }
        }

        tasks.push(new int[]{0, rows - 1, 0, cols - 1});
    }

    @Override
    public boolean step() {
        // Skip regions too small to divide
        while (!tasks.isEmpty()) {
            int[] task = tasks.peek();
            int h = task[1] - task[0] + 1;
            int w = task[3] - task[2] + 1;
            if (h < 2 && w < 2) { tasks.pop(); continue; }
            break;
        }

        if (tasks.isEmpty()) {
            complete = true;
            currentCells.clear();
            return false;
        }

        currentCells.clear();
        int[] task      = tasks.pop();
        int rowStart    = task[0], rowEnd = task[1];
        int colStart    = task[2], colEnd = task[3];
        int h           = rowEnd  - rowStart + 1;
        int w           = colEnd  - colStart + 1;

        // Choose orientation: horizontal wall if taller, vertical if wider
        boolean horizontal;
        if      (h < 2) horizontal = false;
        else if (w < 2) horizontal = true;
        else            horizontal = (h > w) || (h == w && rng.nextBoolean());

        if (horizontal) {
            // Wall spans a row; passage is a random column in the region
            int wallRow = rowStart + rng.nextInt(h - 1);   // row ABOVE the gap
            int passage = colStart + rng.nextInt(w);

            for (int c = colStart; c <= colEnd; c++) {
                if (c != passage) {
                    maze.getCell(wallRow,     c).setWall(Cell.BOTTOM, true);
                    maze.getCell(wallRow + 1, c).setWall(Cell.TOP,    true);
                    currentCells.add(maze.getCell(wallRow, c));
                }
            }

            // Push sub-regions (only if they can still be divided)
            if (wallRow - rowStart >= 1 || w >= 2)
                tasks.push(new int[]{rowStart,     wallRow, colStart, colEnd});
            if (rowEnd - (wallRow + 1) >= 1 || w >= 2)
                tasks.push(new int[]{wallRow + 1, rowEnd,  colStart, colEnd});

        } else {
            // Wall spans a column; passage is a random row in the region
            int wallCol = colStart + rng.nextInt(w - 1);   // col LEFT of the gap
            int passage = rowStart + rng.nextInt(h);

            for (int r = rowStart; r <= rowEnd; r++) {
                if (r != passage) {
                    maze.getCell(r, wallCol    ).setWall(Cell.RIGHT, true);
                    maze.getCell(r, wallCol + 1).setWall(Cell.LEFT,  true);
                    currentCells.add(maze.getCell(r, wallCol));
                }
            }

            if (wallCol - colStart >= 1 || h >= 2)
                tasks.push(new int[]{rowStart, rowEnd, colStart,     wallCol});
            if (colEnd - (wallCol + 1) >= 1 || h >= 2)
                tasks.push(new int[]{rowStart, rowEnd, wallCol + 1, colEnd });
        }

        return true;
    }

    @Override public boolean  isComplete()       { return complete;      }
    @Override public Set<Cell> getCurrentCells()  { return currentCells;  }
    @Override public Set<Cell> getFrontierCells() { return frontierCells; }
}
