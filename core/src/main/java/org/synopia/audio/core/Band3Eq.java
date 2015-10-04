package org.synopia.audio.core;


import org.synopia.audio.model.ABand3Eq;
import org.synopia.shadow.Parameter;

import java.util.Arrays;
import java.util.List;

/**
 * Created by synopia on 21.09.2015.
 */
public class Band3Eq extends BaseHasOutput implements HasInput, ABand3Eq {
    private HasOutput input;
    private double lf;
    private double f1p0;
    private double f1p1;
    private double f1p2;
    private double f1p3;
    private double hf;
    private double f2p0;
    private double f2p1;
    private double f2p2;
    private double f2p3;
    private double sdm1;
    private double sdm2;
    private double sdm3;
    @Parameter
    private float lowGain = 1;
    @Parameter
    private float midGain = 1;
    @Parameter
    private float highGain = 1;
    private double vsa = (1.0 / 4294967295.0);

    @Parameter
    private float lowFreq = 880f;
    @Parameter
    private float mixFreq = 44100f;
    @Parameter
    private float highFreq = 5000f;

    private double lastLow;
    private double lastMid;
    private double lastHigh;

    public Band3Eq(Patch parent) {
        super(parent);
    }

    @Override
    public void process(int samplesPos) {
        lf = 2 * Math.sin(Math.PI * ((lowFreq / mixFreq)));
        hf = 2 * Math.sin(Math.PI * ((highFreq / mixFreq)));

        for (int i = 0; i < getBufferSize(); i++) {
            outputBuffer[i] = (float) do3Band(input.output()[i]);
        }
    }

    public double do3Band(double sample) {
        f1p0 += (lf * (sample - f1p0)) + vsa;
        f1p1 += (lf * (f1p0 - f1p1));
        f1p2 += (lf * (f1p1 - f1p2));
        f1p3 += (lf * (f1p2 - f1p3));
        lastLow = f1p3;

        f2p0 += (hf * (sample - f2p0)) + vsa;
        f2p1 += (hf * (f2p0 - f2p1));
        f2p2 += (hf * (f2p1 - f2p2));
        f2p3 += (hf * (f2p2 - f2p3));

        lastHigh = sdm3 - f2p3;
        lastMid = sdm3 - (lastHigh + lastLow);
        lastLow *= lowGain;
        lastMid *= midGain;
        lastHigh *= highGain;

        sdm3 = sdm2;
        sdm2 = sdm1;
        sdm1 = sample;

        return lastLow + lastMid + lastHigh;
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

    public double getLastLow() {
        return lastLow;
    }

    public double getLastMid() {
        return lastMid;
    }

    public double getLastHigh() {
        return lastHigh;
    }

    @Override
    public float getLowFreq() {
        return lowFreq;
    }

    @Override
    public void setLowFreq(float lowFreq) {
        this.lowFreq = lowFreq;
    }

    @Override
    public float getMixFreq() {
        return mixFreq;
    }

    @Override
    public void setMixFreq(float mixFreq) {
        this.mixFreq = mixFreq;
    }

    @Override
    public float getHighFreq() {
        return highFreq;
    }

    @Override
    public void setHighFreq(float highFreq) {
        this.highFreq = highFreq;
    }

    @Override
    public float getLowGain() {
        return lowGain;
    }

    @Override
    public void setLowGain(float lowGain) {
        this.lowGain = lowGain;
    }

    @Override
    public float getMidGain() {
        return midGain;
    }

    @Override
    public void setMidGain(float midGain) {
        this.midGain = midGain;
    }

    @Override
    public float getHighGain() {
        return highGain;
    }

    @Override
    public void setHighGain(float highGain) {
        this.highGain = highGain;
    }
}
