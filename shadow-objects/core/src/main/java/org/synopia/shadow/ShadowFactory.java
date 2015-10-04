package org.synopia.shadow;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by synopia on 27.09.2015.
 */
public class ShadowFactory {
    private Map<ShadowObject, ShadowProxy> proxies = new HashMap<>();
    private Map<Class, ManagedClass> classes = new HashMap<>();
    private Map<ShadowObject, ShadowObject> cache = new HashMap<>();

    public ShadowProxy getProxyNode(ShadowObject node) {
        ShadowProxy proxyNode = proxies.get(node);
        if (proxyNode == null) {
            createProxy(node);
            proxyNode = proxies.get(cache.get(node));
        }
        return proxyNode;
    }

    public void apply() {
        List<ShadowProxy> nodes = new ArrayList<>(proxies.values());
        nodes.forEach(ShadowProxy::apply);
    }

    public ManagedClass getManagedClass(Class<? extends ShadowObject> type) {
        ManagedClass managedClass = classes.get(type);
        if (managedClass == null) {
            managedClass = new ManagedClass(type);
            classes.put(type, managedClass);
        }
        return managedClass;
    }

    public <T extends ShadowObject> T createProxy(ShadowObject instance) {
        if (cache.containsKey(instance)) {
            return (T) cache.get(instance);
        }
        Class type = null;
        Class<?>[] interfaces = instance.getClass().getInterfaces();
        String typeName = instance.getClass().getName();
        typeName = typeName.substring(typeName.lastIndexOf('.') + 1);
        for (Class<?> anInterface : interfaces) {
            String name = anInterface.getName();
            name = name.substring(name.lastIndexOf('.') + 1);
            if (name.endsWith(typeName)) {
                type = anInterface;
                break;
            }
        }
        if (type == null) {
            return (T) instance;
        }
        return (T) createProxy(type, instance);
    }

    public <T extends ShadowObject> T createProxy(Class<T> type, T instance) {
        if (cache.containsKey(instance)) {
            return (T) cache.get(instance);
        }
        ShadowProxy node = new ShadowProxy(this, instance);
        ShadowObject proxy = (ShadowObject) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, node);
        node.reload();
        proxies.put(proxy, node);
        cache.put(instance, proxy);
        return (T) proxy;
    }

    public class ManagedClass {
        private Map<String, ManagedField> fields = new HashMap<>();
        private Map<String, ManagedList> lists = new HashMap<>();
        private Map<String, String> methods = new HashMap<>();

        public String getManagedFieldFor(String methodName) {
            return methods.get(methodName);
        }

        public Map<String, ManagedField> getFields() {
            return fields;
        }

        public Map<String, ManagedList> getLists() {
            return lists;
        }

        public ManagedField getField(String name) {
            return fields.get(name);
        }

        public ManagedList getList(String name) {
            return lists.get(name);
        }

        public ManagedClass(Class<? extends ShadowObject> type) {
            Map<String, Method> methods = new HashMap<>();
            for (Method method : type.getDeclaredMethods()) {
                methods.put(method.getName(), method);
            }
            for (Field field : type.getDeclaredFields()) {
                Parameter parameter = field.getAnnotation(Parameter.class);
                if (parameter != null) {
                    String name = field.getName();
                    String upcase = Character.toUpperCase(name.charAt(0)) + name.substring(1);
                    if (List.class.isAssignableFrom(field.getType())) {
                        Method getter = methods.get("get" + upcase);
                        Method setter = methods.get("set" + upcase);
                        Method size = methods.get(name + "Count");
                        ManagedList managedList = new ManagedList(getter, setter, size);
                        lists.put(name, managedList);
                        this.methods.put(getter.getName(), name);
                        this.methods.put(setter.getName(), name);
                        this.methods.put(size.getName(), name);
                    } else {
                        Method getter = methods.get("get" + upcase);
                        Method setter = methods.get("set" + upcase);
                        ManagedField managedField = new ManagedField(getter, setter);
                        fields.put(name, managedField);
                        this.methods.put(getter.getName(), name);
                        this.methods.put(setter.getName(), name);
                    }
                }
            }
        }
    }

    public abstract class ManagedElement {
        public Object createProxyOnDemand(Object value) {
            if (!(value instanceof ShadowProxy) && value instanceof ShadowObject) {
                value = createProxy((ShadowObject) value);
            }
            return value;
        }
    }

    public class ManagedField extends ManagedElement {
        protected Method getter;
        protected Method setter;

        public ManagedField(Method getter, Method setter) {
            this.getter = getter;
            this.setter = setter;
        }

        public void set(Object delegate, Object value) {
            try {
                setter.invoke(delegate, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        public Object get(Object delegate) {
            Object value = null;
            try {
                value = getter.invoke(delegate);
                value = createProxyOnDemand(value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return value;
        }
    }

    public class ManagedList extends ManagedElement {
        protected Method getter;
        protected Method setter;
        private Method size;

        public ManagedList(Method getter, Method setter, Method size) {
            this.getter = getter;
            this.setter = setter;
            this.size = size;
        }

        public void set(Object delegate, int index, Object value) {
            try {
                setter.invoke(delegate, index, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        public Object get(Object delegate, int index) {
            Object value = null;
            try {
                value = getter.invoke(delegate, index);
                value = createProxyOnDemand(value);

            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return value;
        }

        public int size(Object delegate) {
            try {
                return (int) size.invoke(delegate);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return 0;
        }
    }

}
