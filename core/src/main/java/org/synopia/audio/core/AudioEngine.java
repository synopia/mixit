package org.synopia.audio.core;

import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.synopia.shadow.ShadowFactory;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Consumer;

/**
 * Created by synopia on 22/09/15.
 */
public class AudioEngine extends Patch {
    private static final Logger logger = LogManager.getLogger(AudioEngine.class);
    private AudioOut audioOut;
    private AudioOut audioOut2;
    private final PriorityBlockingQueue<AudioCommand> inputQueue = new PriorityBlockingQueue<>();
    private final PriorityBlockingQueue<AudioCommand> outputQueue = new PriorityBlockingQueue<>();
    private final TarsosDSPAudioFormat format;
    private int playPosition;
    private float bpm;
    private long lastTick;
    private ShadowFactory shadowFactory;
    private Map<Integer, List<Consumer>> beatConsumer = Collections.synchronizedMap(new HashMap<>());
    private int lastBeat;
    private boolean shutdown;

    public AudioEngine(ShadowFactory shadowFactory, float bpm) {
        super(null);
        this.shadowFactory = shadowFactory;
        this.bpm = bpm;
        audioOut = add(new AudioOut("master"));
//        audioOut2 = add(new AudioOut("cue"));
        format = new TarsosDSPAudioFormat(44100, 16, 1, true, false);
    }

    public void setBufferSize(int size) {
        setFormat(format, size);
    }

    public void runInSync(int beat, Consumer<Integer> consumer) {
        List<Consumer> consumers = beatConsumer.get(beat);
        if (consumers == null) {
            consumers = new ArrayList<>();
            beatConsumer.put(beat, consumers);
        }
        consumers.add(consumer);
    }

    public void startProcessing() {
        Thread thread = new Thread(() -> {
            playPosition = 0;
            while (!shutdown) {
                try {
                    int currentBeat = (int) (playPosition / format.getSampleRate() * bpm / 60);

                    if (currentBeat != lastBeat) {
                        logger.trace("Start playing at beat {} playPos {} ", currentBeat, playPosition);
                        Set<Integer> consumed = new HashSet<>();
                        Set<Map.Entry<Integer, List<Consumer>>> entries = new HashSet<>(beatConsumer.entrySet());
                        for (Map.Entry<Integer, List<Consumer>> entry : entries) {
                            if ((currentBeat % entry.getKey()) == 0) {
                                for (Consumer consumer : entry.getValue()) {
                                    consumer.accept(currentBeat);
                                }
                                consumed.add(entry.getKey());
                            }
                        }
                        for (Integer integer : consumed) {
                            beatConsumer.remove(integer);
                        }
                    }
                    shadowFactory.apply();

                    PriorityQueue<AudioCommand> commands = new PriorityQueue<>(inputQueue);
                    process(playPosition);
                    playPosition += audioOut.getSamplesProcessed();
                    lastTick = System.currentTimeMillis();
                    if (!commands.isEmpty()) {
                        int topPos = commands.peek().getPosition();
                        while (topPos < playPosition) {
                            inputQueue.remove(commands.poll());
                            topPos = !commands.isEmpty() ? commands.peek().getPosition() : playPosition;
                        }
                    }
                    lastBeat = currentBeat;

                } catch (Exception e) {
                    e.printStackTrace();
                }
                Thread.yield();
            }
            audioOut.stop();

        }, "audio");
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    public AudioOut getAudioOut() {
        return audioOut;
    }

    public void stop() {
        shutdown = true;
    }

//    public AudioOut getAudioOut2() {
//        return audioOut2;
//    }
}
