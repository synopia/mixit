package org.synopia.audio.model;
public interface ATrack extends org.synopia.shadow.ShadowObject {
    java.lang.String getFilename();
    void setFilename(java.lang.String filename);
    java.lang.String getArtist();
    void setArtist(java.lang.String artist);
    java.lang.String getTitle();
    void setTitle(java.lang.String title);
    java.lang.String getLabel();
    void setLabel(java.lang.String label);
    org.synopia.audio.model.Key getTagKey();
    void setTagKey(org.synopia.audio.model.Key tagKey);
    org.synopia.audio.model.Key getCalculatedKey();
    void setCalculatedKey(org.synopia.audio.model.Key calculatedKey);
    float getTagBpm();
    void setTagBpm(float tagBpm);
    float getCalculatedBpm();
    void setCalculatedBpm(float calculatedBpm);
    float getBpm();
    void setBpm(float bpm);
    float getFirstBeat();
    void setFirstBeat(float firstBeat);
    java.util.List<org.synopia.audio.model.Marker> getBeatMarkers();
}
