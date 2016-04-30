package io.zacholauson.coffeetime;

public class StartingWeightValidator {

    public interface Callback {
        void onValidStartingWeight(final double startingWeight);
        void onInvalidStartingWeight();
    }

    public static void validate(String startingWeight, Callback callback) {
        final String strippedStartingWeight = startingWeight.replaceAll("[^0-9?!\\.]","");

        if (strippedStartingWeight != null && !strippedStartingWeight.isEmpty()) {
            callback.onValidStartingWeight(Double.parseDouble(strippedStartingWeight));
        } else {
            callback.onInvalidStartingWeight();
        }
    }
}
