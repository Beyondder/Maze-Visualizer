package com.maze.view;

import com.maze.model.Cell;
import com.maze.model.Maze;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Resizable {@link Canvas} responsible for all maze rendering.
 * The controller feeds it state via setters, then calls {@link #render()}.
 *
 * <p>Rendering layers (bottom to top):
 * <ol>
 *   <li>Background fill</li>
 *   <li>Cell background colours (visited / frontier / solver states / path)</li>
 *   <li>Walls</li>
 *   <li>Start / End labels</li>
 * </ol>
 */
public class MazeCanvas extends Canvas {

    // ── Palette ────────────────────────────────────────────────────────
private static final Color C_BACKGROUND   = Color.web("#061417");  // deep teal-black
private static final Color C_CELL_EMPTY   = Color.web("black");  // muted teal
private static final Color C_CELL_VISITED = Color.web("black");  // desaturated cyan
private static final Color C_WALL         = Color.web("white");  // slate gray

private static final Color C_CURRENT      = Color.web("teal");  // amber (focus)
private static final Color C_FRONTIER     = Color.web("grey", 0.55); // bright cyan
private static final Color C_OPEN_SET     = Color.web("blue", 0.65); // sky blue
private static final Color C_CLOSED_SET   = Color.web("black", 0.80); // deep teal

private static final Color C_PATH         = Color.web("#486856");  // lime green (success)
private static final Color C_PATH_GLOW    = Color.web("#486856");

private static final Color C_START        = Color.web("#10b981");  // emerald
private static final Color C_END          = Color.web("#ef4444");  // soft red

private static final Color C_LABEL        = Color.web("#e2e8f0");  // light slate (readable text)

    private static final double WALL_WIDTH = 0.3;
    private static final double PADDING    = 2.0;

    // ── State fed from outside ─────────────────────────────────────────
    private Maze       maze          = null;
    private Set<Cell>  currentCells  = Collections.emptySet();
    private Set<Cell>  frontierCells = Collections.emptySet();
    private Set<Cell>  openSet       = Collections.emptySet();
    private Set<Cell>  closedSet     = Collections.emptySet();
    private List<Cell> path          = Collections.emptyList();

    // ── Constructor ────────────────────────────────────────────────────

    public MazeCanvas() {
        super(800, 680);
    }

    /** Allow the canvas to be resized by its parent layout. */
    @Override public boolean isResizable()              { return true; }
    @Override public double  prefWidth(double height)   { return getWidth();  }
    @Override public double  prefHeight(double width)   { return getHeight(); }

    // ── State setters ──────────────────────────────────────────────────

    public void setMaze(Maze maze) { this.maze = maze; }

    public void setGeneratorHighlights(Set<Cell> current, Set<Cell> frontier) {
        this.currentCells  = current  != null ? current  : Collections.emptySet();
        this.frontierCells = frontier != null ? frontier : Collections.emptySet();
    }

    public void setSolverState(Set<Cell> openSet, Set<Cell> closedSet, List<Cell> path) {
        this.openSet   = openSet   != null ? openSet   : Collections.emptySet();
        this.closedSet = closedSet != null ? closedSet : Collections.emptySet();
        this.path      = path      != null ? path      : Collections.emptyList();
    }

    public void clearGeneratorHighlights() {
        currentCells  = Collections.emptySet();
        frontierCells = Collections.emptySet();
    }

    public void clearSolverState() {
        openSet   = Collections.emptySet();
        closedSet = Collections.emptySet();
        path      = Collections.emptyList();
    }

    // ── Main render ────────────────────────────────────────────────────

    public void render() {
        GraphicsContext gc  = getGraphicsContext2D();
        double          cw  = getWidth();
        double          ch  = getHeight();

        // Background
        gc.setFill(C_BACKGROUND);
        gc.fillRect(0, 0, cw, ch);

        if (maze == null) return;

        int    rows     = maze.getRows();
        int    cols     = maze.getCols();
        double cellSize = Math.min((cw - PADDING * 2) / cols,
                                   (ch - PADDING * 2) / rows);
        double ox       = Math.round((cw - cellSize * cols) / 2.0);
        double oy       = Math.round((ch - cellSize * rows) / 2.0);

        drawCells(gc, rows, cols, cellSize, ox, oy);
        drawWalls(gc, rows, cols, cellSize, ox, oy);
        drawLabels(gc, cellSize, ox, oy);
    }

    // ── Layer: cell fills ──────────────────────────────────────────────

    private void drawCells(GraphicsContext gc, int rows, int cols,
                           double cs, double ox, double oy) {
        boolean hasPath = !path.isEmpty();
        Set<Cell> pathSet = hasPath ? new java.util.HashSet<>(path) : Collections.emptySet();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = maze.getCell(r, c);
                double x  = ox + c * cs;
                double y  = oy + r * cs;
                double sz = cs - 1;           // 1-px gap gives a subtle grid feel

                Color fill = resolveCellColor(cell, pathSet);
                gc.setFill(fill);
                gc.fillRect(x + 0.5, y + 0.5, sz, sz);

                // Extra glow pass for path cells
                if (pathSet.contains(cell) && cell != maze.getStart() && cell != maze.getEnd()) {
                    gc.setFill(C_PATH_GLOW);
                    gc.fillRect(x - 1, y - 1, cs + 2, cs + 2);
                    gc.setFill(C_PATH);
                    gc.fillRect(x + 0.5, y + 0.5, sz, sz);
                }
            }
        }
    }

    private Color resolveCellColor(Cell cell, Set<Cell> pathSet) {
        if (cell == maze.getStart())         return C_START;
        if (cell == maze.getEnd())           return C_END;
        if (pathSet.contains(cell))          return C_PATH;
        if (currentCells.contains(cell))     return C_CURRENT;
        if (closedSet.contains(cell))        return C_CLOSED_SET;
        if (openSet.contains(cell))          return C_OPEN_SET;
        if (frontierCells.contains(cell))    return C_FRONTIER;
        if (cell.isVisited())                return C_CELL_VISITED;
        return C_CELL_EMPTY;
    }

    // ── Layer: walls ───────────────────────────────────────────────────

    private void drawWalls(GraphicsContext gc, int rows, int cols,
                           double cs, double ox, double oy) {
        gc.setStroke(C_WALL);
        gc.setLineWidth(WALL_WIDTH);
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.SQUARE);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell   cell = maze.getCell(r, c);
                double x    = ox + c * cs;
                double y    = oy + r * cs;

                if (cell.hasWall(Cell.TOP))
                    gc.strokeLine(x, y, x + cs, y);
                if (cell.hasWall(Cell.RIGHT))
                    gc.strokeLine(x + cs, y, x + cs, y + cs);
                if (cell.hasWall(Cell.BOTTOM))
                    gc.strokeLine(x, y + cs, x + cs, y + cs);
                if (cell.hasWall(Cell.LEFT))
                    gc.strokeLine(x, y, x, y + cs);
            }
        }
    }

    // ── Layer: S / E labels ────────────────────────────────────────────

    private void drawLabels(GraphicsContext gc, double cs, double ox, double oy) {
        if (cs < 14) return;

        double fontSize = Math.max(8, cs * 0.38);
        gc.setFont(Font.font("Courier New", FontWeight.BOLD, fontSize));
        gc.setFill(C_LABEL);

        Cell start = maze.getStart();
        gc.fillText("",
            ox + start.getCol() * cs + cs * 0.22,
            oy + start.getRow() * cs + cs * 0.72);

        Cell end = maze.getEnd();
        gc.fillText("",
            ox + end.getCol() * cs + cs * 0.25,
            oy + end.getRow() * cs + cs * 0.72);
    }
}
