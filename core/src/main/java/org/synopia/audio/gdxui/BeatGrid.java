package org.synopia.audio.gdxui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.synopia.audio.model.Marker;
import org.synopia.audio.model.Markers;

/**
 * Created by synopia on 18.09.2015.
 */
public class BeatGrid extends Markers {
    private static Logger logger = LogManager.getLogger(BeatGrid.class);

    private float originTime = 0;
    private float bpm = 170;

    private int frames;
    private double sampleRate;
    private boolean valid;

    @Override
    protected boolean accept(Marker marker, TimeWindow timeWindow, double pixelPerSecond) {
        double pixelPerBeat = pixelPerSecond * 60 / bpm;
        int mod = 1;
        while (pixelPerBeat < 50) {
            pixelPerBeat *= 2;
            mod *= 2;
        }
        return (marker.beatNumber % mod) == 0;
    }

    public boolean isValid() {
        return valid;
    }

    public void recalculate() {
        if (valid || bpm <= 0 || sampleRate <= 0 || frames <= 0) {
            return;
        }
        logger.trace("Calculate new beatgrid bpm {} samplerate {} frames {}", bpm, sampleRate, frames);
        float beatsPerMinute = Math.min(Math.max(bpm, 50), 200);
        double bps = beatsPerMinute / 60;
        int count = (int) (frames / sampleRate * bps);
        int one = (int) (originTime * bps);
        for (int i = 0; i < count; i++) {
            Marker marker;
            if (i < markers.size()) {
                marker = getMarkers(i);
            } else {
                marker = new Marker();
                markers.add(marker);
            }
            marker.beatNumber = i - one;
            marker.time = originTime + (i - one) / bps;
            marker.type = Marker.Type.BEAT_GRID;
            marker.salience = 7;
            marker.text = beatText(marker.beatNumber);
        }
        for (int i = count; i < markers.size(); i++) {
            markers.remove(markers.size() - 1);
        }

        valid = true;
    }

    public Marker getBeat(int beatNo) {
        int index = beatNo - markers.get(0).beatNumber;
        return markers.get(index);
    }

    private String beatText(int beat) {
        boolean neg = beat < 0;
        beat = Math.abs(beat);
        int b3 = beat % 4;
        int b2 = beat / 4;
        return (neg ? "-" : "") + (b2 + 1) + "." + (b3 + 1);
    }

    public float getOriginTime() {
        return originTime;
    }

    public float getBpm() {
        return bpm;
    }

    public void setOriginTime(float originTime) {
        this.originTime = originTime;
        valid = false;
    }


    public int getFrames() {
        return frames;
    }

    public void setFrames(int frames) {
        this.frames = frames;
        valid = false;
    }

    public void setBpm(float bpm) {
        this.bpm = bpm;
        valid = false;
    }

    public double getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(double sampleRate) {
        this.sampleRate = sampleRate;
        valid = false;
    }
}
