package org.synopia.audio.model;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.WaveformSimilarityBasedOverlapAdd;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.resample.RateTransposer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.synopia.audio.core.BaseHasOutput;
import org.synopia.audio.core.Patch;
import org.synopia.shadow.Parameter;

import java.util.Arrays;

/**
 * Created by synopia on 19.09.2015.
 */
public class ClipPlayer extends BaseHasOutput implements AClipPlayer {
    private final static Logger logger = LogManager.getLogger(ClipPlayer.class);
    @Parameter
    private ClipPlayerType type = ClipPlayerType.RESAMPLER;
    private Player player = new ResamplerPlayer();
    private AudioEvent event;
    private Clip clip;
    @Parameter
    private int position;
    @Parameter
    private boolean playing;

    private float playerInput[];
    private float playerOutput[];
    private int posInOutput;
    @Parameter
    private boolean notchBack;
    @Parameter
    private boolean notchForward;
    @Parameter
    private float speed = 1;

    private int outputBufferSize;
    private Float newSpeed;

    public ClipPlayer(Patch parent) {
        super(parent);
    }

    protected void setPlayerSpeed(float speed) {
        logger.trace("set new player speed {} posInOutput {} position {}", speed, posInOutput, position);
        speed = Math.min(Math.max(speed, 0.5f), 2);
        newSpeed = speed;
    }

    public void setClip(Clip clip) {
        this.clip = clip;
        player.setSpeed(speed);
    }

    public Clip getClip() {
        return clip;
    }

    @Override
    public void setFormat(TarsosDSPAudioFormat format, int bufferSize) {
        super.setFormat(format, bufferSize);
        event = new AudioEvent(format);
        player.setFormat(format, bufferSize);
    }

    @Override
    public boolean canFinish() {
        return true;
    }

    private int lastPosition;

    private void readNextWsola() {
        if (playing && clip.getMonoSamples() != null) {
            if (newSpeed != null) {
                float actualSpeed = newSpeed;
                if (notchBack) {
                    actualSpeed *= 0.99f;
                }
                if (notchForward) {
                    actualSpeed *= 1.01f;
                }

                logger.info("New speed {} (nudging {})", newSpeed, actualSpeed);
                player.setSpeed(actualSpeed);
                speed = newSpeed;
                newSpeed = null;
            }
            logger.trace("Reading buffer from input posInOutput {} position {} length {} posStep {} speed {} delta pos {}", posInOutput, position, playerInput.length, player.getPositionSteps(), speed, position - lastPosition);

            float[] samples = clip.getMonoSamples();
            System.arraycopy(samples, position, playerInput, 0, playerInput.length);
            lastPosition = position;

            position += player.getPositionSteps();
        } else {
            Arrays.fill(playerInput, 0, playerInput.length, 0);
        }
        event.setFloatBuffer(playerInput);
        player.process(event);
        playerOutput = event.getFloatBuffer();
        posInOutput = 0;
        outputBufferSize = playerOutput.length;
    }

    @Override
    public void process(int samplesPos) {
        int offs = 0;
        int length = getBufferSize();
        if (playerOutput == null || posInOutput >= playerOutput.length) {
            readNextWsola();
        }
        while (length > 0) {
            if (posInOutput + length < playerOutput.length) {
                if (playing) {
                    logger.trace("Copy player output from {} (max {}) to {} len {}", posInOutput, playerOutput.length, offs, length);
                }
                System.arraycopy(playerOutput, posInOutput, outputBuffer, offs, length);
                posInOutput += length;
                length = 0;
            } else {
                int len = playerOutput.length - posInOutput;
                if (playing) {
                    logger.trace("Copy player output from {} (max {}) to {} len {}", posInOutput, playerOutput.length, offs, len);
                }
                System.arraycopy(playerOutput, posInOutput, outputBuffer, offs, len);
                offs += len;
                length -= len;
                posInOutput += len;
                readNextWsola();
            }
        }
    }

    @Override
    public void setType(ClipPlayerType type) {
        this.type = type;
        switch (type) {
            case RESAMPLER:
                player = new ResamplerPlayer();
                break;
            case WSOLA:
                player = new WsolaPlayer();
                break;
        }
        player.setFormat(format, getBufferSize());
        playerOutput = null;
    }

    @Override
    public void setPosition(int position) {
        this.position = position - posInOutput;
        playerOutput = null;
        playing = true;
    }

    @Override
    public void setPlaying(boolean playing) {
        this.playing = playing;
        playerOutput = null;
    }

    @Override
    public void setSpeed(float speed) {
        this.speed = speed;
        if (!player.ready() || playerInput == null) {
            return;
        }
        setPlayerSpeed(speed);
    }

    @Override
    public void setNotchBack(boolean notchBack) {
        this.notchBack = notchBack;
        if (!player.ready()) {
            return;
        }
        setPlayerSpeed(speed);
    }

    @Override
    public void setNotchForward(boolean notchForward) {
        this.notchForward = notchForward;
        if (!player.ready()) {
            return;
        }
        setPlayerSpeed(speed);
    }

    @Override
    public ClipPlayerType getType() {
        return type;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public boolean getPlaying() {
        return playing;
    }

    public int getPosInOutput() {
        return posInOutput;
    }

    @Override
    public boolean getNotchBack() {
        return notchBack;
    }

    @Override
    public boolean getNotchForward() {
        return notchForward;
    }

    @Override
    public float getSpeed() {
        return speed;
    }


    public interface Player {
        boolean ready();

        void setSpeed(float speed);

        void setFormat(TarsosDSPAudioFormat format, int bufferSize);

        void process(AudioEvent event);

        int getPositionSteps();
    }

    public class WsolaPlayer implements Player {
        private WaveformSimilarityBasedOverlapAdd wsola;

        @Override
        public boolean ready() {
            return wsola != null;
        }

        @Override
        public void setSpeed(float speed) {
            wsola.setParameters(WaveformSimilarityBasedOverlapAdd.Parameters.musicDefaults(speed, 44100));
//            setFormat(format, getBufferSize());
        }

        @Override
        public void setFormat(TarsosDSPAudioFormat format, int bufferSize) {
            wsola = new WaveformSimilarityBasedOverlapAdd(WaveformSimilarityBasedOverlapAdd.Parameters.musicDefaults(speed, 44100));
            wsola.setCallback((inputBufferSize, overlap) -> {
                playerInput = new float[wsola.getInputBufferSize()];
            });
            playerInput = new float[wsola.getInputBufferSize()];
        }

        @Override
        public void process(AudioEvent event) {
            wsola.process(event);
        }

        @Override
        public int getPositionSteps() {
            return playerInput.length - wsola.getOverlap();
        }
    }

    public class ResamplerPlayer implements Player {
        private RateTransposer transposer;

        @Override
        public boolean ready() {
            return transposer != null;
        }

        @Override
        public void setSpeed(float speed) {
            transposer.setFactor(1 / speed);
        }

        @Override
        public void setFormat(TarsosDSPAudioFormat format, int bufferSize) {
            transposer = new RateTransposer(1 / speed);
            playerInput = new float[bufferSize];
        }

        @Override
        public void process(AudioEvent event) {
            transposer.process(event);
        }

        @Override
        public int getPositionSteps() {
            return playerInput.length;
        }
    }
}
