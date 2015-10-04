package org.synopia.audio.model;


import org.synopia.shadow.Parameter;
import org.synopia.shadow.ReadonlyParameter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by synopia on 20.09.2015.
 */
public class Track implements Serializable, ATrack {
    @Parameter
    private String filename;
    @Parameter
    private String artist;
    @Parameter
    private String title;
    @Parameter
    private String label;

    @Parameter
    private Key tagKey;
    @Parameter
    private Key calculatedKey;

    @Parameter
    private float tagBpm;
    @Parameter
    private float calculatedBpm;
    @Parameter
    private float bpm;
    @Parameter
    private float firstBeat;

    @ReadonlyParameter
    private List<Marker> beatMarkers = new ArrayList<>();

    public Track() {
    }

    public Track(String filename) {
        this.filename = filename;
    }

    @Override
    public float getBpm() {
        return bpm;
    }

    @Override
    public void setBpm(float bpm) {
        this.bpm = bpm;
    }

    public Key getKey() {
        return calculatedKey != null ? calculatedKey : tagKey;
    }

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public String getArtist() {
        return artist;
    }

    @Override
    public void setArtist(String artist) {
        this.artist = artist;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public Key getTagKey() {
        return tagKey;
    }

    @Override
    public void setTagKey(Key tagKey) {
        this.tagKey = tagKey;
    }

    @Override
    public Key getCalculatedKey() {
        return calculatedKey;
    }

    @Override
    public void setCalculatedKey(Key calculatedKey) {
        this.calculatedKey = calculatedKey;
    }

    @Override
    public void setTagBpm(float tagBpm) {
        this.tagBpm = tagBpm;
    }

    @Override
    public float getTagBpm() {
        return tagBpm;
    }

    @Override
    public float getCalculatedBpm() {
        return calculatedBpm;
    }

    @Override
    public void setCalculatedBpm(float calculatedBpm) {
        this.calculatedBpm = calculatedBpm;
    }

    public List<Marker> getBeatMarkers() {
        return beatMarkers;
    }

    public void setBeatMarkers(List<Marker> beatMarkers) {
        this.beatMarkers = beatMarkers;
    }

    public float getFirstBeat() {
        return firstBeat;
    }

    public void setFirstBeat(float firstBeat) {
        this.firstBeat = firstBeat;
    }


    @Override
    public String toString() {
        return "Track{" +
                "filename='" + filename + '\'' +
                ", artist='" + artist + '\'' +
                ", title='" + title + '\'' +
                ", label='" + label + '\'' +
                ", tagKey=" + tagKey +
                ", calculatedKey=" + calculatedKey +
                ", tagBpm=" + tagBpm +
                ", calculatedBpm=" + calculatedBpm +
                ", firstBeat=" + firstBeat +
                '}';
    }
}
