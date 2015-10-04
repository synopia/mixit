package org.synopia.audio.gdxui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.synopia.audio.core.AudioEngine;
import org.synopia.shadow.ShadowFactory;
import org.synopia.shadow.ShadowObject;
import org.synopia.shadow.ShadowProxy;

import java.util.function.Consumer;

/**
 * Created by synopia on 26.09.2015.
 */
public class Binding {
    private static final Logger logger = LogManager.getLogger(Binding.class);
    private AudioEngine engine;
    private ShadowFactory shadowFactory;

    public Binding(AudioEngine engine, ShadowFactory shadowFactory) {
        this.engine = engine;
        this.shadowFactory = shadowFactory;
    }

    public Subscription create(Object subscriber) {
        return new Subscription(subscriber);
    }

    public void runInSync(int beat, Consumer<Integer> consumer) {
        engine.runInSync(beat, consumer);
    }

    public class Subscription {
        private Object subscriber;

        private Subscription(Object subscriber) {
            this.subscriber = subscriber;
        }

        public VisTextField bind(ShadowObject node, String field, VisTextField tf) {
            ShadowProxy proxyNode = shadowFactory.getProxyNode(node);
            ShadowFactory.ManagedClass mc = shadowFactory.getManagedClass(proxyNode.getDelegate().getClass());
            proxyNode.onChange(subscriber, field, new Consumer<Float>() {
                @Override
                public void accept(Float o) {
                    tf.setText(o.toString());
                }
            });
            tf.setText(mc.getField(field).get(proxyNode.getDelegate()).toString());
            tf.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    proxyNode.put(field, Float.parseFloat(tf.getText()));
                }
            });
            return tf;
        }

        public VisProgressBar bind(ShadowObject node, String field, VisProgressBar bar) {
            ShadowProxy proxyNode = shadowFactory.getProxyNode(node);
            ShadowFactory.ManagedClass mc = shadowFactory.getManagedClass(proxyNode.getDelegate().getClass());
            proxyNode.onChange(subscriber, field, new Consumer<Float>() {
                @Override
                public void accept(Float o) {
                    logger.trace("{}.{} -> slider {}", node, field, o);
                    bar.setValue(o);
                }
            });
            bar.setValue((Float) mc.getField(field).get(proxyNode.getDelegate()));
            bar.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    logger.trace("slider -> {}.{}= {} ", node, field, bar.getValue());
                    proxyNode.put(field, bar.getValue());
                }
            });
            return bar;
        }

        public Button bind(ShadowObject node, String field, Button button, boolean value) {
            ShadowProxy proxyNode = shadowFactory.getProxyNode(node);
            button.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    logger.trace("button changed {}.{} = true", node, field);
                    proxyNode.put(field, true);
                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    logger.trace("button changed {}.{} = false", node, field);
                    proxyNode.put(field, false);
                    super.touchUp(event, x, y, pointer, button);
                }
            });
            proxyNode.onChange(subscriber, field, new Consumer<Boolean>() {
                @Override
                public void accept(Boolean o) {
                    logger.trace("field changed {}.{} = new state: {}", node, field, o);
                    button.setChecked(o);
                }
            });
            return button;
        }

        public Button bind(ShadowObject node, String field, Button button) {
            ShadowProxy proxyNode = shadowFactory.getProxyNode(node);
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    logger.trace("button changed {}.{} = checked: {} down:", node, field, button.isChecked(), button.isPressed());
                    proxyNode.put(field, button.isChecked());
                }
            });
            proxyNode.onChange(subscriber, field, new Consumer<Boolean>() {
                @Override
                public void accept(Boolean o) {
                    logger.trace("field changed {}.{} = new state: {}", node, field, o);
                    button.setChecked(o);
                }
            });
            return button;
        }

        public <T> VisSelectBox<T> bind(ShadowObject node, String field, VisSelectBox<T> list, Array<T> items) {
            ShadowProxy proxyNode = shadowFactory.getProxyNode(node);
            ShadowFactory.ManagedClass mc = shadowFactory.getManagedClass(proxyNode.getDelegate().getClass());
            proxyNode.onChange(subscriber, field, new Consumer<Object>() {
                @Override
                public void accept(Object o) {
                    logger.trace("{}.{} -> selectbox {}", node, field, o);

                    list.setSelected((T) o);
                }
            });
            list.setItems(items);
            list.setSelected((T) mc.getField(field).get(proxyNode.getDelegate()));
            list.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    T selected = list.getSelected();
                    logger.trace("selected {}.{} = {}", node, field, selected);
                    proxyNode.put(field, selected);
                }
            });
            return list;
        }

        public <T> void onChange(ShadowObject node, String field, Consumer<T> consumer) {
            ShadowProxy proxyNode = shadowFactory.getProxyNode(node);
            proxyNode.onChange(subscriber, field, consumer);
        }
    }
}
