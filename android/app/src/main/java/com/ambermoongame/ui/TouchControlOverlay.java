package com.ambermoongame.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.ambermoongame.core.GamePreferences;
import com.ambermoongame.input.TouchInputManager;

/**
 * Virtual touch control overlay for the Android port.
 * Provides on-screen controls that map to desktop keyboard actions.
 *
 * Layout:
 * - Left side: D-Pad for movement (WASD)
 * - Bottom left: Sprint button (Shift)
 * - Bottom right: Jump, Attack, Interact buttons
 * - Top corners: Menu and Inventory buttons
 */
public class TouchControlOverlay extends View {

    // Touch input manager reference
    private TouchInputManager inputManager;

    // Control dimensions (scaled to screen)
    private float scale = 1.0f;
    private int screenWidth = 1920;
    private int screenHeight = 1080;

    // D-Pad
    private float dpadCenterX;
    private float dpadCenterY;
    private float dpadRadius = 120;
    private float dpadInnerRadius = 40;
    private int dpadTouchId = -1;
    private float dpadTouchAngle = 0;
    private float dpadTouchDistance = 0;

    // Action buttons
    private RectF jumpButton;
    private RectF attackButton;
    private RectF interactButton;
    private RectF sprintButton;
    private RectF menuButton;
    private RectF inventoryButton;

    // Button touch tracking
    private int jumpTouchId = -1;
    private int attackTouchId = -1;
    private int interactTouchId = -1;
    private int sprintTouchId = -1;
    private int menuTouchId = -1;
    private int inventoryTouchId = -1;

    // Paints
    private Paint dpadPaint;
    private Paint dpadActivePaint;
    private Paint buttonPaint;
    private Paint buttonActivePaint;
    private Paint buttonTextPaint;
    private Paint outlinePaint;

    // Opacity
    private int opacity = 128; // 0-255

    public TouchControlOverlay(Context context, TouchInputManager inputManager) {
        super(context);
        this.inputManager = inputManager;
        init();
    }

    public TouchControlOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setWillNotDraw(false);

        // Load opacity from preferences
        opacity = (int) (GamePreferences.getTouchOpacity() * 255);

        // Initialize paints
        dpadPaint = new Paint();
        dpadPaint.setColor(Color.argb(opacity, 50, 50, 50));
        dpadPaint.setAntiAlias(true);

        dpadActivePaint = new Paint();
        dpadActivePaint.setColor(Color.argb(opacity + 50, 100, 100, 150));
        dpadActivePaint.setAntiAlias(true);

        buttonPaint = new Paint();
        buttonPaint.setColor(Color.argb(opacity, 60, 60, 80));
        buttonPaint.setAntiAlias(true);

        buttonActivePaint = new Paint();
        buttonActivePaint.setColor(Color.argb(opacity + 50, 80, 80, 120));
        buttonActivePaint.setAntiAlias(true);

        buttonTextPaint = new Paint();
        buttonTextPaint.setColor(Color.argb(200, 255, 255, 255));
        buttonTextPaint.setTextSize(28);
        buttonTextPaint.setTextAlign(Paint.Align.CENTER);
        buttonTextPaint.setAntiAlias(true);

        outlinePaint = new Paint();
        outlinePaint.setColor(Color.argb(opacity, 100, 100, 120));
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(3);
        outlinePaint.setAntiAlias(true);

        // Initialize button rects (will be updated in onSizeChanged)
        jumpButton = new RectF();
        attackButton = new RectF();
        interactButton = new RectF();
        sprintButton = new RectF();
        menuButton = new RectF();
        inventoryButton = new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        screenWidth = w;
        screenHeight = h;
        scale = Math.min(w / 1920f, h / 1080f);

        // Scale dimensions
        dpadRadius = 100 * scale;
        dpadInnerRadius = 35 * scale;

        float buttonSize = 80 * scale;
        float smallButtonSize = 60 * scale;
        float margin = 30 * scale;

        // D-Pad position (bottom left)
        dpadCenterX = margin + dpadRadius + 20 * scale;
        dpadCenterY = h - margin - dpadRadius - 20 * scale;

        // Action buttons (bottom right)
        float rightX = w - margin;
        float bottomY = h - margin;

        // Jump button (large, lower right)
        jumpButton.set(
            rightX - buttonSize * 2 - 20 * scale,
            bottomY - buttonSize,
            rightX - buttonSize - 20 * scale,
            bottomY
        );

        // Attack button (right of jump)
        attackButton.set(
            rightX - buttonSize,
            bottomY - buttonSize * 1.5f,
            rightX,
            bottomY - buttonSize * 0.5f
        );

        // Interact button (above jump)
        interactButton.set(
            rightX - buttonSize * 2 - 20 * scale,
            bottomY - buttonSize * 2 - 15 * scale,
            rightX - buttonSize - 20 * scale,
            bottomY - buttonSize - 15 * scale
        );

        // Sprint button (below d-pad)
        sprintButton.set(
            margin,
            bottomY - smallButtonSize,
            margin + smallButtonSize * 1.5f,
            bottomY
        );

        // Menu button (top left)
        menuButton.set(
            margin,
            margin,
            margin + smallButtonSize * 1.5f,
            margin + smallButtonSize
        );

        // Inventory button (top right)
        inventoryButton.set(
            rightX - smallButtonSize * 1.5f,
            margin,
            rightX,
            margin + smallButtonSize
        );

        // Update text size based on scale
        buttonTextPaint.setTextSize(24 * scale);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!GamePreferences.isShowTouchControls()) {
            return;
        }

        // Draw D-Pad
        drawDPad(canvas);

        // Draw action buttons
        drawButton(canvas, jumpButton, "JUMP", jumpTouchId >= 0);
        drawButton(canvas, attackButton, "ATK", attackTouchId >= 0);
        drawButton(canvas, interactButton, "E", interactTouchId >= 0);
        drawButton(canvas, sprintButton, "RUN", sprintTouchId >= 0);
        drawButton(canvas, menuButton, "MENU", menuTouchId >= 0);
        drawButton(canvas, inventoryButton, "INV", inventoryTouchId >= 0);
    }

    private void drawDPad(Canvas canvas) {
        // Background circle
        canvas.drawCircle(dpadCenterX, dpadCenterY, dpadRadius, dpadPaint);
        canvas.drawCircle(dpadCenterX, dpadCenterY, dpadRadius, outlinePaint);

        // Center circle
        canvas.drawCircle(dpadCenterX, dpadCenterY, dpadInnerRadius, buttonPaint);

        // Direction indicators
        if (dpadTouchId >= 0 && dpadTouchDistance > dpadInnerRadius) {
            float indicatorX = dpadCenterX + (float) Math.cos(dpadTouchAngle) * dpadRadius * 0.6f;
            float indicatorY = dpadCenterY + (float) Math.sin(dpadTouchAngle) * dpadRadius * 0.6f;
            canvas.drawCircle(indicatorX, indicatorY, dpadInnerRadius * 0.8f, dpadActivePaint);
        }

        // Draw direction arrows/labels
        float arrowOffset = dpadRadius * 0.7f;
        Paint arrowPaint = new Paint(buttonTextPaint);
        arrowPaint.setTextSize(20 * scale);

        canvas.drawText("W", dpadCenterX, dpadCenterY - arrowOffset + 8 * scale, arrowPaint);
        canvas.drawText("S", dpadCenterX, dpadCenterY + arrowOffset + 8 * scale, arrowPaint);
        canvas.drawText("A", dpadCenterX - arrowOffset, dpadCenterY + 8 * scale, arrowPaint);
        canvas.drawText("D", dpadCenterX + arrowOffset, dpadCenterY + 8 * scale, arrowPaint);
    }

    private void drawButton(Canvas canvas, RectF rect, String label, boolean pressed) {
        Paint paint = pressed ? buttonActivePaint : buttonPaint;
        canvas.drawRoundRect(rect, 15 * scale, 15 * scale, paint);
        canvas.drawRoundRect(rect, 15 * scale, 15 * scale, outlinePaint);

        float textY = rect.centerY() - (buttonTextPaint.descent() + buttonTextPaint.ascent()) / 2;
        canvas.drawText(label, rect.centerX(), textY, buttonTextPaint);
    }

    // ==================== Touch Handling ====================

    public boolean handleTouchDown(float x, float y, int pointerId) {
        // Check D-Pad
        float dpadDx = x - dpadCenterX;
        float dpadDy = y - dpadCenterY;
        float dpadDist = (float) Math.sqrt(dpadDx * dpadDx + dpadDy * dpadDy);

        if (dpadDist <= dpadRadius && dpadTouchId < 0) {
            dpadTouchId = pointerId;
            updateDPadState(x, y);
            invalidate();
            return true;
        }

        // Check buttons
        if (jumpButton.contains(x, y) && jumpTouchId < 0) {
            jumpTouchId = pointerId;
            inputManager.setJump(true);
            invalidate();
            return true;
        }

        if (attackButton.contains(x, y) && attackTouchId < 0) {
            attackTouchId = pointerId;
            inputManager.setAttack(true);
            invalidate();
            return true;
        }

        if (interactButton.contains(x, y) && interactTouchId < 0) {
            interactTouchId = pointerId;
            inputManager.setInteract(true);
            invalidate();
            return true;
        }

        if (sprintButton.contains(x, y) && sprintTouchId < 0) {
            sprintTouchId = pointerId;
            inputManager.setSprint(true);
            invalidate();
            return true;
        }

        if (menuButton.contains(x, y) && menuTouchId < 0) {
            menuTouchId = pointerId;
            inputManager.setMenu(true);
            invalidate();
            return true;
        }

        if (inventoryButton.contains(x, y) && inventoryTouchId < 0) {
            inventoryTouchId = pointerId;
            inputManager.setInventory(true);
            invalidate();
            return true;
        }

        return false;
    }

    public void handleTouchUp(float x, float y, int pointerId) {
        if (pointerId == dpadTouchId) {
            dpadTouchId = -1;
            inputManager.setMoveLeft(false);
            inputManager.setMoveRight(false);
            inputManager.setMoveUp(false);
            inputManager.setMoveDown(false);
            invalidate();
        }

        if (pointerId == jumpTouchId) {
            jumpTouchId = -1;
            inputManager.setJump(false);
            invalidate();
        }

        if (pointerId == attackTouchId) {
            attackTouchId = -1;
            inputManager.setAttack(false);
            invalidate();
        }

        if (pointerId == interactTouchId) {
            interactTouchId = -1;
            inputManager.setInteract(false);
            invalidate();
        }

        if (pointerId == sprintTouchId) {
            sprintTouchId = -1;
            inputManager.setSprint(false);
            invalidate();
        }

        if (pointerId == menuTouchId) {
            menuTouchId = -1;
            inputManager.setMenu(false);
            invalidate();
        }

        if (pointerId == inventoryTouchId) {
            inventoryTouchId = -1;
            inputManager.setInventory(false);
            invalidate();
        }
    }

    public void handleTouchMove(float x, float y, int pointerId) {
        if (pointerId == dpadTouchId) {
            updateDPadState(x, y);
            invalidate();
        }
    }

    private void updateDPadState(float x, float y) {
        float dx = x - dpadCenterX;
        float dy = y - dpadCenterY;
        dpadTouchDistance = (float) Math.sqrt(dx * dx + dy * dy);
        dpadTouchAngle = (float) Math.atan2(dy, dx);

        // Reset all directions
        inputManager.setMoveLeft(false);
        inputManager.setMoveRight(false);
        inputManager.setMoveUp(false);
        inputManager.setMoveDown(false);

        if (dpadTouchDistance > dpadInnerRadius) {
            // Determine direction (8-way)
            float angle = dpadTouchAngle;
            float threshold = (float) Math.PI / 8; // 22.5 degrees

            // Right: -22.5 to 22.5 degrees
            if (angle > -threshold && angle < threshold) {
                inputManager.setMoveRight(true);
            }
            // Down-Right: 22.5 to 67.5 degrees
            else if (angle >= threshold && angle < 3 * threshold) {
                inputManager.setMoveRight(true);
                inputManager.setMoveDown(true);
            }
            // Down: 67.5 to 112.5 degrees
            else if (angle >= 3 * threshold && angle < 5 * threshold) {
                inputManager.setMoveDown(true);
            }
            // Down-Left: 112.5 to 157.5 degrees
            else if (angle >= 5 * threshold && angle < 7 * threshold) {
                inputManager.setMoveLeft(true);
                inputManager.setMoveDown(true);
            }
            // Left: 157.5 to 180 and -180 to -157.5 degrees
            else if (angle >= 7 * threshold || angle < -7 * threshold) {
                inputManager.setMoveLeft(true);
            }
            // Up-Left: -157.5 to -112.5 degrees
            else if (angle >= -7 * threshold && angle < -5 * threshold) {
                inputManager.setMoveLeft(true);
                inputManager.setMoveUp(true);
            }
            // Up: -112.5 to -67.5 degrees
            else if (angle >= -5 * threshold && angle < -3 * threshold) {
                inputManager.setMoveUp(true);
            }
            // Up-Right: -67.5 to -22.5 degrees
            else if (angle >= -3 * threshold && angle < -threshold) {
                inputManager.setMoveRight(true);
                inputManager.setMoveUp(true);
            }
        }
    }

    // ==================== Settings ====================

    public void setOpacity(float opacity) {
        this.opacity = (int) (opacity * 255);
        updatePaintOpacity();
        invalidate();
    }

    private void updatePaintOpacity() {
        dpadPaint.setColor(Color.argb(opacity, 50, 50, 50));
        dpadActivePaint.setColor(Color.argb(Math.min(255, opacity + 50), 100, 100, 150));
        buttonPaint.setColor(Color.argb(opacity, 60, 60, 80));
        buttonActivePaint.setColor(Color.argb(Math.min(255, opacity + 50), 80, 80, 120));
        outlinePaint.setColor(Color.argb(opacity, 100, 100, 120));
    }
}
