package org.synopia.audio.core;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.jvm.AudioPlayer;
import be.tarsos.dsp.writer.WriterProcessor;

import javax.sound.sampled.LineUnavailableException;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;

/**
 * Created by synopia on 19.09.2015.
 */
public class AudioOut implements HasInput {
    private HasOutput input;
    private AudioPlayer player;
    private AudioEvent event;
    private boolean finished = false;
    private int samplesProcessed;
    private String name;
    private WriterProcessor writer;

    public AudioOut(String name) {
        this.name = name;
    }

    @Override
    public Patch parent() {
        return null;
    }

    @Override
    public void setFormat(TarsosDSPAudioFormat format, int bufferSize) {
        input.setFormat(format, bufferSize);

        try {
            player = new AudioPlayer(format, bufferSize * 4);
            event = new AudioEvent(format);
            writer = new WriterProcessor(format, new RandomAccessFile("out.wav", "rw"));
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addInput(HasOutput input) {
        this.input = input;
    }

    @Override
    public boolean canFinish() {
        return input.finished();
    }

    @Override
    public boolean finished() {
        return finished;
    }

    @Override
    public void finish() {
        finished = true;
    }

    @Override
    public void start() {
        finished = false;
    }

    @Override
    public void process(int samplesPos) {
        float[] buffer = input.output();
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = Math.min(1, Math.max(-1, buffer[i]));
        }
        event.setFloatBuffer(buffer);
        player.process(event);
        event.setFloatBuffer(buffer);
        writer.process(event);
        samplesProcessed = buffer.length;
    }

    @Override
    public List<HasOutput> inputs() {
        return Arrays.asList(input);
    }

    @Override
    public TarsosDSPAudioFormat getFormat() {
        return input.getFormat();
    }

    public int getSamplesProcessed() {
        return samplesProcessed;
    }

    public void stop() {
        writer.processingFinished();
        player.processingFinished();
    }
}
