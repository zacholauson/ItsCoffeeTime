package io.zacholauson.coffeetime;

import com.google.android.glass.timeline.DirectRenderingCallback;

import android.content.Context;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

public class LiveCardRenderer implements DirectRenderingCallback {

    private static final long FRAME_TIME_MILLIS = 40;

    private final Chronometer.Listener mChronometerListener = new Chronometer.Listener() {

        @Override
        public void onChange() {
            if (mHolder != null) {
                draw();
            }
        }
    };


    private final CoffeeTimeCardView view;

    private SurfaceHolder mHolder;
    private boolean mRenderingPaused;

    private RenderThread mRenderThread;

    class CoffeeTimeCardView extends FrameLayout {

        private final Chronometer chronometerView;
        private final TextView startingWeightView;
        private final TextView targetWeightView;

        public CoffeeTimeCardView(Context context) {
            this(context, null, 0);
        }

        public CoffeeTimeCardView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public CoffeeTimeCardView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);

            LayoutInflater.from(context).inflate(R.layout.card_coffee_time, this);

            chronometerView = (Chronometer) findViewById(R.id.chronometer);
            chronometerView.setListener(mChronometerListener);

            startingWeightView = (TextView) findViewById(R.id.starting_weight);
            targetWeightView = (TextView) findViewById(R.id.target_weight);
        }

        public void startChronometer() {
            Log.wtf(CoffeeTimeCardView.class.getSimpleName(), "STARTING CHRONOMETER");
            chronometerView.start();
        }

        public void stopChronometer() {
            Log.wtf(CoffeeTimeCardView.class.getSimpleName(), "STOPPING CHRONOMETER");
            chronometerView.stop();
        }

        public void setStartingWeight(double startingWeight) {
            startingWeightView.setText(String.format("%sg", startingWeight));
        }

        public void setTargetWeight(double targetWeight) {
            targetWeightView.setText(String.format("%sg", targetWeight));
        }
    }

    public LiveCardRenderer(final Context context, final double startingWeight, final double targetWeight) {
        view = new CoffeeTimeCardView(context);
        view.setStartingWeight(startingWeight);
        view.setTargetWeight(targetWeight);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        final int measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        final int measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);

        view.measure(measuredWidth, measuredHeight);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
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
        if (mHolder != null && !mRenderingPaused) {
            view.startChronometer();

//            mRenderThread = new RenderThread();
//            mRenderThread.start();
        } else {
            view.stopChronometer();

//            mRenderThread.quit();
//            mRenderThread = null;
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
            view.draw(canvas);
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
