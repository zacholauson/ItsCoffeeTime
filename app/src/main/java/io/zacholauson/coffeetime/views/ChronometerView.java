package io.zacholauson.coffeetime.views;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.zacholauson.coffeetime.R;

public class ChronometerView extends FrameLayout {

    public interface Listener {
        void onChange();
    }

    static final long DELAY_MILLIS = 41;

    private final TextView mMinutesView;
    private final TextView mSecondsView;

    private final Handler mHandler = new Handler();
    private final Runnable mUpdateTextRunnable = new Runnable() {

        @Override
        public void run() {
            if (mRunning) {
                updateText();

                postDelayed(mUpdateTextRunnable, DELAY_MILLIS);
            }
        }
    };

    private boolean mRunning;

    private long mBaseMillis;

    private Listener mChangeListener;

    public ChronometerView(Context context) {
        this(context, null, 0);
    }

    public ChronometerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChronometerView(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);

        LayoutInflater.from(context).inflate(R.layout.card_chronometer, this);

        mMinutesView = (TextView) findViewById(R.id.minute);
        mSecondsView = (TextView) findViewById(R.id.second);

        setBaseMillis(getElapsedRealtime());
    }

    public void setBaseMillis(long baseMillis) {
        mBaseMillis = baseMillis;
        updateText();
    }

    public void setListener(Listener listener) {
        mChangeListener = listener;
    }

    public void start() {
        if (!mRunning) {
            postDelayed(mUpdateTextRunnable, DELAY_MILLIS);
        }

        mRunning = true;
    }

    public void stop() {
        if (mRunning) {
            removeCallbacks(mUpdateTextRunnable);
        }

        mRunning = false;
    }

    @Override
    public boolean postDelayed(Runnable action, long delayMillis) {
        return mHandler.postDelayed(action, delayMillis);
    }

    @Override
    public boolean removeCallbacks(Runnable action) {
        mHandler.removeCallbacks(action);

        return true;
    }

    private long getElapsedRealtime() {
        return SystemClock.elapsedRealtime();
    }

    private void updateText() {
        long millis = getElapsedRealtime() - mBaseMillis;

        millis %= TimeUnit.HOURS.toMillis(1);
        mMinutesView.setText(String.format(Locale.US, "%02d", TimeUnit.MILLISECONDS.toMinutes(millis)));

        millis %= TimeUnit.MINUTES.toMillis(1);
        mSecondsView.setText(String.format(Locale.US, "%02d", TimeUnit.MILLISECONDS.toSeconds(millis)));

        if (mChangeListener != null) {
            mChangeListener.onChange();
        }
    }
}

