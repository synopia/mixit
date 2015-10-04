package org.synopia.audio.model;

public interface ADecks extends org.synopia.shadow.ShadowObject {
    org.synopia.audio.model.ADeck getDecks(int index);

    int decksCount();

    void setDecks(int index, org.synopia.audio.model.ADeck decks);

    float getBpm();

    void setBpm(float bpm);

    org.synopia.audio.model.AMixer getMixer();

    void setMixer(org.synopia.audio.model.AMixer mixer);

    org.synopia.audio.model.AMixer getCueMixer();

    void setCueMixer(org.synopia.audio.model.AMixer cueMixer);
}
