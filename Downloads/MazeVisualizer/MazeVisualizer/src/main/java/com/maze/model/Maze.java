package com.maze.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The full maze grid.  Owns the 2-D array of {@link Cell} objects and
 * provides helpers used by generation algorithms and the A* solver.
 */
public class Maze {

    private final int rows;
    private final int cols;
    private Cell[][] cells;

    // ── Constructor ───────────────────────────────────────────
    public Maze(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        allocate();
    }

    // ── Internal allocation ───────────────────────────────────
    private void allocate() {
        cells = new Cell[rows][cols];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                cells[r][c] = new Cell(r, c);
    }

    // ── Full reset (generation can restart) ───────────────────
    public void reset() {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                cells[r][c].reset();
    }

    // ── Pathfinding reset (walls stay intact) ─────────────────
    public void resetPathfinding() {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                cells[r][c].resetPathfinding();
    }

    // ── Accessors ─────────────────────────────────────────────
    public int getRows() { return rows; }
    public int getCols() { return cols; }

    /** Returns null for out-of-bounds coordinates. */
    public Cell getCell(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) return null;
        return cells[row][col];
    }

    public Cell[][] getCells() { return cells; }

    public Cell getStart() { return cells[0][0]; }
    public Cell getEnd()   { return cells[rows - 1][cols - 1]; }

    // ── Wall utilities ────────────────────────────────────────

    /**
     * Removes the shared wall between two orthogonally adjacent cells.
     * Both cells' wall arrays are updated.
     */
    public void removeWallBetween(Cell a, Cell b) {
        int dr = b.getRow() - a.getRow();
        int dc = b.getCol() - a.getCol();

        if      (dr == -1) { a.removeWall(Cell.TOP);    b.removeWall(Cell.BOTTOM); }
        else if (dr ==  1) { a.removeWall(Cell.BOTTOM); b.removeWall(Cell.TOP);    }
        else if (dc == -1) { a.removeWall(Cell.LEFT);   b.removeWall(Cell.RIGHT);  }
        else if (dc ==  1) { a.removeWall(Cell.RIGHT);  b.removeWall(Cell.LEFT);   }
    }

    /**
     * Returns all four orthogonal neighbours (null where out-of-bounds).
     * Order: TOP, RIGHT, BOTTOM, LEFT.
     */
    public Cell[] getNeighbours(Cell cell) {
        int r = cell.getRow();
        int c = cell.getCol();
        return new Cell[]{
            getCell(r - 1, c),
            getCell(r,     c + 1),
            getCell(r + 1, c),
            getCell(r,     c - 1)
        };
    }

    /**
     * Returns neighbours reachable from {@code cell} — i.e. where the
     * shared wall has been removed.  Used by the A* solver.
     */
    public List<Cell> getAccessibleNeighbours(Cell cell) {
        List<Cell> result = new ArrayList<>(4);
        int r = cell.getRow();
        int c = cell.getCol();

        Cell top    = getCell(r - 1, c);
        Cell right  = getCell(r,     c + 1);
        Cell bottom = getCell(r + 1, c);
        Cell left   = getCell(r,     c - 1);

        if (!cell.hasWall(Cell.TOP)    && top    != null) result.add(top);
        if (!cell.hasWall(Cell.RIGHT)  && right  != null) result.add(right);
        if (!cell.hasWall(Cell.BOTTOM) && bottom != null) result.add(bottom);
        if (!cell.hasWall(Cell.LEFT)   && left   != null) result.add(left);

        return result;
    }
}
