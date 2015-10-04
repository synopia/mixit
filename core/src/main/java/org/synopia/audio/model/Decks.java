package org.synopia.audio.model;

import org.synopia.audio.core.AudioNode;
import org.synopia.audio.core.Mixer;
import org.synopia.audio.core.Patch;
import org.synopia.shadow.Parameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by synopia on 19.09.2015.
 */
public class Decks extends Patch implements ADecks {
    @Parameter
    private List<Deck> decks = new ArrayList<>();
    @Parameter
    private float bpm = 170f;

    @Parameter
    private Mixer mixer;
    @Parameter
    private Mixer cueMixer;

    public Decks(Patch parent) {
        super(parent);

        mixer = new Mixer(this);
        cueMixer = new Mixer(this);
    }

    @Override
    public ADeck getDecks(int index) {
        return decks.get(index);
    }

    @Override
    public int decksCount() {
        return decks.size();
    }

    @Override
    public void setDecks(int index, ADeck decks) {
        this.decks.set(index, (Deck) decks);
    }

    @Override
    public <T extends AudioNode> T add(T node) {
        T add = super.add(node);
        if (add instanceof Deck) {
            Deck deck = (Deck) add;
            decks.add(deck);
            connect(deck, mixer);
            connect(deck, cueMixer);

            cueMixer.setGains(decks.size() - 1, 0f);
        }
        return add;
    }

    @Override
    public Mixer getMixer() {
        return mixer;
    }

    @Override
    public Mixer getCueMixer() {
        return cueMixer;
    }

    @Override
    public float getBpm() {
        return bpm;
    }

    @Override
    public void setBpm(float bpm) {
        this.bpm = bpm;
        for (Deck deck : decks) {
            deck.getClipPlayer().setSpeed(bpm / deck.getBpm());
        }
    }

    @Override
    public void setCueMixer(AMixer cueMixer) {
        this.cueMixer = (Mixer) cueMixer;
    }

    @Override
    public void setMixer(AMixer mixer) {
        this.mixer = (Mixer) mixer;
    }
}
