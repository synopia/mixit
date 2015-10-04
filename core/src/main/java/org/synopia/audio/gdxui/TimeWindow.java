package org.synopia.audio.gdxui;

/**
 * Created by synopia on 18.09.2015.
 */
public class TimeWindow {
    public double sampleRate;
    public double start;
    public double length;

    public int screenToFrame(double screen) {
        return (int) (screenToTime(screen) * sampleRate);
    }

    public double screenToTime(double screen) {
        return (start + length * screen);
    }

    public double timeToScreen(double time) {
        return (time - start) / length;
    }

    public double end() {
        return start + length;
    }

    @Override
    public String toString() {
        return "TimeWindow{" +
                "start=" + start +
                ", length=" + length +
                '}';
    }
}
