package com.wsu.towerdefense;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import androidx.annotation.NonNull;
import com.wsu.towerdefense.view.activity.PauseActivity;

/**
 * Base class for the game. Manages the {@link SurfaceView}, game loop {@link Thread}, and {@link
 * Canvas}. The game child class implements {@link #update(double)} and {@link #render(double,
 * Canvas, Paint)} and everything else is handled automatically.
 */
public abstract class AbstractGame extends SurfaceView implements Callback {

    /**
     * Interval between game state updates. e.g.
     * <ul>
     *   <li>1 / 60 = 16.6 ms between updates = 60 updates per second</li>
     *   <li>1 / 20 = 50 ms between updates = 20 updates per second</li>
     * </ul>
     * Limiting the number of updates per second has advantages such as reducing battery consumption.
     * Setting the frequency too slow can have side effects like incorrect/delayed physics.
     * <p>
     * See documentation for {@link #loop()} for more info.
     */
    private static final double TIMESTEP = 1.0 / 60;

    /**
     * Whether the game state should update. Set to false when app is minimized.
     */
    protected boolean running;
    private boolean isPaused = false;
    private boolean isDoubleSpeed = false;
    private int gameWidth;
    private int gameHeight;

    private final SurfaceHolder surfaceHolder;
    private Thread thread;
    private final Paint paint;

    public AbstractGame(Context context, int gameWidth, int gameHeight) {
        super(context);
        running = false;
        this.gameWidth = gameWidth;
        this.gameHeight = gameHeight;

        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        thread = null;
        paint = new Paint();

        setKeepScreenOn(true);
    }

    /**
     * Game loop that handles updating and rendering at correct intervals. It is <i>fixed</i> (as
     * opposed to <i>variable</i>) timestep, which means that there is a constant amount of time
     * ({@link #TIMESTEP}) between game updates.
     * <p>
     * Further reading:
     * <p>
     * <a href="https://gameprogrammingpatterns.com/game-loop.html">Game Loop · Game Programming
     * Patterns</a>
     * <p>
     * <a href="https://gafferongames.com/post/fix_your_timestep/">Fix Your Timestep!
     * | Gaffer On Games</a>
     */
    public void loop() {
        double timePrevious = (System.currentTimeMillis() / 1000.0);
        // represents accumulated (unprocessed) time
        double acc = 0;

        while (running) {

            double timeCurrent = (System.currentTimeMillis() / 1000.0);
            // time difference between previous & current loop
            double delta = timeCurrent - timePrevious;
            timePrevious = timeCurrent;

            acc += delta; // passed time accumulates in acc variable

            // when time passed is >= minimum time between updates (TIMESTEP), game is updated
            while (acc >= TIMESTEP) {
                if (!this.isPaused && this.isDoubleSpeed) {
                    update(TIMESTEP * 2);
                }
                else if(!this.isPaused){
                    update(TIMESTEP);
                }
                acc -= TIMESTEP; // one interval was processed, so subtract it
            }
            // keep updating the game until passed time < minimum

            // when done updating, render the game, unless the game is paused
            // remaining unprocessed time is used by render method to interpolate
            if (!this.isPaused || PauseActivity.rerender) {
                _render(acc / TIMESTEP);
                PauseActivity.rerender = false;
            }

        }
    }

    /**
     * Internal render method. Handles locking and unlocking the canvas so that child classes don't
     * have to.
     * <p>
     * See documentation for {@link #render(double, Canvas, Paint)} for more info.
     *
     * @param alpha represents amount of unprocessed time, normalized to 0.0 and 1.0; used to
     *              interpolate between game updates
     */
    private void _render(double alpha) {
        Canvas canvas = surfaceHolder.lockCanvas();
        if (canvas != null) {
            render(alpha * TIMESTEP, canvas, paint);
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    /**
     * Instantiates the game loop {@link #thread}.
     */
    private void startThread() {
        thread = new Thread(this::loop);
        thread.start();
    }

    /**
     * Stops the game loop {@link #thread}.
     */
    private void stopThread() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            Log.e(getContext().getString(R.string.logcatKey), Log.getStackTraceString(e));
        }
    }

    /**
     * Starts/resume the game and starts the game loop {@link #thread}.
     */
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        running = true;
        startThread();
    }

    /**
     * Stops/pauses the game and stops the game loop {@link #thread}.
     */
    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        running = false;
        stopThread();
        holder.getSurface().release();
    }

    /**
     * Keeps track of changes in display size. {@link #getGameWidth()} and {@link #getGameHeight()}
     * can be used by child classes.
     */
    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        gameWidth = width;
        gameHeight = height;
    }

    public int getGameWidth() {
        return gameWidth;
    }

    public int getGameHeight() {
        return gameHeight;
    }

    /**
     * Called by game {@link #loop()} at a fixed interval. Update the game state from this method.
     * <p>
     * <b>When an object in the game changes over time (e.g. position), multiply the change by
     * <code>delta</code> so that it stays consistent regardless of game loop updates.</b>
     * e.g.
     * <pre>
     * {@code
     * object1.position.x += object1.velocity.x * delta;
     * object2.hue += 10 * delta;
     * }
     * </pre>
     * instead of
     * <pre>
     * {@code
     * object1.position.x += object1.velocity.x;
     * object2.hue += 10;
     * }
     * </pre>
     *
     * @param delta amount of time that has passed between updates
     */
    protected abstract void update(double delta);

    /**
     * Called by game {@link #loop()} when the game should be drawn. Draw game objects from this
     * method. Interpolation with <code>lerp</code> allows the game to be smoothly drawn regardless
     * of update frequency ({@link #TIMESTEP}).
     * <p>
     * <b>When an object in the game changes over time (e.g. position), interpolate between its
     * current and next state using <code>lerp</code>.
     * </b>
     * e.g.
     * <pre>
     * {@code
     * float x = object.position.x + object.velocity.x * lerp;
     * float y = object.position.y + object.velocity.y * lerp;
     * // draw object at x, y
     * }
     * </pre>
     * instead of
     * <pre>
     * {@code
     * float x = object.position.x;
     * float y = object.position.y;
     * // draw object at x, y
     * }
     * </pre>
     *
     * @param lerp interpolation factor
     */
    protected abstract void render(double lerp, Canvas canvas, Paint paint);

    /**
     * To pause the game when the pause button is pressed
     *
     * @param paused
     */
    public void setPaused(boolean paused) {
        isPaused = paused;
    }

    public void setDoubleSpeed(boolean doubleSpeed){
        this.isDoubleSpeed = doubleSpeed;
    }
}
