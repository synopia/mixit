package org.synopia.audio.model;
public interface AMixer extends org.synopia.shadow.ShadowObject {
    float getGains(int index);
    int gainsCount();
    void setGains(int index, float gains);
}
