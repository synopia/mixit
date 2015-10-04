package org.synopia.audio.gdxui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.kotcrab.vis.ui.VisUI;
import org.synopia.audio.model.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by synopia on 26.09.2015.
 */
public class MarkerLayer extends PixmapLayer {
    private final Pixmap atlas;
    private final TextureRegion region;
    private final BitmapFont.Glyph[] glyphs = new BitmapFont.Glyph[12];
    private Marker currentBeat;
    private Marker selectedBeat;

    public MarkerLayer(TimeWindow timeWindow, int width, int height) {
        super(timeWindow, width, height);

        BitmapFont font1 = VisUI.getSkin().getFont("small-font");
        BitmapFont.BitmapFontData data = font1.getData();
        region = font1.getRegion();
        this.region.getTexture().getTextureData().prepare();
        atlas = this.region.getTexture().getTextureData().consumePixmap();
        for (int i = 0; i < 10; i++) {
            glyphs[i] = data.getGlyph((char) ('0' + i));
        }
        glyphs[10] = data.getGlyph('.');
        glyphs[11] = data.getGlyph('-');

        addListener(new InputListener() {

            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                double time = timeWindow.screenToTime(x / width);
                currentBeat = beatGrid.findFirst(timeWindow, width, time);
                selectedBeat = beatGrid.findNext(timeWindow, width, time);
                return false;
            }
        });
        addListener(new DragListener() {
            private Marker first;
            boolean dragging;
            float dragStartX;
            float dragStartY;
            double dragStartTime;
            double dragStartOriginTime;
            double dragStartMarkerTime;
            int startBeat;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return super.touchDown(event, x, y, pointer, button) || (event.getButton() == 1 && selectedBeat != null);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                if (event.getButton() == 1 && selectedBeat != null) {
                    beatGrid.setOriginTime((float) selectedBeat.time);
                }
            }

            @Override
            public void dragStart(InputEvent event, float x, float y, int pointer) {
                if (y > pixmap.getHeight() - 20) {
                    dragStartX = getTouchDownX();
                    dragStartY = getTouchDownY();
                    dragStartOriginTime = beatGrid.getOriginTime();
                    dragStartTime = timeWindow.screenToTime(getTouchDownX() / pixmap.getWidth());

                    first = beatGrid.findNext(timeWindow, pixmap.getWidth(), dragStartTime);
                    if (first != null) {
                        dragStartMarkerTime = first.time;
                        startBeat = first.beatNumber;
                    }
                    dragging = true;
                }
            }

            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                if (dragging) {
                    double time = timeWindow.screenToTime(x / pixmap.getWidth());
                    if (first == null) {
                        float firstBeat = (float) (dragStartOriginTime - (dragStartTime - time));
                        track.setFirstBeat(firstBeat);
                    } else {
                        double newTime = dragStartMarkerTime - (dragStartTime - time);
                        double bpm = startBeat / (newTime - dragStartOriginTime) * 60;
                        if (bpm > 50 && bpm < 200) {
                            track.setBpm((float) bpm);
                        }
                    }
                }
            }

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer) {
                dragging = false;
                first = null;

            }
        });
    }

    @Override
    protected void draw(TimeWindow timeWindow) {
        pixmap.setColor(Color.LIGHT_GRAY);
        pixmap.fillRectangle(0, 0, pixmap.getWidth(), 10);
        pixmap.setColor(Color.BLACK);
        pixmap.fillRectangle(0, 11, pixmap.getWidth(), 20);
        pixmap.setColor(0, 0, 0, 0);
        Pixmap.setBlending(Pixmap.Blending.None);
        pixmap.fillRectangle(0, 20, pixmap.getWidth(), pixmap.getHeight());
        Pixmap.setBlending(Pixmap.Blending.SourceOver);
        if (beatGrid != null) {
            List<Marker> area = new ArrayList<>();
            beatGrid.filter(timeWindow, pixmap.getWidth()).forEach(marker -> {
                int x = (int) (pixmap.getWidth() * timeWindow.timeToScreen(marker.time));
                pixmap.setColor(Color.DARK_GRAY);
                pixmap.fillRectangle(x - 2, 1, 4, 4);
                if (marker == selectedBeat) {
                    pixmap.setColor(Color.WHITE);
                    pixmap.drawRectangle(x - 3, 0, 6, 6);
                }
                pixmap.drawLine(x, 20, x, pixmap.getHeight());
                if (marker == currentBeat || area.size() == 1) {
                    area.add(marker);
                }
                if (marker.text != null) {
                    for (int i = 0; i < marker.text.length(); i++) {
                        x += drawChar(marker.text.charAt(i), x, 10);
                    }
                }
            });

            if (area.size() == 2) {
                Marker nextBeat = area.get(1);
                int x = (int) (pixmap.getWidth() * timeWindow.timeToScreen(currentBeat.time));
                int nextX = (int) (pixmap.getWidth() * timeWindow.timeToScreen(nextBeat.time));
                pixmap.setColor(Color.WHITE);
                pixmap.drawRectangle(x + 5, 0, nextX - x - 10, 8);
            }
        }
    }

    public Marker getCurrentBeat() {
        return currentBeat;
    }

    public Marker getSelectedBeat() {
        return selectedBeat;
    }

    private int drawChar(char ch, int x, int y) {
        BitmapFont.Glyph glyph = null;
        if (ch >= '0' && ch <= '9') {
            glyph = glyphs[ch - '0'];
        } else if (ch == '.') {
            glyph = glyphs[10];
        } else {
            glyph = glyphs[11];
        }
        pixmap.drawPixmap(atlas, x + glyph.xoffset, y - glyph.height - glyph.yoffset, region.getRegionX() + glyph.srcX, region.getRegionY() + glyph.srcY, glyph.width, glyph.height);
        return glyph.xadvance;
    }
}
