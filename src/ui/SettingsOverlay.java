package ui;

import audio.AudioManager;
import core.GamePanel;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * A global settings overlay that can be displayed on top of any scene.
 * Contains volume controls and mute button for audio management.
 */
public class SettingsOverlay {

    private boolean visible;
    private AudioManager audioManager;

    // Panel dimensions and position
    private int panelX, panelY, panelWidth, panelHeight;

    // UI Components
    private UISlider musicVolumeSlider;
    private UISlider sfxVolumeSlider;
    private UIButton muteButton;
    private UIButton closeButton;

    // Colors
    private static final Color OVERLAY_BG = new Color(0, 0, 0, 180);
    private static final Color PANEL_BG = new Color(40, 40, 50, 240);
    private static final Color PANEL_BORDER = new Color(100, 100, 120);
    private static final Color TITLE_COLOR = new Color(220, 200, 160);
    private static final Color TEXT_COLOR = new Color(200, 200, 210);

    // Button colors
    private static final Color BUTTON_NORMAL = new Color(70, 70, 90);
    private static final Color BUTTON_HOVER = new Color(90, 90, 120);
    private static final Color MUTE_ACTIVE = new Color(180, 60, 60);
    private static final Color MUTE_ACTIVE_HOVER = new Color(220, 80, 80);

    public SettingsOverlay(AudioManager audioManager) {
        this.audioManager = audioManager;
        this.visible = false;

        // Center the panel on screen
        panelWidth = 400;
        panelHeight = 320;
        panelX = (GamePanel.SCREEN_WIDTH - panelWidth) / 2;
        panelY = (GamePanel.SCREEN_HEIGHT - panelHeight) / 2;

        initUI();
    }

    private void initUI() {
        int contentX = panelX + 40;
        int contentWidth = panelWidth - 80;
        int sliderHeight = 8;

        // Music Volume Slider
        musicVolumeSlider = new UISlider(
            contentX, panelY + 100, contentWidth, sliderHeight,
            "Music Volume", 0.0, 1.0, audioManager.getMusicVolume()
        );
        musicVolumeSlider.setColors(
            new Color(50, 50, 60),
            new Color(100, 160, 100),
            new Color(220, 220, 230),
            TEXT_COLOR
        );
        musicVolumeSlider.setOnChange(() -> {
            audioManager.setMusicVolume((float) musicVolumeSlider.getValue());
        });

        // SFX Volume Slider
        sfxVolumeSlider = new UISlider(
            contentX, panelY + 160, contentWidth, sliderHeight,
            "Sound Effects Volume", 0.0, 1.0, audioManager.getSFXVolume()
        );
        sfxVolumeSlider.setColors(
            new Color(50, 50, 60),
            new Color(100, 140, 180),
            new Color(220, 220, 230),
            TEXT_COLOR
        );
        sfxVolumeSlider.setOnChange(() -> {
            audioManager.setSFXVolume((float) sfxVolumeSlider.getValue());
        });

        // Mute All Button
        muteButton = new UIButton(
            contentX, panelY + 210, contentWidth, 40,
            getMuteButtonText(), this::toggleMute
        );
        updateMuteButtonColors();

        // Close Button
        closeButton = new UIButton(
            panelX + panelWidth - 50, panelY + 10, 40, 30,
            "X", this::hide
        );
        closeButton.setColors(
            new Color(80, 60, 60),
            new Color(150, 80, 80),
            Color.WHITE
        );
    }

    private String getMuteButtonText() {
        return audioManager.isMuted() ? "Unmute All" : "Mute All";
    }

    private void updateMuteButtonColors() {
        if (audioManager.isMuted()) {
            muteButton.setColors(MUTE_ACTIVE, MUTE_ACTIVE_HOVER, Color.WHITE);
        } else {
            muteButton.setColors(BUTTON_NORMAL, BUTTON_HOVER, Color.WHITE);
        }
    }

    private void toggleMute() {
        audioManager.setMuteAll(!audioManager.isMuted());
        // Reinitialize UI to update button text and colors
        initUI();
    }

    /**
     * Show the settings overlay.
     */
    public void show() {
        visible = true;
        // Refresh slider values from audio manager
        musicVolumeSlider.setValue(audioManager.getMusicVolume());
        sfxVolumeSlider.setValue(audioManager.getSFXVolume());
        // Update mute button
        initUI();
    }

    /**
     * Hide the settings overlay.
     */
    public void hide() {
        visible = false;
    }

    /**
     * Toggle the settings overlay visibility.
     */
    public void toggle() {
        if (visible) {
            hide();
        } else {
            show();
        }
    }

    /**
     * Check if the overlay is visible.
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Check if a point is within the settings panel.
     */
    public boolean containsPoint(int x, int y) {
        return visible && x >= panelX && x <= panelX + panelWidth &&
               y >= panelY && y <= panelY + panelHeight;
    }

    /**
     * Handle mouse pressed events.
     * @return true if the event was consumed by the overlay
     */
    public boolean handleMousePressed(int x, int y) {
        if (!visible) return false;

        // Handle sliders
        musicVolumeSlider.handleMousePressed(x, y);
        sfxVolumeSlider.handleMousePressed(x, y);

        // Always consume clicks when visible (to prevent clicking through)
        return true;
    }

    /**
     * Handle mouse released events.
     */
    public void handleMouseReleased(int x, int y) {
        if (!visible) return;

        musicVolumeSlider.handleMouseReleased(x, y);
        sfxVolumeSlider.handleMouseReleased(x, y);
    }

    /**
     * Handle mouse dragged events.
     */
    public void handleMouseDragged(int x, int y) {
        if (!visible) return;

        musicVolumeSlider.handleMouseDragged(x, y);
        sfxVolumeSlider.handleMouseDragged(x, y);
    }

    /**
     * Handle mouse moved events.
     */
    public void handleMouseMoved(int x, int y) {
        if (!visible) return;

        muteButton.handleMouseMove(x, y);
        closeButton.handleMouseMove(x, y);
    }

    /**
     * Handle mouse click events.
     * @return true if the event was consumed by the overlay
     */
    public boolean handleMouseClicked(int x, int y) {
        if (!visible) return false;

        // Handle buttons
        if (muteButton.handleClick(x, y)) {
            return true;
        }
        if (closeButton.handleClick(x, y)) {
            return true;
        }

        // Consume click if within panel bounds
        return containsPoint(x, y);
    }

    /**
     * Handle key press for closing overlay with ESC.
     * @return true if the key was handled
     */
    public boolean handleKeyPressed(int keyCode) {
        if (visible && keyCode == KeyEvent.VK_ESCAPE) {
            hide();
            return true;
        }
        return false;
    }

    /**
     * Draw the settings overlay.
     */
    public void draw(Graphics g) {
        if (!visible) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw darkened background overlay
        g2d.setColor(OVERLAY_BG);
        g2d.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

        // Draw panel background
        g2d.setColor(PANEL_BG);
        g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 20, 20);

        // Draw panel border
        g2d.setColor(PANEL_BORDER);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 20, 20);

        // Draw title
        g2d.setColor(TITLE_COLOR);
        g2d.setFont(new Font("Arial", Font.BOLD, 28));
        String title = "Settings";
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = panelX + (panelWidth - fm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, panelY + 50);

        // Draw separator line
        g2d.setColor(PANEL_BORDER);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawLine(panelX + 30, panelY + 70, panelX + panelWidth - 30, panelY + 70);

        // Draw sliders
        musicVolumeSlider.draw(g);
        sfxVolumeSlider.draw(g);

        // Draw mute button
        muteButton.draw(g);

        // Draw close button
        closeButton.draw(g);

        // Draw hint text at bottom
        g2d.setColor(new Color(150, 150, 160));
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        String hint = "Press ESC to close";
        fm = g2d.getFontMetrics();
        int hintX = panelX + (panelWidth - fm.stringWidth(hint)) / 2;
        g2d.drawString(hint, hintX, panelY + panelHeight - 15);
    }
}
