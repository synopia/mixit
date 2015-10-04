package org.synopia.audio.model.keyfinder;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.ConstantQ;
import org.synopia.audio.model.Clip;

/**
 * Created by synopia on 24.09.2015.
 */
public class SpectrumAnalyzer {
    public Chromagram buildChromagram(Clip clip) {
        ConstantQ constantQ = new ConstantQ(clip.getMonoFormat().getSampleRate(), 27.5f, (float) Math.pow(2, KeyFinder.OCTAVES) * 27.5f, 12);
        Chromagram chromagram = new Chromagram();
        AudioEvent event = new AudioEvent(clip.getMonoFormat());

        clip.toStream(constantQ.getFFTlength(), constantQ.getFFTlength() / 4).forEach(buffer -> {
            event.setFloatBuffer(buffer);
            constantQ.process(event);
            float[] magnitudes = constantQ.getMagnitudes();
            float[] b = new float[magnitudes.length];
            System.arraycopy(magnitudes, 0, b, 0, b.length);
            chromagram.append(b);
        });

        return chromagram;
    }
}
