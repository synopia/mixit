package org.synopia.audio.core;

import be.tarsos.dsp.io.TarsosDSPAudioFormat;

import java.util.*;

/**
 * Created by synopia on 19.09.2015.
 */
public class Patch extends BaseHasOutput implements HasInput {
    private List<AudioNode> nodes = new ArrayList<>();
    private HasOutput input;

    public Patch(Patch parent) {
        super(parent);
    }

    @Override
    public void process(int samplesPos) {
        nodes.forEach(AudioNode::start);
        Queue<AudioNode> queue = new ArrayDeque<>(nodes);
        AudioNode node = null;
        while (!queue.isEmpty()) {
            node = queue.poll();
            if (node.canFinish()) {
                node.process(samplesPos);
                node.finish();
            } else {
                queue.offer(node);
            }
        }
        if (node != null && node instanceof HasOutput) {
            float[] output = ((HasOutput) node).output();
            int bufferSize = output.length;
            if (outputBuffer == null || outputBuffer.length != bufferSize) {
                outputBuffer = new float[bufferSize];
            }
            System.arraycopy(output, 0, outputBuffer, 0, bufferSize);
        }
    }

    public <T extends AudioNode> T add(T node) {
        nodes.add(node);
        return node;
    }

    public void connect(HasOutput output, HasInput input) {
        input.addInput(output);
    }

    @Override
    public void addInput(HasOutput input) {
        this.input = input;
    }

    @Override
    public List<HasOutput> inputs() {
        return Arrays.asList(input);
    }

    @Override
    public void setFormat(TarsosDSPAudioFormat format, int bufferSize) {
        super.setFormat(format, bufferSize);
        for (AudioNode node : nodes) {
            node.setFormat(format, bufferSize);
        }
    }

    @Override
    public boolean canFinish() {
        return input == null || input.finished();
    }

}
