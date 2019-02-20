package com.dci.intellij.dbn.common.constant;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.intellij.openapi.diagnostic.Logger;
import gnu.trove.THashMap;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Use this "constant" if the possible values are variable (i.e. cannot be implemented with enum).
 */
public abstract class PseudoConstant<T extends PseudoConstant<T>> implements Constant<T>, Serializable {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private static final Map<Class<? extends PseudoConstant>, Map<String, PseudoConstant>> REGISTRY = new HashMap<Class<? extends PseudoConstant>, Map<String, PseudoConstant>>();

    private String id;

    public PseudoConstant(String id) {
        this.id = id;
        getRegistry(getClass()).put(id, this);
    }

    private static Map<String, PseudoConstant> getRegistry(Class clazz) {
        Map<String, PseudoConstant> registry = REGISTRY.get(clazz);
        if (registry == null) {
            synchronized (REGISTRY) {
                registry = REGISTRY.get(clazz);
                if (registry == null) {
                    registry = new THashMap<>();
                    REGISTRY.put(clazz, registry);
                }
            }
        }
        return registry;
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
        return id.equals(that.id);

    }

    @Override
    public final int hashCode() {
        return id.hashCode();
    }

    protected static <T extends PseudoConstant> T get(Class<T> clazz, String id) {
        T constant = null;
        if (StringUtil.isNotEmpty(id)) {
            id = id.trim();
            Map<String, PseudoConstant> registry = getRegistry(clazz);
            constant = (T) registry.get(id);
            if (constant == null) {
                synchronized (REGISTRY) {
                    constant = (T) registry.get(id);
                    if (constant == null) {
                        try {
                            Constructor<T> constructor = clazz.getDeclaredConstructor(String.class);
                            constructor.setAccessible(true);
                            constant = constructor.newInstance(id);
                        } catch (Exception e) {
                            LOGGER.error("Could not instructions constant " + id, e);
                        }
                    }
                }
            }
        }
        return constant;
    }

    protected static <T extends PseudoConstant> T[] list(Class<T> clazz, String commaSeparatedIds) {
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
