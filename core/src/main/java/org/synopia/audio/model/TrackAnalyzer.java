package org.synopia.audio.model;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.beatroot.*;
import be.tarsos.dsp.onsets.BeatRootSpectralFluxOnsetDetector;
import org.synopia.audio.model.keyfinder.Chromagram;
import org.synopia.audio.model.keyfinder.KeyFinder;
import org.synopia.audio.model.keyfinder.SpectrumAnalyzer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by synopia on 18.09.2015.
 */
public class TrackAnalyzer {
    private EventList beats;
    private Agent best;
    private EventList onsetList;

    public void analyze(Track track, Clip clip) {
        onsetDetection(clip);
        beatDetection();
        List<Marker> markers = toMarkers(false);
        track.setBeatMarkers(markers);
        track.setCalculatedBpm((float) getBpm());
        track.setCalculatedKey(keyDetection(clip));
    }

    private void onsetDetection(Clip clip) {
        onsetList = new EventList();
        BeatRootSpectralFluxOnsetDetector detector = new BeatRootSpectralFluxOnsetDetector(clip.getFrames(), 44100, 2048, 441);
        detector.setHandler((time, salience) -> {
            Event e = newEvent(time, 0);
            e.salience = salience;
            onsetList.add(e);
        });
        AudioEvent event = new AudioEvent(clip.getMonoFormat());
        clip.toStream(2048, 441).forEach(buffer -> {
            event.setFloatBuffer(buffer);
            detector.process(event);
        });
        detector.processingFinished();
    }

    private void beatDetection() {
        AgentList agents = null;
        // tempo not given; use tempo induction
        agents = Induction.beatInduction(onsetList);
        agents.beatTrack(onsetList, -1);
        best = agents.bestAgent();
        if (best != null) {
//            best.fillBeats(-1.0);
            beats = best.events;
        } else {
            System.err.println("No best agent");
        }
    }

    private Key keyDetection(Clip clip) {
        KeyFinder keyFinder = new KeyFinder();
        SpectrumAnalyzer analyzer = new SpectrumAnalyzer();
        Chromagram chromagram = analyzer.buildChromagram(clip);
        float[] chromaVector = chromagram.collapseToOneHop();
        return keyFinder.classify(chromaVector);
    }

    private List<Marker> toMarkers(boolean agents) {
        List<Marker> markers = new ArrayList<>();
        Iterator<Event> iterator;
        if (best != null && agents) {
            iterator = best.events.iterator();
        } else {
            iterator = onsetList.iterator();
        }
        while (iterator.hasNext()) {
            Event event = iterator.next();
            Marker marker = new Marker();
            marker.salience = event.salience;
            marker.type = Marker.Type.BEAT;
            marker.time = event.keyDown;
            markers.add(marker);
        }
        return markers;
    }

    private double getBpm() {
        return best != null ? 60.d / best.beatInterval : 170;
    }

    public static Event newEvent(double time, int beatNum) {
        return new Event(time, time, time, 56, 64, beatNum, 0, 1);
    }

}
