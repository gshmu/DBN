package com.dci.intellij.dbn.common.dispose;

import com.dci.intellij.dbn.common.Pair;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.util.Unsafe;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ComponentDisposer {
    private static final Map<Class, List<Pair<Method, Class[]>>> LISTENER_REMOVE_METHODS = new ConcurrentHashMap<>();

    private ComponentDisposer() {}


    public static void removeListeners(Component comp) {
        List<Pair<Method, Class[]>> methodPairs = getListenerRemovalMethods(comp);
        for (Pair<Method, Class[]> methodPair : methodPairs) {
            Method method = methodPair.first();
            Class[] params = methodPair.second();
            EventListener[] listeners = Unsafe.silent(new EventListener[0], () -> comp.getListeners(params[0]));
            if (listeners.length > 0) {
                for (EventListener listener : listeners) {
                    Unsafe.silent(() -> method.invoke(comp, listener));
                }
            }
        }

    }

    @NotNull
    private static List<Pair<Method, Class[]>> getListenerRemovalMethods(Component comp) {
        return LISTENER_REMOVE_METHODS.computeIfAbsent(comp.getClass(), k -> {
            List<Pair<Method, Class[]>> listenerMethods = new ArrayList<>();
            Method[] methods = comp.getClass().getMethods();
            for (Method method : methods) {
                String name = method.getName();
                if (name.startsWith("remove") && name.endsWith("Listener")) {
                    Class[] params = method.getParameterTypes();
                    if (params.length == 1) {
                        listenerMethods.add(Pair.of(method, params));
                    }
                }
            }
            return listenerMethods;
        });
    }

    public static void dispose(@Nullable Component component) {
        if (component != null) {
            Dispatch.runConditional(() -> {
                UIUtil.dispose(component);
                removeListeners(component);
                if (component instanceof Container) {
                    Container container = (Container) component;
                    Component[] components = container.getComponents();
                    if (components.length > 0) {
                        for (Component child : components) {
                            dispose(child);
                            Unsafe.silent(() -> container.remove(child));
                        }
                    }
                }
            });
        }
    }
}
