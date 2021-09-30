package com.dci.intellij.dbn.common.constant;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.common.util.ThreadLocalFlag;
import lombok.SneakyThrows;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Use this "constant" if the possible values are variable (i.e. cannot be implemented with enum).
 */
public abstract class PseudoConstant<T extends PseudoConstant<T>> implements Constant<T>, Serializable {
    private static final Map<Class<? extends PseudoConstant<?>>, Map<String, PseudoConstant<?>>> REGISTRY = new ConcurrentHashMap<>();
    private static final ThreadLocalFlag INTERNAL = new ThreadLocalFlag(false);

    private final String id;

    public PseudoConstant(String id) {
        this.id = id.intern();
        if (!INTERNAL.get()) {
            // register the pseudo constant if initialised over constructor
            Class<T> clazz = (Class<T>) getClass();
            Map<String, T> registry = getRegistry(clazz);
            registry.putIfAbsent(id, (T) this);
        }
    }

    private static <T extends PseudoConstant<T>> Map<String, T> getRegistry(Class<T> clazz) {
        return (Map<String, T>) REGISTRY.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>());
    }

    @Override
    public final String id() {
        return id;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PseudoConstant that = (PseudoConstant) o;
        return Objects.equals(id, that.id);

    }

    @Override
    public final int hashCode() {
        return id.hashCode();
    }

    protected static <T extends PseudoConstant<T>> T get(Class<T> clazz, String id) {
        T constant = null;
        if (StringUtil.isNotEmpty(id)) {
            id = id.trim();
            Map<String, T> registry = getRegistry(clazz);
            return registry.computeIfAbsent(id, key -> createConstant(clazz, key));
        }
        return constant;
    }

    @SneakyThrows
    private static <T extends PseudoConstant<T>> T createConstant(Class<T> clazz, String id) {
        boolean internal = INTERNAL.get();
        try {
            INTERNAL.set(true);
            Constructor<T> constructor = clazz.getDeclaredConstructor(String.class);
            constructor.setAccessible(true);
            return constructor.newInstance(id);
        } finally {
            INTERNAL.set(internal);
        }
    }

    protected static <T extends PseudoConstant<T>> T[] list(Class<T> clazz, String commaSeparatedIds) {
        List<T> constants = new ArrayList<T>();
        if (StringUtil.isNotEmpty(commaSeparatedIds)) {
            String[] ids = commaSeparatedIds.split(",");

            for (String id : ids) {
                if (StringUtil.isNotEmpty(id)) {
                    T constant = get(clazz, id.trim());
                    constants.add(constant);
                }
            }
        }
        return constants.toArray((T[]) Array.newInstance(clazz, constants.size()));
    }

    @Override
    public final String toString() {
        return id();
    }
}
