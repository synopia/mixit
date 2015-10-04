package org.synopia.audio.model.keyfinder;

import org.synopia.audio.model.Key;

/**
 * Created by synopia on 24.09.2015.
 */
public class KeyFinder {
    public static final int OCTAVES = 6;
    public static final int SEMITONES = 12;
    public static final int BANDS = OCTAVES * SEMITONES;
    public static final int KEYS = SEMITONES * 2;
    public static final int FFT_FRAME_SIZE = 16384;
    public static final int HOP_SIZE = FFT_FRAME_SIZE / 4;
    private final ToneProfile major;
    private final ToneProfile minor;
    private final ToneProfile silence;

    public KeyFinder() {
        major = new ToneProfile(ToneProfile.major);
        minor = new ToneProfile(ToneProfile.minor);
        silence = new ToneProfile(new double[BANDS]);
    }

    public Key classify(float[] chromaVector) {
        int bestMatch = Key.SILENCE.ordinal();
        float[] scores = new float[24];
        float bestScore = silence.cosineSimilarity(chromaVector, 0);
        for (int i = 0; i < SEMITONES; i++) {
            scores[i * 2] = major.cosineSimilarity(chromaVector, i);
            scores[i * 2 + 1] = minor.cosineSimilarity(chromaVector, i);
        }
        for (int i = 0; i < 24; i++) {
            if (scores[i] > bestScore) {
                bestScore = scores[i];
                bestMatch = i;
            }
        }
        return Key.values()[bestMatch];
    }
}
