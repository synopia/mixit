package org.synopia.audio.model;
public interface AMarkers extends org.synopia.shadow.ShadowObject {
    org.synopia.audio.model.Marker getMarkers(int index);
    int markersCount();
    void setMarkers(int index, org.synopia.audio.model.Marker markers);
}
