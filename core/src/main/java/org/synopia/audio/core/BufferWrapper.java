package org.synopia.audio.core;

import be.tarsos.dsp.io.TarsosDSPAudioFormat;

/**
 * Created by synopia on 27.09.2015.
 */
public class BufferWrapper implements HasOutput {
    private float[] buffer;

    public BufferWrapper(float[] buffer) {
        this.buffer = buffer;
    }

    @Override
    public float[] output() {
        return buffer;
    }

    @Override
    public Patch parent() {
        return null;
    }

    @Override
    public void process(int samplesPos) {
    }

    @Override
    public void setFormat(TarsosDSPAudioFormat format, int bufferSize) {
    }

    @Override
    public TarsosDSPAudioFormat getFormat() {
        return null;
    }

    @Override
    public boolean finished() {
        return true;
    }

    @Override
    public boolean canFinish() {
        return true;
    }

    @Override
    public void finish() {

    }

    @Override
    public void start() {

    }
}
