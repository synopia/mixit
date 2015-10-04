package org.synopia.audio.model;

public interface ABand3Eq extends org.synopia.shadow.ShadowObject {
    float getLowGain();

    void setLowGain(float lowGain);

    float getMidGain();

    void setMidGain(float midGain);

    float getHighGain();

    void setHighGain(float highGain);

    float getLowFreq();

    void setLowFreq(float lowFreq);

    float getMixFreq();

    void setMixFreq(float mixFreq);

    float getHighFreq();

    void setHighFreq(float highFreq);
}
