package org.synopia.audio.core;

import org.synopia.audio.model.AGain;
import org.synopia.shadow.Parameter;

import java.util.Arrays;
import java.util.List;

/**
 * Created by synopia on 25.09.2015.
 */
public class Gain extends BaseHasOutput implements HasInput, AGain {
    private HasOutput input;
    @Parameter
    private float gain = 0.5f;

    public Gain(Patch parent) {
        super(parent);
    }

    @Override
    public void process(int samplesPos) {
        for (int i = 0; i < getBufferSize(); i++) {
            outputBuffer[i] = input.output()[i] * gain;
        }
    }

    @Override
    public void addInput(HasOutput input) {
        this.input = input;
    }

    @Override
    public List<HasOutput> inputs() {
        return Arrays.asList(input);
    }

    @Override
    public boolean canFinish() {
        return input.finished();
    }

    @Override
    public float getGain() {
        return gain;
    }

    @Override
    public void setGain(float gain) {
        this.gain = gain;
    }
}
