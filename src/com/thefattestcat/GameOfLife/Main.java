package com.thefattestcat.GameOfLife;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Settings.loadFromFile("settings.cfg");

        Window window = new Window(Settings.getWindowSize());
        CellWorld cellWorld = new CellWorld();

        WorldRenderer worldRenderer = window.getWorldRenderer();
        worldRenderer.setCellWorld(cellWorld);

        Camera camera = new Camera();

        worldRenderer.setCamera(camera);
        window.setVisible(true);

        cellWorld.setPaused(true);

        while (window.isVisible()) {
            if (!cellWorld.isPaused()) worldRenderer.revalidate();
            worldRenderer.repaint();
            Thread.sleep(Settings.getRenderTime());
        }
    }
}