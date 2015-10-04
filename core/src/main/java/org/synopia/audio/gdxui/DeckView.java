package org.synopia.audio.gdxui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.synopia.audio.model.AClip;
import org.synopia.audio.model.ADeck;
import org.synopia.audio.model.ATrack;
import org.synopia.audio.model.ClipPlayerType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by synopia on 26.09.2015.
 */
public class DeckView extends VisTable {
    private static final Logger logger = LogManager.getLogger(DeckView.class);

    private ADeck deck;
    private BeatGrid beatGrid = new BeatGrid();
    private Binding.Subscription subscription;
    private VisTextField bpmBox;

    public DeckView(ADeck deck, Binding binding, boolean left) {
        this.deck = deck;
        subscription = binding.create(this);
        LayerView layerView = new LayerView(500, 200);
        layerView.setZoomable(true);
        layerView.setFollowPlayer(true);
        layerView.setShowPlayerPos(false);
        layerView.setBeatGrid(beatGrid);

        LayerView miniMap = new LayerView(500, 130);
        miniMap.setZoomable(false);
        miniMap.setFollowPlayer(false);
        miniMap.setShowPlayerPos(true);
        miniMap.setBeatGrid(beatGrid);

        layerView.addListener(event -> {
            if (event instanceof LayerView.BeatClicked) {
                LayerView.BeatClicked clicked = (LayerView.BeatClicked) event;
                logger.info("Start playing at {}", clicked.currentBeat.text);
                binding.runInSync(4, beat -> deck.getClipPlayer().setPosition((int) (clicked.currentBeat.time * 44100)));
                return true;
            }
            return false;
        });
        subscription.onChange(deck, "track", new Consumer<ATrack>() {
            @Override
            public void accept(ATrack track) {
                layerView.setTrack(track);
                miniMap.setTrack(track);
                float bpm = track.getBpm();
                if (bpm == 0) {
                    bpm = track.getCalculatedBpm();
                }
                if (bpm == 0) {
                    bpm = track.getTagBpm();
                }
                deck.setBpm(bpm);
                beatGrid.setBpm(bpm);
                bpmBox.setText(bpm + "");
                beatGrid.setOriginTime(track.getFirstBeat());

                subscription.onChange(track, "bpm", new Consumer<Float>() {
                    @Override
                    public void accept(Float bpm) {
                        deck.setBpm(bpm);
                        beatGrid.setBpm(bpm);
                        bpmBox.setText(bpm.toString());
                    }
                });
                subscription.onChange(track, "firstBeat", new Consumer<Float>() {
                    @Override
                    public void accept(Float firstBeat) {
                        beatGrid.setOriginTime(firstBeat);
                    }
                });
            }
        });
        subscription.onChange(deck, "clip", new Consumer<AClip>() {
            @Override
            public void accept(AClip clip) {
                layerView.setClip(clip);
                layerView.setClipPlayer(deck.getClipPlayer());
                miniMap.getTimeWindow().length = clip.getFrames() / 44100f;
                miniMap.getTimeWindow().start = 0;
                miniMap.setClip(clip);
                miniMap.setClipPlayer(deck.getClipPlayer());

                beatGrid.setFrames(clip.getFrames());
                beatGrid.setSampleRate(clip.getSampleRate());
            }
        });

        List<Actor> horiz = new ArrayList<>();

        VisSlider.VisSliderStyle style = new VisSlider.VisSliderStyle(Ui.getDrawable("tempo_back"), Ui.getDrawable("tempo_knob"));
        style.knobDown = style.knob;
        style.knobOver = style.knob;
        VisSlider pitch = new VisSlider(0.96f, 1.04f, 0.001f, true, style);


        horiz.add(subscription.bind(deck.getClipPlayer(), "speed", pitch));

        VisTable ctrl = new VisTable();
        bpmBox = new VisTextField("170");
        ctrl.add(bpmBox);
        ctrl.add(subscription.bind(deck.getClipPlayer(), "type", new VisSelectBox<>(), new Array<>(ClipPlayerType.values())));
        ctrl.add(subscription.bind(deck.getClipPlayer(), "notchBack", new Button(Ui.getButtonStyle(Ui.ButtonType.REWIND)), true));
        ctrl.add(subscription.bind(deck.getClipPlayer(), "playing", new Button(Ui.getButtonStyle(Ui.ButtonType.PLAY))));
        ctrl.add(subscription.bind(deck.getClipPlayer(), "notchForward", new Button(Ui.getButtonStyle(Ui.ButtonType.FORWARD)), true));
        ctrl.add(new Button(Ui.getButtonStyle(Ui.ButtonType.STOP)));
        ctrl.add();


        VisTable players = new VisTable();
        players.add(layerView).row();
        players.add(ctrl).row();
        players.add(miniMap);
        horiz.add(players);

        VisTable eq = new VisTable();
        eq.add(subscription.bind(deck.getGain(), "gain", new RotaryKnob("Gain", 0, 1, 0.01f))).row();
        eq.add(subscription.bind(deck.getEq(), "highGain", new RotaryKnob("High", 0, 1, 0.01f))).row();
        eq.add(subscription.bind(deck.getEq(), "midGain", new RotaryKnob("Mid", 0, 1, 0.01f))).row();
        eq.add(subscription.bind(deck.getEq(), "lowGain", new RotaryKnob("Low", 0, 1, 0.01f))).row();
        horiz.add(eq);

        if (!left) {
            Collections.reverse(horiz);
        }
        for (Actor actor : horiz) {
            add(actor).fillY();
        }

        add().row();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (deck != null) {
            if (!beatGrid.isValid()) {
                beatGrid.recalculate();
            }
        }
        super.draw(batch, parentAlpha);
    }
}
