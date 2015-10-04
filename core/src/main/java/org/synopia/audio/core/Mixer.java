package org.synopia.audio.core;

import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import org.synopia.audio.model.AMixer;
import org.synopia.shadow.Parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by synopia on 19.09.2015.
 */
public class Mixer extends BaseHasOutput implements HasInput, AMixer {
    private List<HasOutput> inputs = new ArrayList<>();
    @Parameter
    private List<Float> gains = new ArrayList<>();

    public Mixer(Patch parent) {
        super(parent);
    }

    @Override
    public void addInput(HasOutput input) {
        inputs.add(input);
        gains.add(1f);
    }

    @Override
    public int gainsCount() {
        return gains.size();
    }

    @Override
    public void process(int samplesPos) {
        int offs = 0;
        int length = getBufferSize();
        Arrays.fill(outputBuffer, offs, length + offs, 0);
        for (int inputIndex = 0; inputIndex < inputs.size(); inputIndex++) {
            HasOutput input = inputs.get(inputIndex);
            for (int i = offs; i < offs + length; i++) {
                outputBuffer[i] += input.output()[i] * gains.get(inputIndex);
            }
        }
    }

    @Override
    public List<HasOutput> inputs() {
        return inputs;
    }

    @Override
    public boolean canFinish() {
        for (HasOutput input : inputs) {
            if (!input.finished()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void setFormat(TarsosDSPAudioFormat format, int bufferSize) {
        super.setFormat(format, bufferSize);
        for (HasOutput input : inputs) {
            input.setFormat(format, bufferSize);
        }
    }

    @Override
    public float getGains(int index) {
        return gains.get(index);
    }

    @Override
    public void setGains(int index, float gains) {
        this.gains.set(index, gains);
    }
}
