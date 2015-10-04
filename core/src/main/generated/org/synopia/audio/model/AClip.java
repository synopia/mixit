package org.synopia.audio.model;
public interface AClip extends org.synopia.shadow.ShadowObject {
    float[] getSamples();
    float[] getMonoSamples();
    byte[][] getDownSampled();
    be.tarsos.dsp.io.TarsosDSPAudioFormat getFormat();
    be.tarsos.dsp.io.TarsosDSPAudioFormat getMonoFormat();
    int getFrames();
    float getSampleRate();
    boolean getLoaded();
}
