package io.zacholauson.coffeetime.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.Locale;

import io.zacholauson.coffeetime.R;

public class CoffeeTimeCardView extends FrameLayout {

    private final ChronometerView chronometerView;
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

        chronometerView    = (ChronometerView) findViewById(R.id.chronometer);
        startingWeightView = (TextView)    findViewById(R.id.starting_weight);
        targetWeightView   = (TextView)    findViewById(R.id.target_weight);
    }

    public void setChronometerViewListener(ChronometerView.Listener listener) {
        chronometerView.setListener(listener);
    }

    public void startChronometer() {
        chronometerView.start();
    }

    public void stopChronometer() {
        chronometerView.stop();
    }

    public void setStartingWeight(double startingWeight) {
        startingWeightView.setText(formatWeight(startingWeight));
    }

    public void setTargetWeight(double targetWeight) {
        targetWeightView.setText(formatWeight(targetWeight));
    }

    private String formatWeight(double weight) {
        if (weight % 1 == 0) {
            return String.format(Locale.US, "%2.0fg", weight);
        } else {
            return String.format(Locale.US, "%2.1fg", weight);
        }
    }
}
