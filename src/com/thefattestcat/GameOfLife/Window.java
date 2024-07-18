package com.thefattestcat.GameOfLife;

import java.awt.*;

import javax.swing.JFrame;


public class Window extends JFrame {
    private WorldRenderer worldRenderer;
    
    public Window(Dimension size) {
        setTitle("Game Of Life");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        worldRenderer = new WorldRenderer();
        add(worldRenderer);
        pack();
        
        setSize(size);
        setResizable(false);
    }

    public WorldRenderer getWorldRenderer() {
        return worldRenderer;
    }
}
