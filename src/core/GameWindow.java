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

        addWindowFocusListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                panel.requestFocusInWindow();
            }
        });

        // 🔥 This is the critical part:
        SwingUtilities.invokeLater(() -> {
            panel.requestFocusInWindow();
        });

        // 🔥 And this ensures focus WON’T be stolen:
        panel.addNotify();
        SwingUtilities.invokeLater(() -> {
            panel.requestFocusInWindow();
            panel.setFocusable(true);
            panel.setFocusTraversalKeysEnabled(false);
        });
    }

}
