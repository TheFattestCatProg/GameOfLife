package com.thefattestcat.GameOfLife;

import java.awt.Point;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.Timer;

enum CellState {
    INACTIVE,
    ACTIVE,
}


public class CellWorld implements ActionListener {
    public static final Color CELL_COLOR = Color.DARK_GRAY;
    public static final Color PLACE_COLOR = new Color(147, 196, 62);
    public static final Color DESTROY_COLOR = new Color(222, 126, 42);

    private final CellState[] cells = new CellState[Settings.getWorldSize() * Settings.getWorldSize()];
    private UpdateBuffer current = new UpdateBuffer();
    private UpdateBuffer next = new UpdateBuffer();

    private final HashSet<Point> userPoints = new HashSet<>();

    private boolean isPaused = true;

    private final Timer simulationTimer;


    public CellWorld() {
        simulationTimer = new Timer(Settings.getSimulationTime(), this);

        for (int i = 0; i < cells.length; i++) {
            cells[i] = CellState.INACTIVE;
        }
    }

    public void setPaused(boolean p) {
        isPaused = p;
        if (!p) simulationTimer.start();
        else simulationTimer.stop();
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void clear() {
        current.reset();
        next.reset();

        for (int i = 0; i < cells.length; i++) {
            cells[i] = CellState.INACTIVE;
        }
    }

    public void step() {
        putUserPoints();
        swap();

        for (final Entry<Point, CellState> v : current.values()) {
            if (v.getValue() != null) {
                Point p = v.getKey();
                set(p.x, p.y, v.getValue());
            }
        }
        for (final Point p : current.points()) {
            processPoint(p);
        }
    }

    private void putUserPoints() {
        for (final Point p : userPoints) {
            CellState st = get(p.x, p.y);

            if (st == CellState.ACTIVE) st = CellState.INACTIVE;
            else st = CellState.ACTIVE;

            next.add(p.x, p.y, st);
            considerNextAround(p.x, p.y);
        }

        userPoints.clear();
    }

    private void processPoint(Point p) {
        int x = p.x, y = p.y;
        CellState c = get(x, y);
        CellState r = processRules(x, y, c);

        if (c != r) {
            next.add(x, y, r);
            considerNextAround(x, y);
        }
    }

    public boolean isValidPos(int x, int y) {
        if (x < 0 || x >= Settings.getWorldSize()) return false;
        if (y < 0 || y >= Settings.getWorldSize()) return false;

        return true;
    }

    public CellState get(int x, int y) {
        if (!isValidPos(x, y)) return null;
        return cells[x + y * Settings.getWorldSize()];
    }

    public void put(int x, int y, CellState state) {
        if (!isPaused) return;
        if (state == null) return;

        Point p = new Point(x, y);
        if (userPoints.contains(p)) userPoints.remove(p);
        else userPoints.add(p);
    }

    public void put(Point p, CellState state) {
        if (!isPaused) return;
        if (state == null) return;

        if (userPoints.contains(p)) userPoints.remove(p);
        else userPoints.add(p);
    }

    public boolean hasUserPoint(Point p) {
        return userPoints.contains(p);
    }

    public boolean hasUserPoint(int x, int y) {
        return userPoints.contains(new Point(x, y));
    }

    private void set(int x, int y, CellState state) {
        cells[x + y * Settings.getWorldSize()] = state;
    }

    protected CellState[] getCells() {
        return cells;
    }

    protected void draw(Graphics2D g2d, int startX, int startY, int endX, int endY) {
        final int cs = Global.CELL_SIZE;
        final int ws = Settings.getWorldSize();
        
        g2d.setColor(CELL_COLOR);

        int startWorldX = (int) Math.floor( (double) startX / cs );
        int startWorldY = (int) Math.floor( (double) startY / cs );

        int endWorldX = (int) Math.ceil( (double) endX / cs );
        int endWorldY = (int) Math.ceil( (double) endY / cs );

        if (startWorldX < 0) startWorldX = 0;
        if (startWorldY < 0) startWorldY = 0;
        if (endWorldX > ws) endWorldX = ws;
        if (endWorldY > ws) endWorldY = ws;

        for (int y = startWorldY; y < endWorldY; y++) {
            for (int x = startWorldX; x < endWorldX; x++) {
                int i = x + y * ws;
                Point p = new Point(x, y);

                if (userPoints.contains(p)) {
                    if (cells[i] == CellState.ACTIVE) {
                        g2d.setColor(DESTROY_COLOR);
                        g2d.fillRect(x * cs, y * cs, cs, cs);
                        g2d.setColor(CELL_COLOR);
                    }
                    else {
                        g2d.setColor(PLACE_COLOR);
                        g2d.fillRect(x * cs, y * cs, cs, cs);
                        g2d.setColor(CELL_COLOR);
                    }
                }
                else if (cells[i] == CellState.ACTIVE) {
                    g2d.fillRect(x * cs, y * cs, cs, cs);
                }
            }
        }
    }

    private CellState processRules(int x, int y, CellState currentState) {
        int aliveAmount = 0;

        for (int i = x - 1; i <= x + 1; i++)
            if (get(i, y - 1) == CellState.ACTIVE) aliveAmount++;

        if (get(x - 1, y) == CellState.ACTIVE) aliveAmount++;
        if (get(x + 1, y) == CellState.ACTIVE) aliveAmount++;

        for (int i = x - 1; i <= x + 1; i++)
            if (get(i, y + 1) == CellState.ACTIVE) aliveAmount++;

        if (aliveAmount < 2 || aliveAmount > 3) return CellState.INACTIVE;

        if (aliveAmount == 3 && currentState == CellState.INACTIVE) return CellState.ACTIVE;
        return currentState;
    }

    private void considerNextAround(int x, int y) {
        for (int i = x - 1; i <= x + 1; i++)
            if (get(i, y - 1) != null) next.add(i, y - 1, null);

        if (get(x - 1, y) != null) next.add(x - 1, y, null);
        if (get(x + 1, y) != null) next.add(x + 1, y, null);

        for (int i = x - 1; i <= x + 1; i++)
            if (get(i, y + 1) != null) next.add(i, y + 1, null);
    }

    private void swap() {
        UpdateBuffer b = current;
        current = next;
        next = b;
        next.reset();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        step();
    }
}


class UpdateBuffer {
    private final PointFabric pointFabric = new PointFabric();
    private final HashSet<Point> updateSet = new HashSet<>();
    private final HashMap<Point, CellState> valueSet = new HashMap<>();

    public void reset() {
        pointFabric.reset();
        updateSet.clear();
        valueSet.clear();
    }

    public void add(int x, int y, CellState state) {
        Point p = pointFabric.getNext(x, y);
        updateSet.add(p);
        
        if (state == null) return;

        CellState st = valueSet.get(p);
        if (st == null) {
            valueSet.put(p, state);
            return;
        }
        if (st == state) return;

        throw new RuntimeException("Contradictory states");
    }

    public Set<Point> points() {
        return updateSet;
    }

    public Set<Entry<Point, CellState>> values() {
        return valueSet.entrySet();
    }
}

class PointFabric {
    private ArrayList<Point> buffer = new ArrayList<>();
    private int nextIndex = 0;

    public Point getNext(int x, int y) {
        Point p;
        try {
            p = buffer.get(nextIndex++);
            p.setLocation(x, y);
            return p;
        }
        catch (IndexOutOfBoundsException err) {
            p = new Point(x, y);
            buffer.add(p);
            return p;
        }
    }

    public void reset() {
        nextIndex = 0;
    }
}