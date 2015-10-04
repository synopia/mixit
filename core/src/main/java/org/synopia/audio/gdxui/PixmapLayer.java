package org.synopia.audio.gdxui;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.kotcrab.vis.ui.widget.VisImage;
import org.synopia.audio.model.AClip;
import org.synopia.audio.model.ATrack;

/**
 * Created by synopia on 26.09.2015.
 */
public abstract class PixmapLayer extends VisImage {
    protected BeatGrid beatGrid;
    protected TimeWindow timeWindow;
    protected final Pixmap pixmap;
    protected AClip clip;
    protected ATrack track;
    private Texture texture;

    public PixmapLayer(TimeWindow timeWindow, int width, int height) {
        this.timeWindow = timeWindow;
        pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        texture = new Texture(pixmap);
        setDrawable(texture);
    }

    public void redraw(TimeWindow timeWindow) {
        if (clip != null) {
            draw(timeWindow);
        }
        drawTexture();
    }

    protected abstract void draw(TimeWindow timeWindow);

    public AClip getClip() {
        return clip;
    }

    public void setClip(AClip clip) {
        this.clip = clip;
    }

    public ATrack getTrack() {
        return track;
    }

    public void setTrack(ATrack track) {
        this.track = track;
    }

    public void setBeatGrid(BeatGrid beatGrid) {
        this.beatGrid = beatGrid;
    }

    public void drawTexture() {
        texture.draw(pixmap, 0, 0);
    }
}
