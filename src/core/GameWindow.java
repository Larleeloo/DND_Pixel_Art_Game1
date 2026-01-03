package core;
import scene.*;
import entity.*;
import entity.player.*;
import input.*;
import audio.*;
import graphics.*;

import java.awt.*;
import javax.swing.*;

public class GameWindow extends JFrame {
    private GamePanel panel;

    // Target window size - borderless 1920x1080 for consistent experience across monitors
    private static final int WINDOW_WIDTH = 1920;
    private static final int WINDOW_HEIGHT = 1080;

    public GameWindow() {
        setTitle("The Amber Moon");
        setUndecorated(true);  // Borderless window
        setResizable(false);

        panel = new GamePanel();
        add(panel);

        // Set preferred size for the panel and pack the window
        panel.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        pack();

        // Center the window on screen
        setLocationRelativeTo(null);

        // Allow window to lose focus normally (don't force always-on-top)
        setAlwaysOnTop(false);

        setVisible(true);
        panel.startGameLoop();

        // Request focus on startup, but don't aggressively grab it later
        addWindowFocusListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                panel.requestFocusInWindow();
            }
            // Don't re-grab focus when lost - let user switch to other apps
        });

        // Initial focus setup
        SwingUtilities.invokeLater(() -> {
            panel.requestFocusInWindow();
            panel.setFocusable(true);
            panel.setFocusTraversalKeysEnabled(false);
        });
    }

}
