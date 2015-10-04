package org.synopia.audio.core;

import be.tarsos.dsp.io.TarsosDSPAudioFormat;

/**
 * Created by synopia on 19.09.2015.
 */
public interface AudioNode {
    Patch parent();

    void process(int samplesPos);

    void setFormat(TarsosDSPAudioFormat format, int bufferSize);

    TarsosDSPAudioFormat getFormat();

    boolean finished();

    boolean canFinish();

    void finish();

    void start();
}
