package org.synopia.audio.model;

/**
 * Created by synopia on 26.09.2015.
 */
public class MinMax {
    private float min = 1;
    private float max = -1;
    private int minCount;
    private int maxCount;

    public void nextSample(float sample) {
        if (sample < min) {
            min = sample;
            minCount++;
        }
        if (sample > max) {
            max = sample;
            maxCount++;
        }
    }

    public float min() {
        return minCount > 0 ? min : 0;
    }

    public float max() {
        return maxCount > 0 ? max : 0;
    }
}
