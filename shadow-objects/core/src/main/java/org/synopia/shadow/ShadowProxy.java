package org.synopia.shadow;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

/**
 * Created by synopia on 27.09.2015.
 */
public class ShadowProxy implements InvocationHandler {
    private Map<String, Object> content = new HashMap<>();
    private Map<String, Map<Object, Consumer>> listeners = new HashMap<>();
    private Set<String> changed = new HashSet<>();

    private ShadowObject delegate;
    private final ShadowFactory factory;

    public ShadowProxy(ShadowFactory factory, ShadowObject delegate) {
        this.factory = factory;
        this.delegate = delegate;
    }

    public ShadowObject getDelegate() {
        return delegate;
    }

    public void reload() {
        ShadowFactory.ManagedClass mc = factory.getManagedClass(delegate.getClass());
        mc.getFields().forEach((name, field) -> {
            Object newValue = field.get(delegate);
            if (newValue == null && content.containsKey(name)) {
                content.remove(name);
                fire(name, null);
            } else if (newValue != null) {
                Object old = content.get(name);
                if (changed.contains(name) || !newValue.equals(old)) {
                    content.put(name, newValue);
                    fire(name, newValue);
                }
            }
        });
        mc.getLists().forEach((name, list) -> {
            List old = (List) content.get(name);
            List arr = new ArrayList();
            boolean changed;
            if (old == null) {
                changed = true;
                for (int i = 0; i < list.size(delegate); i++) {
                    Object newValue = list.get(delegate, i);
                    arr.add(newValue);
                }
            } else {
                changed = false;
                for (int i = 0; i < list.size(delegate); i++) {
                    Object newValue = list.get(delegate, i);
                    if (old.get(i) != newValue) {
                        changed = true;
                    }
                    arr.add(newValue);
                }
            }
            if (changed) {
                content.put(name, arr);
                fire(name, arr);
            }
        });
    }

    public void apply() {
        ShadowFactory.ManagedClass mc = factory.getManagedClass(delegate.getClass());
        for (String s : changed) {
            ShadowFactory.ManagedField field = mc.getField(s);
            Object value = content.get(s);
            if (field != null) {
                if (field.get(delegate) != value) {
                    if (value instanceof ShadowObject) {
                        ShadowProxy proxyNode = factory.getProxyNode((ShadowObject) value);
                        if (proxyNode != null) {
                            field.set(delegate, proxyNode.delegate);
                        } else {
                            field.set(delegate, value);
                        }
                    } else {
                        field.set(delegate, value);
                    }
                }
            } else {
                ShadowFactory.ManagedList list = mc.getList(s);
                List arr = (List) value;
                for (int i = 0; i < arr.size(); i++) {
                    Object v = arr.get(i);
                    if (list.get(delegate, i) != v) {
                        if (v instanceof ShadowObject) {
                            ShadowProxy proxyNode = factory.getProxyNode((ShadowObject) value);
                            if (proxyNode != null) {
                                list.set(delegate, i, proxyNode.delegate);

                            } else {
                                list.set(delegate, i, v);
                            }
                        } else {
                            list.set(delegate, i, v);
                        }
                    }
                }
            }
        }
        reload();
        changed.clear();
    }

    public <T> void onChange(Object subscriber, String field, Consumer<T> consumer) {
        Map<Object, Consumer> consumers = listeners.get(field);
        if (consumers == null) {
            consumers = new HashMap<>();
            listeners.put(field, consumers);
        }
        consumers.put(subscriber, consumer);
    }

    public void put(String field, Object value) {
        content.put(field, value);
        changed.add(field);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("equals")) {
            return this.equals(args[0]);
        }
        ShadowFactory.ManagedClass mc = factory.getManagedClass(delegate.getClass());
        String fieldName = mc.getManagedFieldFor(method.getName());
        if (fieldName == null) {
            return method.invoke(delegate, args);
        }
        String methodName = method.getName();
        boolean getter = methodName.startsWith("get");
        boolean count = methodName.endsWith("Count");
        if (count) {
            return mc.getList(fieldName).size(delegate);
        } else {
            if (mc.getField(fieldName) != null) {
                if (getter) {
                    return content.get(fieldName);
                } else {
                    changed.add(fieldName);
                    content.put(fieldName, args[0]);
//                    fire(fieldName, args[0]);
                    return null;
                }
            } else if (mc.getList(fieldName) != null) {
                List list = (List) content.get(fieldName);
                if (getter) {
                    return list.get((int) args[0]);
                } else {
                    changed.add(fieldName);
//                    fire(fieldName, list);
                    list.set((int) args[0], args[1]);
                    return null;
                }
            } else {
                return method.invoke(delegate, args);
            }
        }
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ShadowObject) {
            ShadowProxy proxyNode = factory.getProxyNode((ShadowObject) obj);
            if (proxyNode != null) {
                return delegate.equals(proxyNode.getDelegate());
            }
        }
        return delegate.equals(obj);
    }

    private void fire(String fieldName, Object value) {
        Map<Object, Consumer> consumers = listeners.get(fieldName);
        if (consumers != null) {
            for (Consumer consumer : consumers.values()) {
                consumer.accept(value);
            }
        }
    }
}
