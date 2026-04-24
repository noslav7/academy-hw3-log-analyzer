package academy.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class PercentileEstimator {

    /** Количество маркеров алгоритма P². */
    private static final int MARKERS = 5;
    /** Порог числа сэмплов для перехода от точного расчёта к потоковой оценке. */
    private static final int EXACT_THRESHOLD = 10_000;

    /** Целевой процентиль (например, 0.95). */
    private final double percentile;
    /** Буфер точных сэмплов для небольших выборок. */
    private final List<Long> exactSamples = new ArrayList<>();
    /** Потоковый оценщик P² для больших объёмов данных. */
    private P2Estimator streamingEstimator;
    /** Общее количество зарегистрированных значений. */
    private long count;

    /** Создаёт оценщик для заданного процентиля. */
    PercentileEstimator(double percentile) {
        if (percentile <= 0.0d || percentile >= 1.0d) {
            throw new IllegalArgumentException("percentile must be between 0 and 1");
        }
        this.percentile = percentile;
    }

    /** Добавляет очередной размер ответа в расчёт процентиля. */
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

    /** Возвращает оценённое значение процентиля по накопленным данным. */
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

    /** Переключает оценку в потоковый режим и переносит накопленные сэмплы в P²-оценщик. */
    private void switchToStreaming() {
        streamingEstimator = new P2Estimator(percentile);
        for (long sample : exactSamples) {
            streamingEstimator.addSample(sample);
        }
        exactSamples.clear();
    }

    /** Точно вычисляет процентиль по отсортированной выборке с линейной интерполяцией. */
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

        /** Целевой процентиль для потоковой оценки. */
        private final double percentile;
        /** Высоты (значения) маркеров P². */
        private final double[] markerHeights = new double[MARKERS];
        /** Текущие позиции маркеров. */
        private final double[] markerPositions = new double[MARKERS];
        /** Желаемые позиции маркеров. */
        private final double[] desiredPositions = new double[MARKERS];
        /** Приращения желаемых позиций после каждого сэмпла. */
        private final double[] increments = new double[MARKERS];
        /** Первые сэмплы до полной инициализации маркеров. */
        private final List<Long> initialSamples = new ArrayList<>(MARKERS);

        /** Флаг, показывающий, инициализированы ли маркеры. */
        private boolean initialized;

        /** Создаёт внутренний P²-оценщик для указанного процентиля. */
        private P2Estimator(double percentile) {
            this.percentile = percentile;
            increments[0] = 0.0d;
            increments[1] = percentile / 2.0d;
            increments[2] = percentile;
            increments[3] = (1.0d + percentile) / 2.0d;
            increments[4] = 1.0d;
        }

        /** Добавляет значение в потоковую оценку процентиля. */
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

        /** Возвращает текущую оценку процентиля. */
        private double estimate() {
            if (!initialized) {
                List<Long> sorted = new ArrayList<>(initialSamples);
                Collections.sort(sorted);
                return precisePercentile(sorted, percentile);
            }
            return markerHeights[2];
        }

        /** Инициализирует маркеры после получения первых пяти значений. */
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

        /** Обновляет позиции и высоты маркеров после поступления нового значения. */
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

        /** Выполняет параболическое обновление высоты маркера. */
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

        /** Выполняет линейное обновление высоты маркера как fallback. */
        private double linearUpdate(int index, int direction) {
            int adjacent = index + direction;
            return markerHeights[index]
                    + direction
                            * (markerHeights[adjacent] - markerHeights[index])
                            / (markerPositions[adjacent] - markerPositions[index]);
        }
    }
}
