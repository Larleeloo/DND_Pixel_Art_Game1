package ui;

import audio.AudioManager;
import core.GamePanel;
import input.ControllerBindings;
import input.ControllerManager;
import input.KeyBindings;
import scene.SceneManager;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * A comprehensive settings overlay with tabbed interface.
 * Includes Audio settings, Key/Controller rebindings, Game options (vibration), and Quick Actions.
 * Replaces the in-game UI buttons with a centralized settings menu.
 */
public class SettingsOverlay {

    private boolean visible;
    private AudioManager audioManager;

    // Panel dimensions and position
    private int panelX, panelY, panelWidth, panelHeight;

    // Tabs
    private static final String[] TAB_NAMES = {"Audio", "Controls", "Game", "Actions"};
    private int currentTab = 0;
    private Rectangle[] tabRects;

    // UI Components - Audio Tab
    private UISlider musicVolumeSlider;
    private UISlider sfxVolumeSlider;
    private UIButton muteButton;

    // UI Components - Controls Tab
    private boolean showingControllerBindings = false;  // Toggle between keyboard/controller
    private UIButton controlsToggleButton;

    // Keyboard bindings
    private List<KeyBindButton> keyBindButtons;
    private KeyBindButton currentlyRebindingKey = null;
    private boolean waitingForKeyInput = false;

    // Controller bindings
    private List<ControllerBindButton> controllerBindButtons;
    private ControllerBindButton currentlyRebindingController = null;
    private boolean waitingForControllerInput = false;

    private UIButton resetControlsButton;

    // UI Components - Game Tab
    private UIButton vibrationToggle;
    private UIButton dayNightToggle;
    private UIButton debugToggle;

    // UI Components - Actions Tab
    private UIButton exitGameButton;
    private UIButton returnToMenuButton;
    private UIButton customizeButton;
    private UIButton musicToggleButton;

    // Close button
    private UIButton closeButton;

    // Day/Night and Debug state (managed externally)
    private boolean nightMode = false;
    private boolean debugMode = false;

    // Callbacks for actions
    private Runnable onDayNightToggle;
    private Runnable onDebugToggle;
    private Runnable onCustomize;

    // Colors
    private static final Color OVERLAY_BG = new Color(0, 0, 0, 200);
    private static final Color PANEL_BG = new Color(35, 35, 45, 245);
    private static final Color PANEL_BORDER = new Color(100, 100, 120);
    private static final Color TITLE_COLOR = new Color(220, 200, 160);
    private static final Color TEXT_COLOR = new Color(200, 200, 210);
    private static final Color TAB_ACTIVE = new Color(60, 60, 80);
    private static final Color TAB_INACTIVE = new Color(40, 40, 55);
    private static final Color TAB_HOVER = new Color(70, 70, 95);

    // Button colors
    private static final Color BUTTON_NORMAL = new Color(70, 70, 90);
    private static final Color BUTTON_HOVER = new Color(90, 90, 120);
    private static final Color BUTTON_ACTIVE = new Color(80, 150, 80);
    private static final Color BUTTON_ACTIVE_HOVER = new Color(100, 180, 100);
    private static final Color BUTTON_DANGER = new Color(180, 60, 60);
    private static final Color BUTTON_DANGER_HOVER = new Color(220, 80, 80);
    private static final Color BUTTON_REBIND = new Color(50, 80, 120);
    private static final Color BUTTON_REBIND_ACTIVE = new Color(200, 150, 50);
    private static final Color BUTTON_CONTROLLER = new Color(50, 120, 80);
    private static final Color BUTTON_CONTROLLER_HOVER = new Color(70, 160, 110);

    public SettingsOverlay(AudioManager audioManager) {
        this.audioManager = audioManager;
        this.visible = false;

        // Larger panel for comprehensive settings
        panelWidth = 700;
        panelHeight = 550;
        panelX = (GamePanel.SCREEN_WIDTH - panelWidth) / 2;
        panelY = (GamePanel.SCREEN_HEIGHT - panelHeight) / 2;

        initTabs();
        initUI();
    }

    private void initTabs() {
        tabRects = new Rectangle[TAB_NAMES.length];
        int tabWidth = (panelWidth - 40) / TAB_NAMES.length;
        for (int i = 0; i < TAB_NAMES.length; i++) {
            tabRects[i] = new Rectangle(panelX + 20 + i * tabWidth, panelY + 50, tabWidth - 5, 35);
        }
    }

    private void initUI() {
        int contentX = panelX + 40;
        int contentWidth = panelWidth - 80;

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

        initAudioTab(contentX, contentWidth);
        initControlsTab(contentX, contentWidth);
        initGameTab(contentX, contentWidth);
        initActionsTab(contentX, contentWidth);
    }

    private void initAudioTab(int contentX, int contentWidth) {
        int sliderHeight = 8;
        int startY = panelY + 120;

        // Music Volume Slider
        musicVolumeSlider = new UISlider(
            contentX, startY, contentWidth, sliderHeight,
            "Music Volume", 0.0, 1.0, audioManager != null ? audioManager.getMusicVolume() : 0.5
        );
        musicVolumeSlider.setColors(
            new Color(50, 50, 60),
            new Color(100, 160, 100),
            new Color(220, 220, 230),
            TEXT_COLOR
        );
        musicVolumeSlider.setOnChange(() -> {
            if (audioManager != null) {
                audioManager.setMusicVolume((float) musicVolumeSlider.getValue());
            }
        });

        // SFX Volume Slider
        sfxVolumeSlider = new UISlider(
            contentX, startY + 70, contentWidth, sliderHeight,
            "Sound Effects Volume", 0.0, 1.0, audioManager != null ? audioManager.getSFXVolume() : 0.5
        );
        sfxVolumeSlider.setColors(
            new Color(50, 50, 60),
            new Color(100, 140, 180),
            new Color(220, 220, 230),
            TEXT_COLOR
        );
        sfxVolumeSlider.setOnChange(() -> {
            if (audioManager != null) {
                audioManager.setSFXVolume((float) sfxVolumeSlider.getValue());
            }
        });

        // Mute All Button
        muteButton = new UIButton(
            contentX, startY + 140, contentWidth, 45,
            getMuteButtonText(), this::toggleMute
        );
        updateMuteButtonColors();
    }

    private void initControlsTab(int contentX, int contentWidth) {
        int startY = panelY + 100;

        // Toggle between Keyboard and Controller bindings
        controlsToggleButton = new UIButton(
            contentX, startY, contentWidth, 35,
            getControlsToggleText(), () -> {
                showingControllerBindings = !showingControllerBindings;
                controlsToggleButton.setText(getControlsToggleText());
                updateControlsToggleColors();
                waitingForKeyInput = false;
                waitingForControllerInput = false;
                currentlyRebindingKey = null;
                currentlyRebindingController = null;
            }
        );
        updateControlsToggleColors();

        initKeyboardBindings(contentX, contentWidth);
        initControllerBindings(contentX, contentWidth);

        // Reset to Defaults button
        resetControlsButton = new UIButton(
            contentX + contentWidth / 4, panelY + panelHeight - 100,
            contentWidth / 2, 40,
            "Reset to Defaults", () -> {
                if (showingControllerBindings) {
                    ControllerBindings.getInstance().resetAllToDefaults();
                    for (ControllerBindButton cbb : controllerBindButtons) {
                        cbb.updateText();
                    }
                } else {
                    KeyBindings.getInstance().resetAllToDefaults();
                    for (KeyBindButton kbb : keyBindButtons) {
                        kbb.updateText();
                    }
                }
            }
        );
        resetControlsButton.setColors(BUTTON_DANGER, BUTTON_DANGER_HOVER, Color.WHITE);
    }

    private void initKeyboardBindings(int contentX, int contentWidth) {
        keyBindButtons = new ArrayList<>();
        int startY = panelY + 145;
        int buttonHeight = 32;
        int spacing = 38;

        String[] actions = {
            KeyBindings.MOVE_LEFT, KeyBindings.MOVE_RIGHT,
            KeyBindings.MOVE_UP, KeyBindings.MOVE_DOWN,
            KeyBindings.JUMP, KeyBindings.SPRINT,
            KeyBindings.INTERACT, KeyBindings.INVENTORY,
            KeyBindings.ATTACK
        };

        for (int i = 0; i < actions.length; i++) {
            int row = i / 2;
            int col = i % 2;
            int buttonWidth = (contentWidth - 20) / 2;
            int x = contentX + col * (buttonWidth + 20);
            int y = startY + row * spacing;

            KeyBindButton kbb = new KeyBindButton(x, y, buttonWidth, buttonHeight, actions[i]);
            keyBindButtons.add(kbb);
        }
    }

    private void initControllerBindings(int contentX, int contentWidth) {
        controllerBindButtons = new ArrayList<>();
        int startY = panelY + 145;
        int buttonHeight = 32;
        int spacing = 38;

        String[] buttons = ControllerBindings.getAllButtons();

        for (int i = 0; i < buttons.length; i++) {
            int row = i / 2;
            int col = i % 2;
            int buttonWidth = (contentWidth - 20) / 2;
            int x = contentX + col * (buttonWidth + 20);
            int y = startY + row * spacing;

            ControllerBindButton cbb = new ControllerBindButton(x, y, buttonWidth, buttonHeight, buttons[i]);
            controllerBindButtons.add(cbb);
        }
    }

    private String getControlsToggleText() {
        return showingControllerBindings ? "Showing: Controller Bindings (click to switch)" : "Showing: Keyboard Bindings (click to switch)";
    }

    private void updateControlsToggleColors() {
        if (showingControllerBindings) {
            controlsToggleButton.setColors(BUTTON_CONTROLLER, BUTTON_CONTROLLER_HOVER, Color.WHITE);
        } else {
            controlsToggleButton.setColors(BUTTON_REBIND, BUTTON_HOVER, Color.WHITE);
        }
    }

    private void initGameTab(int contentX, int contentWidth) {
        int startY = panelY + 120;
        int buttonHeight = 50;
        int spacing = 65;

        // Vibration Toggle
        vibrationToggle = new UIButton(
            contentX, startY, contentWidth, buttonHeight,
            getVibrationButtonText(), this::toggleVibration
        );
        updateVibrationButtonColors();

        // Day/Night Toggle
        dayNightToggle = new UIButton(
            contentX, startY + spacing, contentWidth, buttonHeight,
            "Toggle Day/Night", () -> {
                nightMode = !nightMode;
                if (onDayNightToggle != null) {
                    onDayNightToggle.run();
                }
            }
        );
        dayNightToggle.setColors(
            new Color(100, 50, 150, 200),
            new Color(140, 80, 200, 230),
            Color.WHITE
        );

        // Debug Toggle
        debugToggle = new UIButton(
            contentX, startY + spacing * 2, contentWidth, buttonHeight,
            "Toggle Debug Mode (F3)", () -> {
                debugMode = !debugMode;
                if (onDebugToggle != null) {
                    onDebugToggle.run();
                }
            }
        );
        debugToggle.setColors(
            new Color(80, 80, 80, 200),
            new Color(120, 120, 120, 230),
            Color.WHITE
        );
    }

    private void initActionsTab(int contentX, int contentWidth) {
        int startY = panelY + 120;
        int buttonHeight = 55;
        int spacing = 70;

        // Return to Menu
        returnToMenuButton = new UIButton(
            contentX, startY, contentWidth, buttonHeight,
            "Return to Main Menu", () -> {
                hide();
                SceneManager.getInstance().setScene("mainMenu", SceneManager.TRANSITION_FADE);
            }
        );
        returnToMenuButton.setColors(
            new Color(50, 150, 50, 200),
            new Color(80, 200, 80, 230),
            Color.WHITE
        );

        // Customize Character
        customizeButton = new UIButton(
            contentX, startY + spacing, contentWidth, buttonHeight,
            "Customize Character", () -> {
                hide();
                if (onCustomize != null) {
                    onCustomize.run();
                } else {
                    SceneManager.getInstance().setScene("spriteCustomization", SceneManager.TRANSITION_FADE);
                }
            }
        );
        customizeButton.setColors(
            new Color(180, 120, 50, 200),
            new Color(220, 160, 80, 230),
            Color.WHITE
        );

        // Toggle Music (quick access)
        musicToggleButton = new UIButton(
            contentX, startY + spacing * 2, contentWidth, buttonHeight,
            "Toggle Music", () -> {
                if (audioManager != null) {
                    audioManager.toggleMusic();
                }
            }
        );
        musicToggleButton.setColors(
            new Color(50, 100, 200, 200),
            new Color(80, 130, 255, 230),
            Color.WHITE
        );

        // Exit Game
        exitGameButton = new UIButton(
            contentX, startY + spacing * 3, contentWidth, buttonHeight,
            "Exit Game", () -> {
                if (audioManager != null) {
                    audioManager.dispose();
                }
                System.exit(0);
            }
        );
        exitGameButton.setColors(BUTTON_DANGER, BUTTON_DANGER_HOVER, Color.WHITE);
    }

    private String getMuteButtonText() {
        return (audioManager != null && audioManager.isMuted()) ? "Unmute All" : "Mute All";
    }

    private void updateMuteButtonColors() {
        if (audioManager != null && audioManager.isMuted()) {
            muteButton.setColors(BUTTON_ACTIVE, BUTTON_ACTIVE_HOVER, Color.WHITE);
        } else {
            muteButton.setColors(BUTTON_NORMAL, BUTTON_HOVER, Color.WHITE);
        }
    }

    private void toggleMute() {
        if (audioManager != null) {
            audioManager.setMuteAll(!audioManager.isMuted());
        }
        // Reinitialize UI to update button text and colors
        initAudioTab(panelX + 40, panelWidth - 80);
    }

    private String getVibrationButtonText() {
        ControllerManager cm = ControllerManager.getInstance();
        if (!cm.isControllerConnected()) {
            return "Vibration: No Controller";
        }
        return cm.isVibrationEnabled() ? "Vibration: ON" : "Vibration: OFF";
    }

    private void updateVibrationButtonColors() {
        ControllerManager cm = ControllerManager.getInstance();
        if (!cm.isControllerConnected()) {
            vibrationToggle.setColors(
                new Color(60, 60, 70),
                new Color(70, 70, 80),
                new Color(150, 150, 160)
            );
        } else if (cm.isVibrationEnabled()) {
            vibrationToggle.setColors(BUTTON_ACTIVE, BUTTON_ACTIVE_HOVER, Color.WHITE);
        } else {
            vibrationToggle.setColors(BUTTON_NORMAL, BUTTON_HOVER, Color.WHITE);
        }
    }

    private void toggleVibration() {
        ControllerManager cm = ControllerManager.getInstance();
        if (cm.isControllerConnected()) {
            cm.setVibrationEnabled(!cm.isVibrationEnabled());
            // Give feedback vibration when enabling
            if (cm.isVibrationEnabled() && cm.isVibrationSupported()) {
                cm.vibrate(0.5f, 150);
            }
        }
        initGameTab(panelX + 40, panelWidth - 80);
    }

    // Setters for external callbacks
    public void setOnDayNightToggle(Runnable callback) {
        this.onDayNightToggle = callback;
    }

    public void setOnDebugToggle(Runnable callback) {
        this.onDebugToggle = callback;
    }

    public void setOnCustomize(Runnable callback) {
        this.onCustomize = callback;
    }

    public void setNightMode(boolean nightMode) {
        this.nightMode = nightMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public void show() {
        visible = true;
        // Refresh slider values from audio manager
        if (audioManager != null) {
            musicVolumeSlider.setValue(audioManager.getMusicVolume());
            sfxVolumeSlider.setValue(audioManager.getSFXVolume());
        }
        // Refresh button states
        initAudioTab(panelX + 40, panelWidth - 80);
        initGameTab(panelX + 40, panelWidth - 80);
        // Refresh key bindings
        for (KeyBindButton kbb : keyBindButtons) {
            kbb.updateText();
        }
        // Refresh controller bindings
        for (ControllerBindButton cbb : controllerBindButtons) {
            cbb.updateText();
        }
    }

    public void hide() {
        visible = false;
        waitingForKeyInput = false;
        waitingForControllerInput = false;
        currentlyRebindingKey = null;
        currentlyRebindingController = null;
    }

    public void toggle() {
        if (visible) {
            hide();
        } else {
            show();
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean containsPoint(int x, int y) {
        return visible && x >= panelX && x <= panelX + panelWidth &&
               y >= panelY && y <= panelY + panelHeight;
    }

    /**
     * Update method called each frame to check for controller input during rebinding.
     */
    public void update() {
        if (!visible || !waitingForControllerInput || currentlyRebindingController == null) {
            return;
        }

        ControllerManager cm = ControllerManager.getInstance();
        if (!cm.isControllerConnected()) {
            return;
        }

        // Check for any button press
        String pressedButton = getAnyPressedControllerButton(cm);
        if (pressedButton != null) {
            // Set the new binding
            ControllerBindings cb = ControllerBindings.getInstance();
            String currentAction = cb.getAction(currentlyRebindingController.button);
            cb.setBinding(pressedButton, currentAction);

            // Refresh all buttons in case of swap
            for (ControllerBindButton cbb : controllerBindButtons) {
                cbb.updateText();
            }

            waitingForControllerInput = false;
            currentlyRebindingController = null;

            // Vibration feedback
            if (cm.isVibrationSupported() && cm.isVibrationEnabled()) {
                cm.vibrate(0.3f, 100);
            }
        }
    }

    private String getAnyPressedControllerButton(ControllerManager cm) {
        if (cm.isButtonAJustPressed()) return ControllerBindings.BUTTON_A;
        if (cm.isButtonXJustPressed()) return ControllerBindings.BUTTON_X;
        if (cm.isButtonYJustPressed()) return ControllerBindings.BUTTON_Y;
        if (cm.isButtonBackJustPressed()) return ControllerBindings.BUTTON_BACK;
        if (cm.isButtonStartJustPressed()) return ControllerBindings.BUTTON_START;
        if (cm.isButtonLBJustPressed()) return ControllerBindings.BUTTON_LB;
        if (cm.isButtonRBJustPressed()) return ControllerBindings.BUTTON_RB;
        if (cm.isLeftStickJustClicked()) return ControllerBindings.BUTTON_L3;
        if (cm.isRightTriggerJustPressed()) return ControllerBindings.RIGHT_TRIGGER;
        // B button check
        if (cm.isButtonBPressed() && !wasButtonBPressed) {
            wasButtonBPressed = true;
            return ControllerBindings.BUTTON_B;
        }
        if (!cm.isButtonBPressed()) {
            wasButtonBPressed = false;
        }
        return null;
    }

    private boolean wasButtonBPressed = false;

    public boolean handleMousePressed(int x, int y) {
        if (!visible) return false;

        // Handle tab clicks
        for (int i = 0; i < tabRects.length; i++) {
            if (tabRects[i].contains(x, y)) {
                currentTab = i;
                waitingForKeyInput = false;
                waitingForControllerInput = false;
                currentlyRebindingKey = null;
                currentlyRebindingController = null;
                return true;
            }
        }

        // Handle sliders (Audio tab)
        if (currentTab == 0) {
            musicVolumeSlider.handleMousePressed(x, y);
            sfxVolumeSlider.handleMousePressed(x, y);
        }

        // Handle Controls tab
        if (currentTab == 1) {
            // Handle toggle button
            if (controlsToggleButton.handleClick(x, y)) {
                return true;
            }

            if (!showingControllerBindings && !waitingForKeyInput) {
                // Keyboard bindings
                for (KeyBindButton kbb : keyBindButtons) {
                    if (kbb.contains(x, y)) {
                        currentlyRebindingKey = kbb;
                        waitingForKeyInput = true;
                        return true;
                    }
                }
            } else if (showingControllerBindings && !waitingForControllerInput) {
                // Controller bindings
                for (ControllerBindButton cbb : controllerBindButtons) {
                    if (cbb.contains(x, y)) {
                        currentlyRebindingController = cbb;
                        waitingForControllerInput = true;
                        return true;
                    }
                }
            }
        }

        return true;
    }

    public void handleMouseReleased(int x, int y) {
        if (!visible) return;

        if (currentTab == 0) {
            musicVolumeSlider.handleMouseReleased(x, y);
            sfxVolumeSlider.handleMouseReleased(x, y);
        }
    }

    public void handleMouseDragged(int x, int y) {
        if (!visible) return;

        if (currentTab == 0) {
            musicVolumeSlider.handleMouseDragged(x, y);
            sfxVolumeSlider.handleMouseDragged(x, y);
        }
    }

    public void handleMouseMoved(int x, int y) {
        if (!visible) return;

        closeButton.handleMouseMove(x, y);

        if (currentTab == 0) {
            muteButton.handleMouseMove(x, y);
        } else if (currentTab == 1) {
            controlsToggleButton.handleMouseMove(x, y);
            resetControlsButton.handleMouseMove(x, y);
            if (!showingControllerBindings) {
                for (KeyBindButton kbb : keyBindButtons) {
                    kbb.handleMouseMove(x, y);
                }
            } else {
                for (ControllerBindButton cbb : controllerBindButtons) {
                    cbb.handleMouseMove(x, y);
                }
            }
        } else if (currentTab == 2) {
            vibrationToggle.handleMouseMove(x, y);
            dayNightToggle.handleMouseMove(x, y);
            debugToggle.handleMouseMove(x, y);
        } else if (currentTab == 3) {
            returnToMenuButton.handleMouseMove(x, y);
            customizeButton.handleMouseMove(x, y);
            musicToggleButton.handleMouseMove(x, y);
            exitGameButton.handleMouseMove(x, y);
        }
    }

    public boolean handleMouseClicked(int x, int y) {
        if (!visible) return false;

        if (closeButton.handleClick(x, y)) {
            return true;
        }

        if (currentTab == 0) {
            if (muteButton.handleClick(x, y)) {
                return true;
            }
        } else if (currentTab == 1) {
            if (resetControlsButton.handleClick(x, y)) {
                return true;
            }
            // Key bind buttons are handled in handleMousePressed
        } else if (currentTab == 2) {
            if (vibrationToggle.handleClick(x, y)) return true;
            if (dayNightToggle.handleClick(x, y)) return true;
            if (debugToggle.handleClick(x, y)) return true;
        } else if (currentTab == 3) {
            if (returnToMenuButton.handleClick(x, y)) return true;
            if (customizeButton.handleClick(x, y)) return true;
            if (musicToggleButton.handleClick(x, y)) return true;
            if (exitGameButton.handleClick(x, y)) return true;
        }

        return containsPoint(x, y);
    }

    public boolean handleKeyPressed(int keyCode) {
        if (!visible) return false;

        // Handle key rebinding
        if (waitingForKeyInput && currentlyRebindingKey != null) {
            // Escape cancels rebinding
            if (keyCode == KeyEvent.VK_ESCAPE) {
                waitingForKeyInput = false;
                currentlyRebindingKey = null;
                return true;
            }

            // Set the new binding
            KeyBindings.getInstance().setKey(currentlyRebindingKey.action, keyCode);
            currentlyRebindingKey.updateText();
            waitingForKeyInput = false;
            currentlyRebindingKey = null;

            // Refresh all buttons in case of swap
            for (KeyBindButton kbb : keyBindButtons) {
                kbb.updateText();
            }
            return true;
        }

        // Cancel controller rebinding with Escape
        if (waitingForControllerInput && keyCode == KeyEvent.VK_ESCAPE) {
            waitingForControllerInput = false;
            currentlyRebindingController = null;
            return true;
        }

        // M or Escape closes the menu (when not rebinding)
        if (keyCode == KeyEvent.VK_M || keyCode == KeyEvent.VK_ESCAPE) {
            hide();
            return true;
        }

        return false;
    }

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
        g2d.drawString(title, titleX, panelY + 35);

        // Draw tabs
        drawTabs(g2d);

        // Draw current tab content
        switch (currentTab) {
            case 0: drawAudioTab(g2d); break;
            case 1: drawControlsTab(g2d); break;
            case 2: drawGameTab(g2d); break;
            case 3: drawActionsTab(g2d); break;
        }

        // Draw close button
        closeButton.draw(g);

        // Draw hint text at bottom
        g2d.setColor(new Color(150, 150, 160));
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        String hint;
        if (waitingForKeyInput) {
            hint = "Press any key to bind (ESC to cancel)";
        } else if (waitingForControllerInput) {
            hint = "Press any controller button to bind (ESC to cancel)";
        } else {
            hint = "Press M or ESC to close";
        }
        fm = g2d.getFontMetrics();
        int hintX = panelX + (panelWidth - fm.stringWidth(hint)) / 2;
        g2d.drawString(hint, hintX, panelY + panelHeight - 15);
    }

    private void drawTabs(Graphics2D g2d) {
        g2d.setFont(new Font("Arial", Font.BOLD, 14));

        for (int i = 0; i < TAB_NAMES.length; i++) {
            Rectangle rect = tabRects[i];

            // Tab background
            if (i == currentTab) {
                g2d.setColor(TAB_ACTIVE);
            } else if (rect.contains(
                    SceneManager.getInstance().getInputManager().getMouseX(),
                    SceneManager.getInstance().getInputManager().getMouseY())) {
                g2d.setColor(TAB_HOVER);
            } else {
                g2d.setColor(TAB_INACTIVE);
            }
            g2d.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 8, 8);

            // Tab border
            g2d.setColor(i == currentTab ? new Color(120, 120, 150) : new Color(80, 80, 100));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 8, 8);

            // Tab text
            g2d.setColor(i == currentTab ? Color.WHITE : new Color(180, 180, 190));
            FontMetrics fm = g2d.getFontMetrics();
            int textX = rect.x + (rect.width - fm.stringWidth(TAB_NAMES[i])) / 2;
            int textY = rect.y + ((rect.height - fm.getHeight()) / 2) + fm.getAscent();
            g2d.drawString(TAB_NAMES[i], textX, textY);
        }

        // Draw line under tabs
        g2d.setColor(PANEL_BORDER);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawLine(panelX + 20, panelY + 95, panelX + panelWidth - 20, panelY + 95);
    }

    private void drawAudioTab(Graphics2D g2d) {
        musicVolumeSlider.draw(g2d);
        sfxVolumeSlider.draw(g2d);
        muteButton.draw(g2d);

        // Draw audio info
        g2d.setColor(new Color(150, 150, 160));
        g2d.setFont(new Font("Arial", Font.ITALIC, 12));
        String info = "Adjust volume levels for music and sound effects";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(info, panelX + (panelWidth - fm.stringWidth(info)) / 2, panelY + 240);
    }

    private void drawControlsTab(Graphics2D g2d) {
        // Draw toggle button
        controlsToggleButton.draw(g2d);

        if (!showingControllerBindings) {
            // Draw keyboard bindings
            for (KeyBindButton kbb : keyBindButtons) {
                kbb.draw(g2d, kbb == currentlyRebindingKey && waitingForKeyInput);
            }
        } else {
            // Draw controller bindings
            for (ControllerBindButton cbb : controllerBindButtons) {
                cbb.draw(g2d, cbb == currentlyRebindingController && waitingForControllerInput);
            }
        }

        // Draw reset button
        resetControlsButton.draw(g2d);

        // Draw controller info
        ControllerManager cm = ControllerManager.getInstance();
        g2d.setColor(new Color(150, 150, 160));
        g2d.setFont(new Font("Arial", Font.ITALIC, 11));
        String controllerInfo = cm.isControllerConnected()
            ? "Controller: " + cm.getControllerName()
            : "No controller connected. Plug in Xbox controller for gamepad support.";
        g2d.drawString(controllerInfo, panelX + 40, panelY + panelHeight - 55);
    }

    private void drawGameTab(Graphics2D g2d) {
        vibrationToggle.draw(g2d);
        dayNightToggle.draw(g2d);
        debugToggle.draw(g2d);

        // Draw game options info
        g2d.setColor(new Color(150, 150, 160));
        g2d.setFont(new Font("Arial", Font.ITALIC, 12));
        String info = "Configure gameplay and visual options";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(info, panelX + (panelWidth - fm.stringWidth(info)) / 2, panelY + 340);
    }

    private void drawActionsTab(Graphics2D g2d) {
        returnToMenuButton.draw(g2d);
        customizeButton.draw(g2d);
        musicToggleButton.draw(g2d);
        exitGameButton.draw(g2d);

        // Draw quick actions info
        g2d.setColor(new Color(150, 150, 160));
        g2d.setFont(new Font("Arial", Font.ITALIC, 12));
        String info = "Quick access to common game actions";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(info, panelX + (panelWidth - fm.stringWidth(info)) / 2, panelY + panelHeight - 50);
    }

    /**
     * Inner class for keyboard key binding buttons.
     */
    private class KeyBindButton {
        int x, y, width, height;
        String action;
        String displayText;
        boolean hovered = false;

        KeyBindButton(int x, int y, int width, int height, String action) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.action = action;
            updateText();
        }

        void updateText() {
            KeyBindings kb = KeyBindings.getInstance();
            String actionName = KeyBindings.getActionDisplayName(action);
            String keyName = KeyBindings.getKeyName(kb.getKey(action));
            this.displayText = actionName + ": [" + keyName + "]";
        }

        boolean contains(int mx, int my) {
            return mx >= x && mx <= x + width && my >= y && my <= y + height;
        }

        void handleMouseMove(int mx, int my) {
            hovered = contains(mx, my);
        }

        void draw(Graphics2D g2d, boolean isRebinding) {
            // Background
            if (isRebinding) {
                g2d.setColor(BUTTON_REBIND_ACTIVE);
            } else if (hovered) {
                g2d.setColor(BUTTON_HOVER);
            } else {
                g2d.setColor(BUTTON_REBIND);
            }
            g2d.fillRoundRect(x, y, width, height, 8, 8);

            // Border
            g2d.setColor(isRebinding ? new Color(255, 200, 100) : new Color(90, 90, 110));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(x, y, width, height, 8, 8);

            // Text
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            FontMetrics fm = g2d.getFontMetrics();
            String text = isRebinding ? "Press a key..." : displayText;
            int textX = x + (width - fm.stringWidth(text)) / 2;
            int textY = y + ((height - fm.getHeight()) / 2) + fm.getAscent();
            g2d.drawString(text, textX, textY);
        }
    }

    /**
     * Inner class for controller binding buttons.
     */
    private class ControllerBindButton {
        int x, y, width, height;
        String button;  // The controller button (e.g., BUTTON_A)
        String displayText;
        boolean hovered = false;

        ControllerBindButton(int x, int y, int width, int height, String button) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.button = button;
            updateText();
        }

        void updateText() {
            ControllerBindings cb = ControllerBindings.getInstance();
            String buttonName = ControllerBindings.getButtonShortName(button);
            String action = cb.getAction(button);
            String actionName = action != null ? ControllerBindings.getActionDisplayName(action) : "Unbound";
            this.displayText = buttonName + ": " + actionName;
        }

        boolean contains(int mx, int my) {
            return mx >= x && mx <= x + width && my >= y && my <= y + height;
        }

        void handleMouseMove(int mx, int my) {
            hovered = contains(mx, my);
        }

        void draw(Graphics2D g2d, boolean isRebinding) {
            // Background
            if (isRebinding) {
                g2d.setColor(BUTTON_REBIND_ACTIVE);
            } else if (hovered) {
                g2d.setColor(BUTTON_CONTROLLER_HOVER);
            } else {
                g2d.setColor(BUTTON_CONTROLLER);
            }
            g2d.fillRoundRect(x, y, width, height, 8, 8);

            // Border
            g2d.setColor(isRebinding ? new Color(255, 200, 100) : new Color(80, 150, 100));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(x, y, width, height, 8, 8);

            // Text
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            FontMetrics fm = g2d.getFontMetrics();
            String text = isRebinding ? "Press button..." : displayText;
            int textX = x + (width - fm.stringWidth(text)) / 2;
            int textY = y + ((height - fm.getHeight()) / 2) + fm.getAscent();
            g2d.drawString(text, textX, textY);
        }
    }
}
