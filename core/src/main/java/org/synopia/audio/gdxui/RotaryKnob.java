package org.synopia.audio.gdxui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisProgressBar;

import java.util.List;

/**
 * Created by synopia on 26.09.2015.
 */
public class RotaryKnob extends VisProgressBar {
    private float dragStartX;
    private float dragStartY;
    private float dragStartValue;
    private VisLabel label;
    private final List<TextureRegion> regions;

    public RotaryKnob(String label, float min, float max, float stepSize) {
        super(min, max, stepSize, true);
        regions = Ui.getRegions("effect_knob", 31);
        this.label = new VisLabel(label);

        addListener(new DragListener() {
            @Override
            public void dragStart(InputEvent event, float x, float y, int pointer) {
                dragStartX = x;
                dragStartY = y;
                dragStartValue = getValue();
            }

            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                setValue(dragStartValue + (y - dragStartY) / 100 * (max - min));
            }

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer) {

            }
        });
    }

    @Override
    public float getPrefWidth() {
        return Math.max(32, label != null ? label.getPrefWidth() : 0);
    }

    @Override
    public float getPrefHeight() {
        return 32 + 8 + (label != null ? label.getPrefHeight() : 0);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float value = getValue() - getMinValue();
        value /= (getMaxValue() - getMinValue());
        value *= 30;
        int v = (int) value;
        TextureRegion region = regions.get(v);
        batch.draw(region, getX(), getY(), 32, 32);
        label.setPosition(0 + getX(), getY() - 20);
        label.draw(batch, parentAlpha);
    }
}
