package io.zacholauson.coffeetime;

import com.google.android.glass.timeline.DirectRenderingCallback;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.SurfaceHolder;

public class LiveCardRenderer implements DirectRenderingCallback {

    private static final long FRAME_TIME_MILLIS = 40;
    private static final float TEXT_SIZE = 50f;
    private static final int ALPHA_INCREMENT = 5;
    private static final int MAX_ALPHA = 256;

    private final Paint mPaint;
    private final double mStartingWeight;
    private final double mTargetWeight;

    private int mBottomY;
    private int mLeftX;
    private int mRightX;

    private SurfaceHolder mHolder;
    private boolean mRenderingPaused;

    private RenderThread mRenderThread;

    public LiveCardRenderer(double startingWeight, double targetWeight) {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(TEXT_SIZE);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTypeface(Typeface.create("sans-serif-thin", Typeface.NORMAL));
        mPaint.setAlpha(0);

        mStartingWeight = startingWeight;
        mTargetWeight = targetWeight;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mBottomY = (int) (height * 0.85);
        mLeftX = (int) (width * 0.20);
        mRightX = (int) (width * 0.80);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;
        mRenderingPaused = false;
        updateRenderingState();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder = null;
        updateRenderingState();
    }

    @Override
    public void renderingPaused(SurfaceHolder holder, boolean paused) {
        mRenderingPaused = paused;
        updateRenderingState();
    }

    private void updateRenderingState() {
        boolean shouldRender = (mHolder != null) && !mRenderingPaused;
        boolean isRendering = (mRenderThread != null);

        if (shouldRender != isRendering) {
            if (shouldRender) {
                mRenderThread = new RenderThread();
                mRenderThread.start();
            } else {
                mRenderThread.quit();
                mRenderThread = null;
            }
        }
    }

    private void draw() {
        Canvas canvas;
        try {
            canvas = mHolder.lockCanvas();
        } catch (Exception e) {
            return;
        }
        if (canvas != null) {
            mPaint.setAlpha((mPaint.getAlpha() + ALPHA_INCREMENT) % MAX_ALPHA);
            canvas.drawText(String.format("%sg", mStartingWeight), mLeftX, mBottomY, mPaint);
            canvas.drawText(String.format("%sg", mTargetWeight), mRightX, mBottomY, mPaint);

            mHolder.unlockCanvasAndPost(canvas);
        }
    }

    private class RenderThread extends Thread {
        private boolean mShouldRun;

        public RenderThread() {
            mShouldRun = true;
        }

        private synchronized boolean shouldRun() {
            return mShouldRun;
        }

        public synchronized void quit() {
            mShouldRun = false;
        }

        @Override
        public void run() {
            while (shouldRun()) {
                long frameStart = SystemClock.elapsedRealtime();
                draw();
                long frameLength = SystemClock.elapsedRealtime() - frameStart;

                long sleepTime = FRAME_TIME_MILLIS - frameLength;
                if (sleepTime > 0) {
                    SystemClock.sleep(sleepTime);
                }
            }
        }
    }
}
