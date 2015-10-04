package org.synopia.audio.model.keyfinder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by synopia on 24.09.2015.
 */
public class ToneProfile {
    private List<BiNode> tonics = new ArrayList<>();

    public ToneProfile(double[] customProfile) {
        for (int o = 0; o < KeyFinder.OCTAVES; o++) {
            BiNode tonic = new BiNode(customProfile[o * KeyFinder.SEMITONES]);
            BiNode q = tonic;
            for (int i = 1; i < KeyFinder.SEMITONES; i++) {
                q.right = new BiNode(customProfile[o * KeyFinder.SEMITONES + i]);
                q.right.left = q;
                q = q.right;
            }
            q.right = tonic;
            tonic.left = q;

            for (int i = 0; i < 3; i++) {
                tonic = tonic.right;
            }
            tonics.add(tonic);
        }
    }

    public float cosineSimilarity(float[] chromaVector, int offset) {
        double intersection = 0;
        double profileNorm = 0;
        double inputNorm = 0;
        for (int o = 0; o < KeyFinder.OCTAVES; o++) {
            BiNode p = tonics.get(o);
            for (int i = 0; i < offset; i++) {
                p = p.left;
            }
            for (int i = o * KeyFinder.SEMITONES; i < (o + 1) * KeyFinder.SEMITONES; i++) {
                intersection += chromaVector[i] * p.data;
                profileNorm += Math.pow(p.data, 2);
                inputNorm += Math.pow(chromaVector[i], 2);
                p = p.right;
            }
        }

        if (profileNorm > 0 && inputNorm > 0) {
            return (float) (intersection / (Math.sqrt(profileNorm) * Math.sqrt(inputNorm)));
        }
        return 0;
    }

    static double MAJOR_PROFILE[] = {
            7.23900502618145225142,
            3.50351166725158691406,
            3.58445177536649417505,
            2.84511816478676315967,
            5.81898892118549859731,
            4.55865057415321039969,
            2.44778850545506543313,
            6.99473192146829525484,
            3.39106613673504853068,
            4.55614256655143456953,
            4.07392666663523606019,
            4.45932757378886890365,
    };

    static double MINOR_PROFILE[] = {
            7.00255045060284420089,
            3.14360279015996679775,
            4.35904319714962529275,
            5.40418120718934069657,
            3.67234420879306133756,
            4.08971184917797891956,
            3.90791435991553992579,
            6.19960288562316463867,
            3.63424625625277419871,
            2.87241191079875557435,
            5.35467999794542670600,
            3.83242038595048351013,
    };

    static double OCTAVE_WEIGHTS[] = {
            0.39997267549999998559,
            0.55634425248300645173,
            0.52496636345143543600,
            0.60847548384277727607,
            0.59898115679999996974,
            0.49072435317960994006,
    };

    static double[] major;
    static double[] minor;

    static {
        major = new double[KeyFinder.OCTAVES * KeyFinder.SEMITONES];
        for (int o = 0; o < KeyFinder.OCTAVES; o++) {
            for (int s = 0; s < KeyFinder.SEMITONES; s++) {
                major[o * KeyFinder.SEMITONES + s] = OCTAVE_WEIGHTS[o] * MAJOR_PROFILE[s];
            }
        }
        minor = new double[KeyFinder.OCTAVES * KeyFinder.SEMITONES];
        for (int o = 0; o < KeyFinder.OCTAVES; o++) {
            for (int s = 0; s < KeyFinder.SEMITONES; s++) {
                minor[o * KeyFinder.SEMITONES + s] = OCTAVE_WEIGHTS[o] * MINOR_PROFILE[s];
            }
        }
    }
}
