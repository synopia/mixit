package org.synopia.audio.model;
public interface AClipPlayer extends org.synopia.shadow.ShadowObject {
    org.synopia.audio.model.ClipPlayerType getType();
    void setType(org.synopia.audio.model.ClipPlayerType type);
    int getPosition();
    void setPosition(int position);
    boolean getPlaying();
    void setPlaying(boolean playing);
    boolean getNotchBack();
    void setNotchBack(boolean notchBack);
    boolean getNotchForward();
    void setNotchForward(boolean notchForward);
    float getSpeed();
    void setSpeed(float speed);
}
