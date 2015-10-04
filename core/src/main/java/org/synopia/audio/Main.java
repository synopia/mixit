package org.synopia.audio;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.synopia.audio.core.AudioEngine;
import org.synopia.audio.gdxui.Binding;
import org.synopia.audio.gdxui.DeckView;
import org.synopia.audio.gdxui.LibraryView;
import org.synopia.audio.gdxui.Ui;
import org.synopia.audio.model.*;
import org.synopia.shadow.ShadowFactory;

public class Main extends ApplicationAdapter {
    private static final Logger logger = LogManager.getLogger(Main.class);
    private Stage stage;
    private MenuBar menuBar;
    private Binding binding;
    private TrackDatabase db;
    private AudioEngine engine;

    @Override
    public void create() {
        logger.info("Starting...");
        db = new TrackDatabase();
        db.load();
//        Track track1 = db.get("e:/case/lib3/2015.5/Fast Soul Music/01-09- Waves Breaking_pn.mp3");
//        Track track2 = db.get("e:/case/lib3/2015.5/Fast Soul Music/01-09- Waves Breaking_pn.mp3");
//        Track track2 = db.get("e:/case/lib3/2015.5/Fast Soul Music/01-45- Black Diamonds_pn.mp3");
//        db.analyze(track1);
//        db.analyze(track2);
        VisUI.load(VisUI.SkinScale.X1);
        Ui.load();

        ScreenViewport viewport = new ScreenViewport();
        stage = new Stage(viewport);

        Table root = new Table();
        root.setFillParent(true);
        root.setBackground(new TiledDrawable(Ui.getRegion("background")));
        stage.addActor(root);

        Gdx.input.setInputProcessor(stage);

        menuBar = new MenuBar();
        root.add(menuBar.getTable()).top().expandX().fillX().row();

        Menu fileMenu = new Menu("File");
        fileMenu.addItem(new MenuItem("Exit"));
        menuBar.addMenu(fileMenu);
        ShadowFactory factory = new ShadowFactory();
        engine = new AudioEngine(factory, 170);
        Decks decks = new Decks(null);
        Deck deck = new Deck(db, decks);
        Deck deck1 = new Deck(db, decks);
        engine.add(decks);
        engine.connect(decks.getMixer(), engine.getAudioOut());
//        engine.connect(decks.getCueMixer(), engine.getAudioOut2());

        engine.setBufferSize(512);
        engine.startProcessing();

        ADecks decks2 = factory.createProxy(ADecks.class, decks);
        ADeck deckA = decks2.getDecks(0);
        ADeck deckB = decks2.getDecks(1);

        binding = new Binding(engine, factory);
        DeckView deckView = new DeckView(deckA, binding, true);
        DeckView deckView1 = new DeckView(deckB, binding, false);

//        root.add(binding.bind(decks.getBpm(), new VisSlider(100,200,0.5f, false))).row();

        root.add(deckView).left();
        root.add(deckView1).right();
        root.row();
        root.add(new VisScrollPane(new LibraryView(db))).colspan(3).row();
        root.add().growY();
//        deck.setTrack(track1);
//        deck1.setTrack(track2);
//        deck.setBpm(170);
//        db.load(track1).whenComplete((clip, e)->{
//            deck.setClip(clip);
//            deck.getClipPlayer().setPlaying(true);
//        });

        deckA.setTrack(db.all().get(0));
        deckB.setTrack(db.all().get(3));
        root.pack();
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void dispose() {
        engine.stop();
        db.save();
        VisUI.dispose();
        stage.dispose();
    }
}
