package org.synopia.audio.model;

import org.synopia.audio.gdxui.TimeWindow;
import org.synopia.shadow.Parameter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by synopia on 20.09.2015.
 */
public class Markers implements Serializable, AMarkers {
    @Parameter
    protected List<Marker> markers = new ArrayList<>();

    @Override
    public Marker getMarkers(int index) {
        return markers.get(index);
    }

    @Override
    public void setMarkers(int index, Marker markers) {
        this.markers.set(index, markers);
    }

    public Stream<Marker> filter(TimeWindow timeWindow, double width) {
        double pixelPerSecond = width / timeWindow.length;

        return markers.stream()
                .filter(marker -> marker.time > timeWindow.start && marker.time <= timeWindow.end() && accept(marker, timeWindow, pixelPerSecond));
    }

    @Override
    public int markersCount() {
        return markers.size();
    }

    protected boolean accept(Marker marker, TimeWindow timeWindow, double pixelPerSecond) {
        return pixelPerSecond > 50;
    }

    public Marker findFirst(TimeWindow timeWindow, double width, double time) {
        double pixelPerSecond = width / timeWindow.length;
        Marker last = markers.size() > 0 ? markers.get(0) : null;
        for (Marker marker : markers) {
            if (accept(marker, timeWindow, pixelPerSecond)) {
                if (marker.time > time) {
                    return last;
                }
                last = marker;
            }
        }
        return last;
    }

    public Marker findNext(TimeWindow timeWindow, double width, double time) {
        double pixelPerSecond = width / timeWindow.length;
        Marker found = null;
        Marker last = markers.size() > 0 ? markers.get(0) : null;
        for (Marker marker : markers) {
            if (accept(marker, timeWindow, pixelPerSecond)) {
                if (marker.time > time) {
                    if (time - last.time < marker.time - time) {
                        found = last;
                        break;
                    } else {
                        found = marker;
                        break;
                    }
                }
                last = marker;
            }
        }
        if (found != null) {
            double v = Math.abs(found.time - time) * pixelPerSecond;
            if (v < 15) {
                return found;
            }
        }
        return null;
    }

}
