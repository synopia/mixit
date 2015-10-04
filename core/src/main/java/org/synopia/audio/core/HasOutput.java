package org.synopia.audio.core;

/**
 * Created by synopia on 19.09.2015.
 */
public interface HasOutput extends AudioNode {
    float[] output();
}
