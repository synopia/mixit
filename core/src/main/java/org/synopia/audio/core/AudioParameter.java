package org.synopia.audio.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;

/**
 * Created by synopia on 22/09/15.
 */
public class AudioParameter<T> {
    private T value;
    private List<BinaryOperator<T>> consumers = new ArrayList<>();
    private AudioNode node;

    public AudioParameter(AudioNode node, T value) {
        this.node = node;
        set(value);
    }

    public void set(T value) {
        set(value, true);
    }

    public void set(T value, boolean inform) {
        if (value == this.value) {
            return;
        }
        T old = this.value;
        this.value = value;
        if (inform) {
            for (BinaryOperator<T> consumer : consumers) {
                consumer.apply(old, value);
            }
        }
    }

    public void addConsumer(BinaryOperator<T> consumer) {
        consumers.add(consumer);
    }

    public T get() {
        return value;
    }

    public AudioNode getNode() {
        return node;
    }
}
