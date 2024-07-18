package com.thefattestcat.GameOfLife;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JPanel;

public class WorldRenderer extends JPanel {
    private Camera camera;
    private CellWorld cellWorld;

    public WorldRenderer() {
        MouseHandler mh = new MouseHandler(this);
        addKeyListener(new KeyboardHandler(this));

        addMouseListener(mh);
        addMouseMotionListener(mh);
        addMouseWheelListener(mh);
    }

    public void setCamera(Camera c) {
        camera = c;
    }

    protected Camera getCamera() {
        return camera;
    }

    protected CellWorld getWorld() {
        return cellWorld;
    }

    public void setCellWorld(CellWorld w) {
        cellWorld = w;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        Dimension d = getSize();

        double w = d.getWidth(), h = d.getHeight();
        double wh = w / 2, hh = h / 2;

        double x = camera.xPos, y = camera.yPos, scaleInv = 1 / camera.scale;

        g2d.setBackground(Color.BLACK);
        g2d.clearRect(0, 0, (int) w, (int) h);

        g2d.translate(wh, hh);
        g2d.scale(camera.scale, camera.scale);
        g2d.translate(-x, -y);

        double startX = x - wh * scaleInv, startY = y - hh * scaleInv, 
               endX = x + wh * scaleInv, endY = y + hh * scaleInv;

        drawWorld(g2d, (int) startX, (int) startY, (int) endX, (int) endY);

        g2d.dispose();
    }

    private void drawWorld(Graphics2D g2d, int startX, int startY, int endX, int endY) {
        drawCellWorld(g2d, startX, startY, endX, endY);
        drawGrid(g2d, startX, startY, endX, endY, Global.GRID_DARK, Global.GRID_LIGHT);
        drawBarrier(g2d, cellWorld.isPaused() ? Color.RED : Color.YELLOW);
    }

    private void drawCellWorld(Graphics2D g2d, int startX, int startY, int endX, int endY) {
        if (cellWorld == null) return;
        cellWorld.draw(g2d, startX, startY, endX, endY);
    }

    private void drawBarrier(Graphics2D g2d, Color col) {
        g2d.setColor(col);
        int wc = Settings.getWorldSize() * Global.CELL_SIZE;
        g2d.drawLine(0, 0, wc, 0);
        g2d.drawLine(0, 0, 0, wc);
        g2d.drawLine(0, wc, wc, wc);
        g2d.drawLine(wc, 0, wc, wc);
    }

    private void drawGrid(Graphics2D g2d, int startX, int startY, int endX, int endY, Color c1, Color c2) {
        int startGridX = (int) Util.ceilAlign(startX, Global.CELL_SIZE);
        int startGridY = (int) Util.ceilAlign(startY, Global.CELL_SIZE);
        int m = Global.CELL_SIZE * 16;

        g2d.setColor(c1);

        for (int x = startGridX; x < endX; x += Global.CELL_SIZE) {
            if (x % m != 0) {
                g2d.drawLine(x, startY, x, endY);
            }
            else {
                g2d.setColor(c2);
                g2d.drawLine(x, startY, x, endY);
                g2d.setColor(c1);
            }
        }

        for (int y = startGridY; y < endY; y += Global.CELL_SIZE) {
            if (y % m != 0) {
                g2d.drawLine(startX, y, endX, y);
            }
            else {
                g2d.setColor(c2);
                g2d.drawLine(startX, y, endX, y);
                g2d.setColor(c1);
            }
        }
    }

    private void drawDebugPoints(Graphics2D g2d, int startX, int startY, int endX, int endY) {
        g2d.fillArc(startX - 3, startY - 3, 6, 6, 0, 360);
        g2d.fillArc(endX - 3, endY - 3, 6, 6, 0, 360);
        g2d.fillArc((startX + endX) / 2 - 3, (startY + endY) / 2 - 3, 6, 6, 0, 360);
    }
}

enum BuildingMode {
    NONE,
    PLACING,
    DESTROYING
}

class MouseHandler implements MouseListener, MouseMotionListener, MouseWheelListener {
    private final WorldRenderer wr;
    private int prevMouseX, prevMouseY;
    private boolean isDragging = false;
    private BuildingMode buildingMode = BuildingMode.NONE;

    public MouseHandler(WorldRenderer wr) {
        this.wr = wr;
    }

    private Point getCellWorldPosition(int mouseX, int mouseY) {
        Camera c = wr.getCamera();
        Dimension d = wr.getSize();

        double globalX = c.xPos + (mouseX - d.getWidth() / 2) / c.scale;
        double globalY = c.yPos + (mouseY - d.getHeight() / 2) / c.scale;

        int worldX = (int) Math.floor(globalX / Global.CELL_SIZE);
        int worldY = (int) Math.floor(globalY / Global.CELL_SIZE);

        return new Point(worldX, worldY);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        double w = e.getPreciseWheelRotation();

        Dimension d = wr.getSize();

        double dx = e.getX() - d.getWidth() / 2;
        double dy = e.getY() - d.getHeight() / 2;
        Camera c = wr.getCamera();

        if (w < 0) {
            if (c.scale * Global.SCALE_MULTIPLIER > Global.MIN_SCALE) return;

            double m = 1 / c.scale * (1 - 1 / Global.SCALE_MULTIPLIER);
            c.xPos += dx * m;
            c.yPos += dy * m;
            c.scale *= Global.SCALE_MULTIPLIER;
        }
        else {
            if (c.scale < Global.MAX_SCALE * Global.SCALE_MULTIPLIER) return;

            c.scale /= Global.SCALE_MULTIPLIER;
            double m = 1 / c.scale * (1 - 1 / Global.SCALE_MULTIPLIER);
            c.xPos -= dx * m;
            c.yPos -= dy * m;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (isDragging) {
            int dx = e.getX() - prevMouseX;
            int dy = e.getY() - prevMouseY;

            Camera c = wr.getCamera();
            c.xPos -= dx / c.scale;
            c.yPos -= dy / c.scale;

            prevMouseX = e.getX();
            prevMouseY = e.getY();
            return;
        }
        if (buildingMode != BuildingMode.NONE) {
            CellState st = buildingMode == BuildingMode.PLACING ? CellState.ACTIVE : CellState.INACTIVE;
            Point worldPos = getCellWorldPosition(e.getX(), e.getY());
            CellWorld w = wr.getWorld();

            CellState cell = w.get(worldPos.x, worldPos.y);

            if (!w.hasUserPoint(worldPos)) {
                if (cell != st) w.put(worldPos, st);
            }
            else {
                if (buildingMode == BuildingMode.DESTROYING && cell == CellState.INACTIVE) w.put(worldPos, CellState.INACTIVE);
            }

            return;
        }
        
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        wr.grabFocus();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        
    }

    @Override
    public void mousePressed(MouseEvent e) {
        prevMouseX = e.getX();
        prevMouseY = e.getY();

        if (e.getButton() == 3 /* right button */) {
            isDragging = true;
            return;
        }

        if (e.getButton() == 1 /* left button */) {
            CellWorld w = wr.getWorld();
            Point worldPos = getCellWorldPosition(e.getX(), e.getY());
            int worldX = worldPos.x, worldY = worldPos.y;

            CellState st = w.get(worldX, worldY);
            if (st == null) return;

            if (st == CellState.ACTIVE || w.hasUserPoint(worldPos)) {
                w.put(worldPos, CellState.INACTIVE);
                buildingMode = BuildingMode.DESTROYING;
            }
            else {
                w.put(worldPos, CellState.ACTIVE);
                buildingMode = BuildingMode.PLACING;
            }

            return;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == 1) buildingMode = BuildingMode.NONE;
        else if (e.getButton() == 3) isDragging = false;
    }

}

class KeyboardHandler implements KeyListener {
    private final WorldRenderer wr;

    public KeyboardHandler(WorldRenderer wr) {
        this.wr = wr;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == Settings.getSimulationButton()) {
            wr.getWorld().setPaused(!wr.getWorld().isPaused());
        }
        else if (e.getKeyCode() == Settings.getClearFieldButton()) {
            wr.getWorld().clear();
        }
        else if (e.getKeyCode() == Settings.getSimulationStepButton()) {
            if (wr.getWorld().isPaused()) wr.getWorld().step();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        
    }

    @Override
    public void keyTyped(KeyEvent e) {
        
    }

}