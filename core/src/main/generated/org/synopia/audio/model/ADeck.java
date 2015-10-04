package org.synopia.audio.model;
public interface ADeck extends org.synopia.shadow.ShadowObject {
    float getBpm();
    void setBpm(float bpm);
    org.synopia.audio.model.ATrack getTrack();
    void setTrack(org.synopia.audio.model.ATrack track);
    org.synopia.audio.model.AClip getClip();
    void setClip(org.synopia.audio.model.AClip clip);
    org.synopia.audio.model.AClipPlayer getClipPlayer();
    void setClipPlayer(org.synopia.audio.model.AClipPlayer clipPlayer);
    org.synopia.audio.model.ABand3Eq getEq();
    void setEq(org.synopia.audio.model.ABand3Eq eq);
    org.synopia.audio.model.AGain getGain();
    void setGain(org.synopia.audio.model.AGain gain);
}
