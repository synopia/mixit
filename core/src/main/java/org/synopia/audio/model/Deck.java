package org.synopia.audio.model;

import org.synopia.audio.core.Band3Eq;
import org.synopia.audio.core.Gain;
import org.synopia.audio.core.Patch;
import org.synopia.shadow.Parameter;

/**
 * Created by synopia on 19.09.2015.
 */
public class Deck extends Patch implements ADeck {
    @Parameter
    private float bpm = 170f;
    @Parameter
    private Track track;
    @Parameter
    private Clip clip;
    @Parameter
    private ClipPlayer clipPlayer;

    @Parameter
    private Band3Eq eq;
    @Parameter
    private Gain gain;

    private TrackDatabase db;

    public Deck(TrackDatabase db, Patch parent) {
        super(parent);
        this.db = db;
        clipPlayer = new ClipPlayer(this);
        eq = new Band3Eq(this);
        gain = new Gain(this);
        connect(clipPlayer, eq);
        connect(eq, gain);
    }

    @Override
    public ClipPlayer getClipPlayer() {
        return clipPlayer;
    }

    @Override
    public void setClipPlayer(AClipPlayer clipPlayer) {
        this.clipPlayer = (ClipPlayer) clipPlayer;
    }

    @Override
    public Band3Eq getEq() {
        return eq;
    }

    @Override
    public Gain getGain() {
        return gain;
    }

    @Override
    public float getBpm() {
        return bpm;
    }

    @Override
    public Track getTrack() {
        return track;
    }

    @Override
    public Clip getClip() {
        return clip;
    }

    @Override
    public void setBpm(float bpm) {
        this.bpm = bpm;
        float targetBpm = ((ADecks) parent()).getBpm();
        if (track != null) {
            getClipPlayer().setSpeed(targetBpm / bpm);
        }
    }

    @Override
    public void setTrack(ATrack track) {
        this.track = (Track) track;
        clipPlayer.setClip(null);
        if (track != null) {
            db.load(this.track).whenComplete((clip, e) -> {
                bpm = track.getBpm();
                setClip(clip);
            });
        }
    }

    @Override
    public void setClip(AClip clip) {
        this.clip = (Clip) clip;
        clipPlayer.setClip(this.clip);
        clipPlayer.setSpeed(((ADecks) parent()).getBpm() / bpm);
    }

    @Override
    public void setEq(ABand3Eq eq) {
        this.eq = (Band3Eq) eq;
    }

    @Override
    public void setGain(AGain gain) {
        this.gain = (Gain) gain;
    }
}
