package org.synopia.audio.gdxui.table;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by synopia on 27.09.2015.
 */
public class DataTable<T> extends com.badlogic.gdx.scenes.scene2d.ui.List<T> {
    private List<Column<T, ?>> cols = new ArrayList<>();
    private float prefHeight;
    private float prefWidth;
    private Rectangle cullingArea;
    private float itemHeight;

    public DataTable() {
        super(VisUI.getSkin());

        addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (pointer == 0 && button != 0) return false;
                if (getSelection().isDisabled()) return false;
                DataTable.this.touchDown(y);
                return true;
            }
        });

    }

    public <R> void addColumn(Column<T, R> column) {
        cols.add(column);
    }

    public void setRows(Collection<T> rows) {
        setItems(new Array(rows.toArray()));
    }

    @Override
    public void layout() {
        final BitmapFont font = getStyle().font;
        itemHeight = font.getCapHeight() - font.getDescent() * 2 + 4;
        prefHeight = 20 + itemHeight * getItems().size;
        prefWidth = 20;
        if (cols != null) {
            for (Column<T, ?> col : cols) {
                prefWidth += col.width;
            }
        }
    }

    void touchDown(float y) {
        if (getItems().size == 0) return;
        float height = getHeight();
        if (getStyle().background != null) {
            height -= getStyle().background.getTopHeight() + getStyle().background.getBottomHeight();
            y -= getStyle().background.getBottomHeight();
        }
        int index = (int) ((height - y) / itemHeight);
        index = Math.max(0, index);
        index = Math.min(getItems().size - 1, index);
        getSelection().choose(getItems().get(index));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        validate();
        ListStyle style = getStyle();
        BitmapFont font = style.font;
        float textOffsetX = 0;
        float textOffsetY = 0;
        Drawable selectedDrawable = style.selection;
        Color fontColorSelected = style.fontColorSelected;
        Color fontColorUnselected = style.fontColorUnselected;

        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

        float x = getX(), y = getY(), width = getWidth(), height = getHeight();
        float itemY = height;

        Drawable background = style.background;
        if (background != null) {
            background.draw(batch, x, y, width, height);
            float leftWidth = background.getLeftWidth();
            x += leftWidth;
            itemY -= background.getTopHeight();
            width -= leftWidth + background.getRightWidth();
        }

        font.setColor(fontColorUnselected.r, fontColorUnselected.g, fontColorUnselected.b, fontColorUnselected.a * parentAlpha);
        for (int i = 0; i < getItems().size; i++) {
            if (cullingArea == null || (itemY - itemHeight <= cullingArea.y + cullingArea.height && itemY >= cullingArea.y)) {
                T item = getItems().get(i);
                float columnOffset = 0;
                boolean selected = getSelected() == item;
                if (selected) {
                    selectedDrawable.draw(batch, x, y + itemY - itemHeight, width, itemHeight);
                    font.setColor(fontColorSelected.r, fontColorSelected.g, fontColorSelected.b, fontColorSelected.a * parentAlpha);
                }
                for (Column<T, ?> col : cols) {
                    String str = col.stringValue(item);
                    if (str != null) {
                        font.draw(batch, str, columnOffset + x + textOffsetX, y + itemY - textOffsetY);
                    }
                    columnOffset += col.width;
                }
                if (selected) {
                    font.setColor(fontColorUnselected.r, fontColorUnselected.g, fontColorUnselected.b, fontColorUnselected.a
                            * parentAlpha);
                }
            } else if (itemY < cullingArea.y) {
                break;
            }
            itemY -= itemHeight;
        }

    }

    public float getPrefWidth() {
        validate();
        return prefWidth;
    }

    public float getPrefHeight() {
        validate();
        return prefHeight;
    }

    @Override
    public void setCullingArea(Rectangle cullingArea) {
        super.setCullingArea(cullingArea);
        this.cullingArea = cullingArea;
    }

    public abstract static class Column<T, R> {
        public float width;

        public Column(float width) {
            this.width = width;
        }

        public abstract String stringValue(T item);
    }
}
