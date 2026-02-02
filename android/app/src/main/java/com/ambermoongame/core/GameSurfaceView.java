package com.ambermoongame.core;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.ambermoongame.input.TouchInputManager;
import com.ambermoongame.input.AndroidControllerManager;
import com.ambermoongame.scene.AndroidSceneManager;

/**
 * Main game rendering surface that handles the game loop and drawing.
 *
 * Equivalent to GamePanel.java from the desktop version.
 * Uses a SurfaceView with a dedicated render thread for smooth 60 FPS gameplay.
 */
public class GameSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private static final String TAG = "GameSurfaceView";

    // Target FPS
    private static final int TARGET_FPS = 60;
    private static final long FRAME_TIME_NANOS = 1_000_000_000L / TARGET_FPS;

    // Target resolution (matches desktop)
    public static final int TARGET_WIDTH = 1920;
    public static final int TARGET_HEIGHT = 1080;

    // Game thread
    private Thread gameThread;
    private volatile boolean running = false;

    // Managers
    private AndroidSceneManager sceneManager;
    private TouchInputManager touchInputManager;
    private AndroidControllerManager controllerManager;

    // Scaling for different screen sizes
    private float scaleX = 1.0f;
    private float scaleY = 1.0f;
    private float offsetX = 0;
    private float offsetY = 0;
    private Matrix scaleMatrix;

    // Debug info
    private Paint debugPaint;
    private boolean showDebugInfo = false;
    private int currentFps = 0;
    private long lastFpsUpdate = 0;
    private int frameCount = 0;

    // Controller cursor paint
    private Paint cursorPaint;
    private Paint cursorOutlinePaint;

    public GameSurfaceView(Context context, AndroidSceneManager sceneManager,
                           TouchInputManager touchInputManager,
                           AndroidControllerManager controllerManager) {
        super(context);

        this.sceneManager = sceneManager;
        this.touchInputManager = touchInputManager;
        this.controllerManager = controllerManager;

        // Set up surface holder
        getHolder().addCallback(this);

        // Initialize paints
        initPaints();

        // Initialize scale matrix
        scaleMatrix = new Matrix();

        // Make focusable for key events
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    private void initPaints() {
        // Debug text paint
        debugPaint = new Paint();
        debugPaint.setColor(Color.WHITE);
        debugPaint.setTextSize(24);
        debugPaint.setAntiAlias(true);

        // Controller cursor paints
        cursorPaint = new Paint();
        cursorPaint.setColor(Color.argb(255, 255, 200, 50)); // Golden yellow
        cursorPaint.setAntiAlias(true);
        cursorPaint.setStyle(Paint.Style.FILL);

        cursorOutlinePaint = new Paint();
        cursorOutlinePaint.setColor(Color.WHITE);
        cursorOutlinePaint.setAntiAlias(true);
        cursorOutlinePaint.setStyle(Paint.Style.STROKE);
        cursorOutlinePaint.setStrokeWidth(3);
    }

    // ==================== Surface Callbacks ====================

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "Surface created");
        calculateScaling();
        startGameLoop();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "Surface changed: " + width + "x" + height);
        calculateScaling();

        // Update touch input manager with new screen dimensions and scaling
        touchInputManager.setScreenDimensions(width, height, scaleX, scaleY, offsetX, offsetY);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "Surface destroyed");
        stopGameLoop();
    }

    /**
     * Calculate scaling to fit target resolution to screen.
     * Uses letterboxing to maintain aspect ratio.
     */
    private void calculateScaling() {
        int screenWidth = getWidth();
        int screenHeight = getHeight();

        if (screenWidth == 0 || screenHeight == 0) return;

        float targetRatio = (float) TARGET_WIDTH / TARGET_HEIGHT;
        float screenRatio = (float) screenWidth / screenHeight;

        if (screenRatio > targetRatio) {
            // Screen is wider - letterbox on sides
            scaleY = (float) screenHeight / TARGET_HEIGHT;
            scaleX = scaleY;
            offsetX = (screenWidth - TARGET_WIDTH * scaleX) / 2;
            offsetY = 0;
        } else {
            // Screen is taller - letterbox on top/bottom
            scaleX = (float) screenWidth / TARGET_WIDTH;
            scaleY = scaleX;
            offsetX = 0;
            offsetY = (screenHeight - TARGET_HEIGHT * scaleY) / 2;
        }

        scaleMatrix.reset();
        scaleMatrix.postTranslate(offsetX, offsetY);
        scaleMatrix.preScale(scaleX, scaleY);

        Log.d(TAG, "Scaling calculated: scale=" + scaleX + ", offset=(" + offsetX + "," + offsetY + ")");
    }

    // ==================== Game Loop ====================

    private void startGameLoop() {
        if (gameThread != null && running) return;

        running = true;
        gameThread = new Thread(this, "GameThread");
        gameThread.start();
        Log.d(TAG, "Game loop started");
    }

    private void stopGameLoop() {
        running = false;
        if (gameThread != null) {
            try {
                gameThread.join(1000);
            } catch (InterruptedException e) {
                Log.e(TAG, "Interrupted while stopping game thread", e);
            }
            gameThread = null;
        }
        Log.d(TAG, "Game loop stopped");
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double delta = 0;

        while (running) {
            long currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / (double) FRAME_TIME_NANOS;
            lastTime = currentTime;

            // Update and render when enough time has accumulated
            while (delta >= 1) {
                update();
                delta--;
            }

            render();

            // Update FPS counter
            updateFps();

            // Sleep to maintain frame rate
            long frameTime = System.nanoTime() - currentTime;
            long sleepTime = (FRAME_TIME_NANOS - frameTime) / 1_000_000;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /**
     * Update game logic.
     */
    private void update() {
        // Reset input consumption flags
        touchInputManager.resetFrame();

        // Poll controller state
        controllerManager.poll();

        // Update scene manager
        if (sceneManager != null) {
            sceneManager.update(touchInputManager);
        }
    }

    /**
     * Render the current frame.
     */
    private void render() {
        SurfaceHolder holder = getHolder();
        if (holder == null || !holder.getSurface().isValid()) return;

        Canvas canvas = null;
        try {
            canvas = holder.lockCanvas();
            if (canvas != null) {
                // Clear with black (for letterboxing)
                canvas.drawColor(Color.BLACK);

                // Apply scaling transformation
                canvas.save();
                canvas.concat(scaleMatrix);

                // Draw current scene
                if (sceneManager != null) {
                    sceneManager.draw(canvas);
                }

                // Draw controller cursor if using controller
                if (controllerManager.isUsingController()) {
                    drawControllerCursor(canvas);
                }

                canvas.restore();

                // Draw debug info (in screen space, not scaled)
                if (showDebugInfo) {
                    drawDebugInfo(canvas);
                }
            }
        } finally {
            if (canvas != null) {
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    /**
     * Draw controller cursor (matches desktop implementation).
     */
    private void drawControllerCursor(Canvas canvas) {
        float cursorX = controllerManager.getVirtualMouseX();
        float cursorY = controllerManager.getVirtualMouseY();

        int outerSize = 24;
        int innerSize = 8;
        int crosshairLength = 12;
        int crosshairGap = 6;

        // Outer circle (white with black outline)
        cursorOutlinePaint.setColor(Color.BLACK);
        cursorOutlinePaint.setStrokeWidth(3);
        canvas.drawCircle(cursorX, cursorY, outerSize / 2f, cursorOutlinePaint);

        cursorOutlinePaint.setColor(Color.WHITE);
        cursorOutlinePaint.setStrokeWidth(2);
        canvas.drawCircle(cursorX, cursorY, outerSize / 2f, cursorOutlinePaint);

        // Center dot
        canvas.drawCircle(cursorX, cursorY, innerSize / 2f, cursorPaint);

        // Crosshair lines
        cursorOutlinePaint.setColor(Color.WHITE);
        cursorOutlinePaint.setStrokeWidth(2);

        // Top line
        canvas.drawLine(cursorX, cursorY - crosshairGap - crosshairLength,
                       cursorX, cursorY - crosshairGap, cursorOutlinePaint);
        // Bottom line
        canvas.drawLine(cursorX, cursorY + crosshairGap,
                       cursorX, cursorY + crosshairGap + crosshairLength, cursorOutlinePaint);
        // Left line
        canvas.drawLine(cursorX - crosshairGap - crosshairLength, cursorY,
                       cursorX - crosshairGap, cursorY, cursorOutlinePaint);
        // Right line
        canvas.drawLine(cursorX + crosshairGap, cursorY,
                       cursorX + crosshairGap + crosshairLength, cursorY, cursorOutlinePaint);
    }

    /**
     * Draw debug information overlay.
     */
    private void drawDebugInfo(Canvas canvas) {
        int y = 30;
        int lineHeight = 30;

        canvas.drawText("FPS: " + currentFps, 10, y, debugPaint);
        y += lineHeight;

        canvas.drawText("Scale: " + String.format("%.2f", scaleX), 10, y, debugPaint);
        y += lineHeight;

        if (controllerManager.isControllerConnected()) {
            canvas.drawText("Controller: " + controllerManager.getControllerName(), 10, y, debugPaint);
            y += lineHeight;
        }

        canvas.drawText("Touch points: " + touchInputManager.getTouchCount(), 10, y, debugPaint);
    }

    private void updateFps() {
        frameCount++;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFpsUpdate >= 1000) {
            currentFps = frameCount;
            frameCount = 0;
            lastFpsUpdate = currentTime;
        }
    }

    // ==================== Touch Input ====================

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Forward touch events to input manager
        return touchInputManager.handleTouchEvent(event);
    }

    // ==================== Lifecycle ====================

    public void resume() {
        if (!running) {
            startGameLoop();
        }
    }

    public void pause() {
        stopGameLoop();
    }

    public void destroy() {
        stopGameLoop();
    }

    // ==================== Accessors ====================

    public void setShowDebugInfo(boolean show) {
        this.showDebugInfo = show;
    }

    public boolean isShowingDebugInfo() {
        return showDebugInfo;
    }

    public float getScaleX() {
        return scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    /**
     * Convert screen coordinates to game coordinates.
     */
    public float screenToGameX(float screenX) {
        return (screenX - offsetX) / scaleX;
    }

    public float screenToGameY(float screenY) {
        return (screenY - offsetY) / scaleY;
    }

    /**
     * Convert game coordinates to screen coordinates.
     */
    public float gameToScreenX(float gameX) {
        return gameX * scaleX + offsetX;
    }

    public float gameToScreenY(float gameY) {
        return gameY * scaleY + offsetY;
    }
}
