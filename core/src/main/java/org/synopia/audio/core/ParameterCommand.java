package org.synopia.audio.core;

/**
 * Created by synopia on 22/09/15.
 */
public class ParameterCommand extends AudioCommand {
    private Object value;

    public ParameterCommand(AudioParameter parameter, Object value) {
        super(parameter);
        this.value = value;
    }

    @Override
    public void process(AudioNode audioNode) {
        if (getTarget().getNode() == audioNode) {
            getTarget().set(value);
        }
    }
}
