package org.synopia.audio.core;

import be.tarsos.dsp.io.TarsosDSPAudioFormat;

/**
 * Created by synopia on 19.09.2015.
 */
public abstract class BaseHasOutput implements HasOutput {
    protected float[] outputBuffer;
    private boolean finished;
    protected TarsosDSPAudioFormat format;
    protected Patch parent;

    public BaseHasOutput(Patch parent) {
        this.parent = parent;
        if (parent != null) {
            parent.add(this);
        }
    }

    @Override
    public Patch parent() {
        return parent;
    }

    @Override
    public void setFormat(TarsosDSPAudioFormat format, int bufferSize) {
        this.format = format;
        outputBuffer = new float[bufferSize];
    }

    @Override
    public float[] output() {
        return outputBuffer;
    }

    @Override
    public boolean finished() {
        return finished;
    }

    @Override
    public void finish() {
        finished = true;
    }

    @Override
    public void start() {
        finished = false;
    }

    @Override
    public TarsosDSPAudioFormat getFormat() {
        return format;
    }

    public int getBufferSize() {
        return outputBuffer.length;
    }

}
