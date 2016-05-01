package io.zacholauson.coffeetime.views;

import com.google.android.glass.timeline.DirectRenderingCallback;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;

public class CoffeeTimeCardDrawer implements DirectRenderingCallback {

    private static final String TAG = CoffeeTimeCardDrawer.class.getSimpleName();

    private final CoffeeTimeCardView view;
    private SurfaceHolder mHolder;
    private boolean mRenderingPaused;

    public CoffeeTimeCardDrawer(final Context context, final double startingWeight, final double targetWeight) {
        view = new CoffeeTimeCardView(context);

        view.setStartingWeight(startingWeight);
        view.setTargetWeight(targetWeight);

        view.setChronometerViewListener(new ChronometerView.Listener() {
            @Override
            public void onChange() {
                if (mHolder != null) {
                    draw();
                }
            }
        });
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
        mRenderingPaused = false;
        mHolder = holder;
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
        } else {
            view.stopChronometer();
        }
    }

    private void draw() {
        try {
            final Canvas canvas = mHolder.lockCanvas();

            if (canvas != null) {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                view.draw(canvas);

                mHolder.unlockCanvasAndPost(canvas);
            }
        } catch (Exception exception) {
            Log.wtf(TAG, "Failed to draw view.", exception);
        }
    }
}
