package org.synopia.audio.gdxui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import org.synopia.audio.model.AClip;
import org.synopia.audio.model.AClipPlayer;
import org.synopia.audio.model.ATrack;
import org.synopia.audio.model.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by synopia on 26.09.2015.
 */
public class LayerView extends Stack {
    private final TimeWindow timeWindow = new TimeWindow();
    private final List<PixmapLayer> layers = new ArrayList<>();
    private boolean dragged;
    private MarkerLayer markerLayer;
    private AClipPlayer clipPlayer;
    private int lastPosition;
    private boolean zoomable;
    private boolean followPlayer;
    private boolean showPlayerPos;
    private boolean valid;
    private int width;
    private int height;

    public LayerView(int width, int height) {
        super();
        this.width = width;
        this.height = height;

        timeWindow.start = 90;
        timeWindow.length = 5;

        addLayer(new WaveformLayer(timeWindow, width, height));
        addLayer(markerLayer = new MarkerLayer(timeWindow, width, height));

        addListener(new DragListener() {
            double dragTimeStart;
            double dragLengthStart;
            double dragYStart;

            @Override
            public void dragStart(InputEvent event, float x, float y, int pointer) {
                if (!zoomable) {
                    return;
                }
                dragTimeStart = timeWindow.screenToTime(getTouchDownX() / width);
                dragLengthStart = timeWindow.length;
                dragYStart = getTouchDownY();
                dragged = true;
            }

            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                if (!zoomable) {
                    return;
                }
                if (dragYStart < height - 20) {
                    double newTime = timeWindow.screenToTime(x / width);
                    timeWindow.start += (dragTimeStart - newTime);
                    timeWindow.length = Math.max(0.001, dragLengthStart * (1 + (y - dragYStart) / height));
                }
            }

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer) {
            }
        });
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!dragged) {
                    fire(new BeatClicked(markerLayer.getCurrentBeat(), markerLayer.getSelectedBeat()));
                }
                dragged = false;
            }
        });
    }

    @Override
    public float getPrefWidth() {
        return width;
    }

    @Override
    public float getPrefHeight() {
        return height;
    }

    @Override
    public float getMinWidth() {
        return width;
    }

    @Override
    public float getMinHeight() {
        return height;
    }

    @Override
    public float getMaxWidth() {
        return width;
    }

    @Override
    public float getMaxHeight() {
        return height;
    }

    private void addLayer(PixmapLayer layer) {
        layers.add(layer);
        add(layer);
    }

    public void setClip(AClip clip) {
        timeWindow.sampleRate = clip.getSampleRate();
        for (PixmapLayer layer : layers) {
            layer.setClip(clip);
        }
        valid = false;
    }

    public void setTrack(ATrack track) {
        for (PixmapLayer layer : layers) {
            layer.setTrack(track);
        }
        valid = false;
    }

    public void setClipPlayer(AClipPlayer clipPlayer) {
        this.clipPlayer = clipPlayer;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        redraw();
        super.draw(batch, parentAlpha);
    }

    public void redraw() {
        if (clipPlayer != null && clipPlayer.getPlaying()) {
            int currentPosition = clipPlayer.getPosition();
            if (followPlayer) {
                if (currentPosition != lastPosition) {
                    timeWindow.start = (float) (currentPosition / 44100f - timeWindow.length / 2f);
                    lastPosition = currentPosition;
                }
                timeWindow.start += Gdx.graphics.getDeltaTime();
            }
        }
        if (!valid) {
            for (PixmapLayer layer : layers) {
                layer.redraw(timeWindow);
            }
            valid = !zoomable && !followPlayer;
        } else {
            for (PixmapLayer layer : layers) {
                layer.drawTexture();
            }
        }
        if (showPlayerPos) {

        }
    }

    public void setBeatGrid(BeatGrid beatGrid) {
        for (PixmapLayer layer : layers) {
            layer.setBeatGrid(beatGrid);
        }
        valid = false;
    }

    public boolean isZoomable() {
        return zoomable;
    }

    public void setZoomable(boolean zoomable) {
        this.zoomable = zoomable;
    }

    public boolean isFollowPlayer() {
        return followPlayer;
    }

    public void setFollowPlayer(boolean followPlayer) {
        this.followPlayer = followPlayer;
    }

    public boolean isShowPlayerPos() {
        return showPlayerPos;
    }

    public void setShowPlayerPos(boolean showPlayerPos) {
        this.showPlayerPos = showPlayerPos;
    }

    public TimeWindow getTimeWindow() {
        return timeWindow;
    }

    public static class BeatClicked extends Event {
        public final Marker currentBeat;
        public final Marker selectedBeat;

        public BeatClicked(Marker currentBeat, Marker selectedBeat) {
            this.currentBeat = currentBeat;
            this.selectedBeat = selectedBeat;
        }
    }
}
