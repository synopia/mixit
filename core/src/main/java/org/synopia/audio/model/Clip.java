package org.synopia.audio.model;

import be.tarsos.dsp.io.*;
import org.synopia.audio.core.Band3Eq;
import org.synopia.audio.core.BufferWrapper;
import org.synopia.shadow.ReadonlyParameter;

import java.io.IOException;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by synopia on 18.09.2015.
 */
public class Clip implements AClip {
    @ReadonlyParameter
    private float[] samples;
    @ReadonlyParameter
    private float[] monoSamples;
    @ReadonlyParameter
    private byte[][] downSampled;

    @ReadonlyParameter
    private TarsosDSPAudioFormat format;
    @ReadonlyParameter
    private TarsosDSPAudioFormat monoFormat;

    private TarsosDSPAudioFloatConverter converter;

    @ReadonlyParameter
    private int frames;
    @ReadonlyParameter
    private float sampleRate;
    @ReadonlyParameter
    private boolean loaded;

    public float[] getSamples() {
        return samples;
    }

    public float[] getMonoSamples() {
        return monoSamples;
    }

    public byte[] getDownSampled(int band) {
        return downSampled[band];
    }

    public byte[][] getDownSampled() {
        return downSampled;
    }

    @Override
    public int getFrames() {
        return monoSamples.length;
    }

    public TarsosDSPAudioFormat getFormat() {
        return format;
    }

    public TarsosDSPAudioFormat getMonoFormat() {
        return monoFormat;
    }

    @Override
    public float getSampleRate() {
        return monoFormat == null ? 44100 : monoFormat.getSampleRate();
    }

    @Override
    public boolean getLoaded() {
        return loaded;
    }

    public Stream<float[]> toStream(int bufferSize, int hopSize) {
        int len = monoSamples.length / hopSize;
        return IntStream.rangeClosed(0, len).mapToObj(new IntFunction<float[]>() {
            int pos = 0;

            @Override
            public float[] apply(int bufferIndex) {
                float[] buffer = new float[bufferSize];
                int toCopy = Math.min(monoSamples.length - pos, bufferSize);
                System.arraycopy(monoSamples, pos, buffer, 0, toCopy);
                pos += hopSize;
                return buffer;
            }
        });
    }

    public void load(String filename) throws IOException {
        TarsosDSPAudioInputStream stream = new PipedAudioStream(filename).getStereoStream(44100);
        format = stream.getFormat();
        converter = TarsosDSPAudioFloatConverter.getConverter(format);
        if (converter != null) {
            int bytesPerSample = format.getSampleSizeInBits() / 8;
            int bufferSize = 4096;
            int pos = 0;
            byte[] bytes = new byte[bufferSize];
            while (true) {
                int read = stream.read(bytes, 0, bytes.length);
                if (read == -1) {
                    break;
                }
                int samplesRead = read / bytesPerSample;
                ensureBuffer(pos + samplesRead);
                converter.toFloatArray(bytes, samples, pos, samplesRead);
                pos += samplesRead;
            }

            float finalSamples[] = new float[pos];
            System.arraycopy(samples, 0, finalSamples, 0, pos);
            samples = finalSamples;

            createMonoSamples();
            createDownSampled(256);
            loaded = true;
        }
    }

    private void createDownSampled(int hops) {
        Band3Eq eq = new Band3Eq(null);
        eq.setFormat(getMonoFormat(), hops);
        int length = (int) (monoSamples.length / format.getSampleRate() * hops) + 1;
        downSampled = new byte[3][length];
        int pos[] = new int[]{0};
        eq.addInput(new BufferWrapper(new float[2048]));
        eq.setLowFreq(600f);
        eq.setHighFreq(4000f);
        eq.process(0);
        toStream(hops, hops).forEach(buffer -> {
            MinMax mmLow = new MinMax();
            MinMax mmMid = new MinMax();
            MinMax mmHigh = new MinMax();
            for (int i = 0; i < buffer.length; i++) {
                eq.do3Band(buffer[i]);
                double high = eq.getLastHigh();
                double mid = eq.getLastMid();
                double low = eq.getLastLow();
                mmLow.nextSample((float) low);
                mmMid.nextSample((float) mid);
                mmHigh.nextSample((float) high);
            }
            double l = Math.max(Math.abs(mmLow.min()), mmLow.max());
            double m = Math.max(Math.abs(mmMid.min()), mmMid.max());
            double h = Math.max(Math.abs(mmHigh.min()), mmHigh.max());
            downSampled[0][pos[0]] = (byte) (l * Byte.MAX_VALUE);
            downSampled[1][pos[0]] = (byte) (m * Byte.MAX_VALUE);
            downSampled[2][pos[0]] = (byte) (h * Byte.MAX_VALUE);
            pos[0]++;
        });
    }


    private void createMonoSamples() {
        monoFormat = new TarsosDSPAudioFormat(format.getEncoding(), format.getSampleRate(), format.getSampleSizeInBits(), 1, format.getFrameSize() / format.getChannels(), format.getSampleRate(), format.isBigEndian());
        monoSamples = new float[samples.length / format.getChannels()];
        for (int i = 0; i < monoSamples.length; i++) {
            float sum = 0;
            for (int j = 0; j < format.getChannels(); j++) {
                float sample = samples[i * format.getChannels() + j];
                sum += sample;
            }
            monoSamples[i] = sum / format.getChannels();
        }
    }

    private void ensureBuffer(int minSize) {
        if (samples != null && samples.length > minSize) {
            return;
        }
        int oldLength = 0;
        if (samples != null) {
            oldLength = samples.length;
        } else {
            oldLength = minSize;
        }
        int newLength = oldLength * 2;
        while (newLength < minSize) {
            newLength *= 2;
        }
        float[] newSamples = new float[newLength];
        if (samples != null) {
            System.arraycopy(samples, 0, newSamples, 0, oldLength);
        }
        samples = newSamples;
    }

    public boolean isLoaded() {
        return loaded;
    }
}
