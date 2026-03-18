package com.maze.algorithm;

import com.maze.model.Cell;
import com.maze.model.Maze;

import java.util.*;

/**
 * Randomised Kruskal's maze generator.
 *
 * <p>Creates a shuffled list of every internal edge, then processes them
 * one by one: if the two cells belong to different sets (Union-Find),
 * remove the wall between them and merge the sets.  Produces mazes
 * with very uniform, balanced branching — lots of short dead-ends spread
 * evenly across the grid.
 */
public class KruskalsGenerator implements MazeGenerator {

    private Maze             maze;
    private List<int[]>      edges;        // [r1,c1,r2,c2]
    private int              edgeIndex;
    private int[]            parent;
    private int[]            rank;
    private final Set<Cell>  currentCells  = new HashSet<>();
    private final Set<Cell>  frontierCells = Collections.emptySet(); // Kruskal has no frontier concept
    private boolean          complete      = false;

    @Override
    public void initialize(Maze maze) {
        this.maze  = maze;
        edgeIndex  = 0;
        complete   = false;
        currentCells.clear();

        int rows  = maze.getRows();
        int cols  = maze.getCols();
        int total = rows * cols;

        parent = new int[total];
        rank   = new int[total];
        for (int i = 0; i < total; i++) parent[i] = i;

        // Build edge list (horizontal + vertical internal edges)
        edges = new ArrayList<>((rows - 1) * cols + rows * (cols - 1));
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (c + 1 < cols) edges.add(new int[]{r, c, r,     c + 1});
                if (r + 1 < rows) edges.add(new int[]{r, c, r + 1, c    });
            }
        }
        Collections.shuffle(edges);
    }

    // ── Union-Find with path compression + union by rank ─────

    private int find(int x) {
        if (parent[x] != x) parent[x] = find(parent[x]);
        return parent[x];
    }

    /** @return true if the two elements were in different sets (union performed). */
    private boolean union(int x, int y) {
        int px = find(x), py = find(y);
        if (px == py) return false;
        if (rank[px] < rank[py]) { int t = px; px = py; py = t; }
        parent[py] = px;
        if (rank[px] == rank[py]) rank[px]++;
        return true;
    }

    @Override
    public boolean step() {
        currentCells.clear();

        // Skip rejected edges, stop at the first accepted one
        while (edgeIndex < edges.size()) {
            int[] edge = edges.get(edgeIndex++);
            int r1 = edge[0], c1 = edge[1];
            int r2 = edge[2], c2 = edge[3];
            int cols = maze.getCols();

            if (union(r1 * cols + c1, r2 * cols + c2)) {
                Cell a = maze.getCell(r1, c1);
                Cell b = maze.getCell(r2, c2);
                maze.removeWallBetween(a, b);
                a.setVisited(true);
                b.setVisited(true);
                currentCells.add(a);
                currentCells.add(b);
                return true;         // one wall removed — let the renderer refresh
            }
        }

        complete = true;
        return false;
    }

    @Override public boolean  isComplete()       { return complete;      }
    @Override public Set<Cell> getCurrentCells()  { return currentCells;  }
    @Override public Set<Cell> getFrontierCells() { return frontierCells; }
}
