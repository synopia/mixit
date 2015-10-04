package org.synopia.audio.gdxui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by synopia on 29.09.2015.
 */
public class Ui {
    private static Ui ui;
    private TextureAtlas atlas;
    private Map<String, Map<Integer, TextureRegion>> cache = new HashMap<>();

    public enum ButtonType {
        PLAY(3),
        REWIND(6),
        FORWARD(7),
        PAUSE(9),
        STOP(10),
        RECORD(11);

        int id;

        ButtonType(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    private Ui() {
        atlas = new TextureAtlas(Gdx.files.internal("tiles.txt"));
    }

    public static void load() {
        ui = new Ui();
    }

    public List<TextureRegion> getRegionsFromCache(String name, int count) {
        List<TextureRegion> regions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            regions.add(getRegionFromCache(name, i));
        }
        return regions;
    }

    public TextureRegion getRegionFromCache(String name, int index) {
        Map<Integer, TextureRegion> textureRegions = cache.get(name);
        if (textureRegions == null) {
            textureRegions = new HashMap<>();
            cache.put(name, textureRegions);
        }
        TextureRegion textureRegion = textureRegions.get(index);
        if (textureRegion == null) {
            textureRegion = atlas.findRegion(name, index);
            textureRegions.put(index, textureRegion);
        }

        return textureRegion;
    }

    public static Button.ButtonStyle getButtonStyle(ButtonType type) {
        return new Button.ButtonStyle(
                getDrawable("audiobuttons", type.id),
                getDrawable("audiobuttons_pressed", type.id),
                getDrawable("audiobuttons_pressed", type.id)
        );
    }

    public static TextureRegion getRegion(String name) {
        return ui.getRegionFromCache(name, -1);
    }

    public static TextureRegion getRegion(String name, int index) {
        return ui.getRegionFromCache(name, index);
    }

    public static List<TextureRegion> getRegions(String name, int count) {
        return ui.getRegionsFromCache(name, count);
    }

    public static Drawable getDrawable(String name) {
        return new TextureRegionDrawable(getRegion(name));
    }

    public static Drawable getDrawable(String name, int index) {
        return new TextureRegionDrawable(getRegion(name, index));
    }

}
