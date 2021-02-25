package com.wsu.towerdefense;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import androidx.annotation.NonNull;

public abstract class AbstractGame extends SurfaceView implements Callback {

  /**
   * Interval between game state updates
   */
  protected static final double TIMESTEP = 1.0 / 60;

  private boolean running;
  private int displayWidth;
  private int displayHeight;

  private SurfaceHolder surfaceHolder;
  private Thread thread;
  private Paint paint;

  public AbstractGame(Context context, int displayWidth, int displayHeight) {
    super(context);
    running = false;
    this.displayWidth = displayWidth;
    this.displayHeight = displayHeight;

    surfaceHolder = getHolder();
    surfaceHolder.addCallback(this);
    thread = null;
    paint = new Paint();

    setKeepScreenOn(true);
  }

  public void loop() {
    double timePrevious = (System.currentTimeMillis() / 1000.0);
    double acc = 0;

    while (running) {
      double timeCurrent = (System.currentTimeMillis() / 1000.0);
      double delta = timeCurrent - timePrevious;
      timePrevious = timeCurrent;

      acc += delta;
      while (acc >= TIMESTEP) {
        update(TIMESTEP);
        acc -= TIMESTEP;
      }

      _render(acc / TIMESTEP);
    }
  }

  private void _render(double alpha) {
    Canvas canvas = surfaceHolder.lockCanvas();
    if (canvas != null) {
      render(alpha * TIMESTEP, canvas, paint);
      surfaceHolder.unlockCanvasAndPost(canvas);
    }
  }

  private void startThread() {
    running = true;
    thread = new Thread(this::loop);
    thread.start();
  }

  private void stopThread() {
    running = false;
    try {
      thread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
      Log.e(getContext().getString(R.string.logcatKey), Log.getStackTraceString(e));
    }
  }

  @Override
  public void surfaceCreated(@NonNull SurfaceHolder holder) {
    startThread();
  }

  @Override
  public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
    stopThread();
    holder.getSurface().release();
  }

  @Override
  public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
    displayWidth = width;
    displayHeight = height;
  }

  protected int getDisplayWidth() {
    return displayWidth;
  }

  protected int getDisplayHeight() {
    return displayHeight;
  }

  protected abstract void update(double delta);

  protected abstract void render(double lerp, Canvas canvas, Paint paint);
}
