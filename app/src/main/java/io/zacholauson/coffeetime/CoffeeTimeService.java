package io.zacholauson.coffeetime;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.util.Log;

import java.util.ArrayList;

import io.zacholauson.coffeetime.validators.StartingWeightValidator;
import io.zacholauson.coffeetime.views.CoffeeTimeCardDrawer;

public class CoffeeTimeService extends Service {

    private static final String LIVE_CARD_TAG = CoffeeTimeService.class.getSimpleName();
    private static final double DEFAULT_WATER_TO_COFFEE_RATIO = 16;

    private LiveCard mLiveCard;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mLiveCard == null) {
            mLiveCard = new LiveCard(this, LIVE_CARD_TAG);

            final ArrayList<String> voiceResults = intent.getExtras().getStringArrayList(RecognizerIntent.EXTRA_RESULTS);
            if (voiceResults != null && !voiceResults.isEmpty()) {
                StartingWeightValidator.validate(voiceResults.get(0), new StartingWeightValidator.Callback() {
                    @Override
                    public void onValidStartingWeight(double parsedStartingWeight) {
                        final double targetWeight = calculateTargetWeight(
                                parsedStartingWeight, DEFAULT_WATER_TO_COFFEE_RATIO);

                        mLiveCard.setDirectRenderingEnabled(true)
                                .getSurfaceHolder()
                                .addCallback(new CoffeeTimeCardDrawer(CoffeeTimeService.this, parsedStartingWeight, targetWeight));

                        prepareMenuIntent();
                    }

                    @Override
                    public void onInvalidStartingWeight() {
                        Log.wtf(LIVE_CARD_TAG, "Invalid starting weight: " + voiceResults.get(0));
                    }
                });
            } else {
                Log.wtf(LIVE_CARD_TAG, "No starting weight provided: " + intent.getExtras());
            }
        } else {
            mLiveCard.navigate();
        }

        return START_NOT_STICKY;
    }

    private double calculateTargetWeight(double startingWeight, double ratio) {
        return startingWeight * ratio;
    }

    @Override
    public void onDestroy() {
        if (mLiveCard != null && mLiveCard.isPublished()) {
            mLiveCard.unpublish();
            mLiveCard = null;
        }

        super.onDestroy();
    }

    private void prepareMenuIntent() {
        final Intent menuIntent = new Intent(this, LiveCardMenuActivity.class);
        mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
        mLiveCard.attach(this);
        mLiveCard.publish(PublishMode.REVEAL);
    }
}
