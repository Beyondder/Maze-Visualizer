package com.maze.model;

/**
 * Represents a single cell in the maze grid.
 * Walls are indexed: TOP=0, RIGHT=1, BOTTOM=2, LEFT=3.
 * Also carries A* pathfinding fields (g/h costs and parent pointer).
 */
public class Cell {

    public static final int TOP    = 0;
    public static final int RIGHT  = 1;
    public static final int BOTTOM = 2;
    public static final int LEFT   = 3;

    private final int row;
    private final int col;

    /** true  = wall exists,  false = wall removed (passage) */
    private final boolean[] walls = {true, true, true, true};
    private boolean visited = false;

    // ── A* fields ─────────────────────────────────────────────
    private Cell   parent = null;
    private double gCost  = Double.MAX_VALUE;
    private double hCost  = 0.0;

    // ── Constructor ───────────────────────────────────────────
    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
    }

    // ── Maze-generation reset (full) ──────────────────────────
    public void reset() {
        walls[TOP] = walls[RIGHT] = walls[BOTTOM] = walls[LEFT] = true;
        visited = false;
        resetPathfinding();
    }

    // ── Pathfinding-only reset (keeps maze structure) ─────────
    public void resetPathfinding() {
        parent = null;
        gCost  = Double.MAX_VALUE;
        hCost  = 0.0;
    }

    // ── Getters / Setters ─────────────────────────────────────
    public int getRow() { return row; }
    public int getCol() { return col; }

    public boolean hasWall(int dir)              { return walls[dir]; }
    public void    setWall(int dir, boolean val) { walls[dir] = val;  }
    public void    removeWall(int dir)           { walls[dir] = false; }

    public boolean isVisited()             { return visited; }
    public void    setVisited(boolean v)   { visited = v;    }

    public Cell   getParent()             { return parent; }
    public void   setParent(Cell parent)  { this.parent = parent; }

    public double getGCost()              { return gCost; }
    public void   setGCost(double g)      { this.gCost = g; }

    public double getHCost()              { return hCost; }
    public void   setHCost(double h)      { this.hCost = h; }

    public double getFCost() { return gCost + hCost; }

    @Override
    public String toString() {
        return "Cell(" + row + ", " + col + ")";
    }
}
