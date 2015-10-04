package org.synopia.audio.model.keyfinder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by synopia on 24.09.2015.
 */
public class Chromagram {
    private List<float[]> chromaData = new ArrayList<>();

    public float[] collapseToOneHop() {
        float[] oneHop = new float[KeyFinder.BANDS];
        for (int h = 0; h < chromaData.size(); h++) {
            for (int b = 0; b < KeyFinder.BANDS; b++) {
                oneHop[b] += chromaData.get(h)[b] / chromaData.size();
            }
        }
        return oneHop;
    }

    public void append(float[] data) {
        chromaData.add(data);
    }

    public int getHops() {
        return chromaData.size();
    }
}
