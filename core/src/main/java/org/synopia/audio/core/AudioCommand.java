package org.synopia.audio.core;

/**
 * Created by synopia on 21.09.2015.
 */
public abstract class AudioCommand implements Comparable<AudioCommand> {
    private AudioParameter target;
    private int position;

    public AudioCommand(AudioParameter target, int position) {
        this.target = target;
        this.position = position;
    }

    public AudioCommand(AudioParameter target) {
        this(target, 0);
    }

    public AudioParameter getTarget() {
        return target;
    }

    public int getPosition() {
        return position;
    }

    void setPosition(int position) {
        this.position = position;
    }

    public abstract void process(AudioNode audioNode);

    @Override
    public int compareTo(AudioCommand o) {
        return Integer.compare(position, o.position);
    }
}
