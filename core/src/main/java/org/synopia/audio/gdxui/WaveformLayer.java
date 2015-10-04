package org.synopia.audio.gdxui;

import com.badlogic.gdx.graphics.Color;
import org.synopia.audio.model.Marker;

import java.util.List;

/**
 * Created by synopia on 26.09.2015.
 */
public class WaveformLayer extends PixmapLayer {

    public WaveformLayer(TimeWindow timeWindow, int width, int height) {
        super(timeWindow, width, height);
    }

    @Override
    protected void draw(TimeWindow timeWindow) {
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        drawBeats();
        for (int x = 0; x < pixmap.getWidth(); x++) {
            int frameStart = timeWindow.screenToFrame(x / (float) pixmap.getWidth());
            int frameEnd = timeWindow.screenToFrame((x + 1) / (float) pixmap.getWidth());
            frameStart /= 256;
            frameEnd /= 256;
            frameStart = Math.min(clip.getFrames(), Math.max(0, frameStart));
            frameEnd = Math.max(0, Math.min(clip.getFrames(), frameEnd));
            byte[][] data = clip.getDownSampled();
            if (frameStart < 0 || frameEnd >= clip.getFrames()) {
                continue;
            }
            if (frameEnd - frameStart > 1) {
                float low = 0;
                float mid = 0;
                float high = 0;
                for (int i = frameStart; i < frameEnd; i++) {
                    low += data[0][i] / (float) Byte.MAX_VALUE;
                    mid += data[1][i] / (float) Byte.MAX_VALUE;
                    high += data[2][i] / (float) Byte.MAX_VALUE;
                }
                low /= (frameEnd - frameStart);
                mid /= (frameEnd - frameStart);
                high /= (frameEnd - frameStart);
                setColor(low, mid, high);
                drawBars(x, 0, -(low + mid + high));
                drawBars(x, 0, (low + mid + high));
            } else {
                float l1 = data[0][frameStart] / (float) Byte.MAX_VALUE;
                float m1 = data[1][frameStart] / (float) Byte.MAX_VALUE;
                float h1 = data[2][frameStart] / (float) Byte.MAX_VALUE;
                float l2 = data[0][frameEnd] / (float) Byte.MAX_VALUE;
                float m2 = data[1][frameEnd] / (float) Byte.MAX_VALUE;
                float h2 = data[2][frameEnd] / (float) Byte.MAX_VALUE;
                float l = (l1 + l2) / 2;
                float m = (m1 + m2) / 2;
                float h = (h1 + h2) / 2;
                setColor(l, m, h);
                drawBars(x, 0, -(l + m + h));
                drawBars(x, 0, (l + m + h));
            }
        }
    }

    private void drawBeats() {
        List<Marker> beatMarkers = track.getBeatMarkers();
        float start = (float) timeWindow.screenToTime(0);
        float end = (float) timeWindow.screenToTime(1);
        int midY = pixmap.getHeight() / 2;
        pixmap.setColor(Color.CYAN);

        for (Marker marker : beatMarkers) {
            float beatLength = (float) marker.salience / 4;
            float beatHeight = beatLength / 5;
            beatHeight = Math.min(0.9f, beatHeight);
            if (marker.time + beatLength >= start && marker.time < end) {
                int startX = (int) (timeWindow.timeToScreen(marker.time) * pixmap.getWidth());
                int endX = (int) (timeWindow.timeToScreen(marker.time + beatLength) * pixmap.getWidth());
                pixmap.fillTriangle(startX, (int) (midY - beatHeight * pixmap.getHeight()), endX, midY, startX, (int) (midY + beatHeight * pixmap.getHeight()));
            }
        }
    }

    static float[] lowColor = new float[]{0.6f, 0.4f, 0};
    static float[] midColor = new float[]{0.4f, 0, 0.3f};
    static float[] highColor = new float[]{0, 0, 0.5f};

    private void setColor(float low, float mid, float high) {
        float r = (low * lowColor[0] + mid * midColor[0] + high * highColor[0]);
        float g = (low * lowColor[1] + mid * midColor[1] + high * highColor[1]);
        float b = (low * lowColor[2] + mid * midColor[2] + high * highColor[2]);
        float max = Math.max(r, Math.max(g, b));
        pixmap.setColor(r / max, g / max, b / max, 1);
    }

    private void drawBars(int x, float from, float to) {
        int midY = pixmap.getHeight() / 2;
        from /= 6;
        to /= 6;
        pixmap.drawLine(x, (int) (midY + from * pixmap.getHeight()), x, (int) (midY + to * pixmap.getHeight()));
    }

}
