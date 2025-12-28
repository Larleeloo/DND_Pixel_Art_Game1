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

    public GameWindow() {
        setTitle("My Game");
        setUndecorated(true);
        setAlwaysOnTop(true);

        panel = new GamePanel();
        add(panel);
        pack();

        GraphicsDevice device = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();

        try {
            device.setFullScreenWindow(this);
        } catch (Exception ignored) {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }

        setVisible(true);
        panel.startGameLoop();

        // Maintain always-on-top and request focus when focus is lost
        addWindowFocusListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                panel.requestFocusInWindow();
            }

            @Override
            public void windowLostFocus(java.awt.event.WindowEvent e) {
                // Re-request focus and ensure window stays on top
                SwingUtilities.invokeLater(() -> {
                    setAlwaysOnTop(true);
                    toFront();
                    panel.requestFocusInWindow();
                });
            }
        });

        // Ensure focus on startup
        SwingUtilities.invokeLater(() -> {
            panel.requestFocusInWindow();
        });

        // Ensure focus setup is complete
        panel.addNotify();
        SwingUtilities.invokeLater(() -> {
            panel.requestFocusInWindow();
            panel.setFocusable(true);
            panel.setFocusTraversalKeysEnabled(false);
        });
    }

}
