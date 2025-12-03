package academy.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class PercentileEstimator {

    private static final int MARKERS = 5;
    private static final int EXACT_THRESHOLD = 10_000;

    private final double percentile;
    private final List<Long> exactSamples = new ArrayList<>();
    private P2Estimator streamingEstimator;
    private long count;

    PercentileEstimator(double percentile) {
        if (percentile <= 0.0d || percentile >= 1.0d) {
            throw new IllegalArgumentException("percentile must be between 0 and 1");
        }
        this.percentile = percentile;
    }

    void addSample(long value) {
        count++;
        if (streamingEstimator == null) {
            exactSamples.add(value);
            if (exactSamples.size() > EXACT_THRESHOLD) {
                switchToStreaming();
            }
        } else {
            streamingEstimator.addSample(value);
        }
    }

    double estimate() {
        if (count == 0) {
            return 0.0d;
        }
        if (streamingEstimator == null) {
            List<Long> sorted = new ArrayList<>(exactSamples);
            Collections.sort(sorted);
            return precisePercentile(sorted, percentile);
        }
        return streamingEstimator.estimate();
    }

    private void switchToStreaming() {
        streamingEstimator = new P2Estimator(percentile);
        for (long sample : exactSamples) {
            streamingEstimator.addSample(sample);
        }
        exactSamples.clear();
    }

    private static double precisePercentile(List<Long> sorted, double percentile) {
        if (sorted.isEmpty()) {
            return 0.0d;
        }
        double rank = percentile * (sorted.size() - 1);
        int lowerIndex = (int) Math.floor(rank);
        int upperIndex = (int) Math.ceil(rank);
        if (lowerIndex == upperIndex) {
            return sorted.get(lowerIndex);
        }
        double lowerValue = sorted.get(lowerIndex);
        double upperValue = sorted.get(upperIndex);
        double weight = rank - lowerIndex;
        return lowerValue + weight * (upperValue - lowerValue);
    }

    private static final class P2Estimator {

        private final double percentile;
        private final double[] markerHeights = new double[MARKERS];
        private final double[] markerPositions = new double[MARKERS];
        private final double[] desiredPositions = new double[MARKERS];
        private final double[] increments = new double[MARKERS];
        private final List<Long> initialSamples = new ArrayList<>(MARKERS);

        private boolean initialized;

        private P2Estimator(double percentile) {
            this.percentile = percentile;
            increments[0] = 0.0d;
            increments[1] = percentile / 2.0d;
            increments[2] = percentile;
            increments[3] = (1.0d + percentile) / 2.0d;
            increments[4] = 1.0d;
        }

        private void addSample(long value) {
            if (!initialized) {
                initialSamples.add(value);
                if (initialSamples.size() == MARKERS) {
                    initializeMarkers();
                }
                return;
            }
            updateMarkers(value);
        }

        private double estimate() {
            if (!initialized) {
                List<Long> sorted = new ArrayList<>(initialSamples);
                Collections.sort(sorted);
                return precisePercentile(sorted, percentile);
            }
            return markerHeights[2];
        }

        private void initializeMarkers() {
            Collections.sort(initialSamples);
            for (int i = 0; i < MARKERS; i++) {
                markerHeights[i] = initialSamples.get(i);
                markerPositions[i] = i + 1;
            }
            desiredPositions[0] = 1.0d;
            desiredPositions[1] = 1.0d + 2.0d * percentile;
            desiredPositions[2] = 1.0d + 4.0d * percentile;
            desiredPositions[3] = 3.0d + 2.0d * percentile;
            desiredPositions[4] = 5.0d;
            initialSamples.clear();
            initialized = true;
        }

        private void updateMarkers(long value) {
            int k;
            if (value < markerHeights[0]) {
                markerHeights[0] = value;
                k = 0;
            } else if (value < markerHeights[1]) {
                k = 0;
            } else if (value < markerHeights[2]) {
                k = 1;
            } else if (value < markerHeights[3]) {
                k = 2;
            } else if (value < markerHeights[4]) {
                k = 3;
            } else {
                markerHeights[4] = value;
                k = 3;
            }

            for (int i = k + 1; i < MARKERS; i++) {
                markerPositions[i]++;
            }
            for (int i = 0; i < MARKERS; i++) {
                desiredPositions[i] += increments[i];
            }

            for (int i = 1; i <= 3; i++) {
                double d = desiredPositions[i] - markerPositions[i];
                if ((d >= 1.0d && markerPositions[i + 1] - markerPositions[i] > 1.0d)
                        || (d <= -1.0d && markerPositions[i - 1] - markerPositions[i] < -1.0d)) {
                    double sign = Math.signum(d);
                    double candidate = parabolicUpdate(i, sign);
                    if (markerHeights[i - 1] < candidate && candidate < markerHeights[i + 1]) {
                        markerHeights[i] = candidate;
                    } else {
                        markerHeights[i] = linearUpdate(i, (int) sign);
                    }
                    markerPositions[i] += sign;
                }
            }
        }

        private double parabolicUpdate(int index, double direction) {
            double hp = markerHeights[index];
            double hp1 = markerHeights[index + 1];
            double hm1 = markerHeights[index - 1];
            double np = markerPositions[index];
            double np1 = markerPositions[index + 1];
            double nm1 = markerPositions[index - 1];

            double numerator = direction
                    * ((np - nm1 + direction) * (hp1 - hp) / (np1 - np)
                            + (np1 - np - direction) * (hp - hm1) / (np - nm1));
            return hp + numerator / (np1 - nm1);
        }

        private double linearUpdate(int index, int direction) {
            int adjacent = index + direction;
            return markerHeights[index]
                    + direction
                            * (markerHeights[adjacent] - markerHeights[index])
                            / (markerPositions[adjacent] - markerPositions[index]);
        }
    }
}
