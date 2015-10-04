package org.synopia.audio.model;

import javafx.scene.paint.Color;

import java.io.Serializable;

/**
 * Created by synopia on 18.09.2015.
 */
public class Marker implements Serializable {
    public enum Type {
        BEAT_GRID(Color.GREEN, Color.GREEN.darker().darker()),
        BEAT(Color.RED, Color.DARKGRAY.darker().darker()),;
        public Color color;
        public Color lineColor;

        Type(Color color, Color lineColor) {
            this.color = color;
            this.lineColor = lineColor;
        }
    }

    public String text;
    public int beatNumber;
    public double time;
    public double salience;
    public Type type;

}
