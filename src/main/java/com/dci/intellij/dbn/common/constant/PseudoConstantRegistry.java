package com.dci.intellij.dbn.common.constant;

import com.dci.intellij.dbn.common.util.Strings;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.dci.intellij.dbn.common.util.Commons.nvl;
import static com.dci.intellij.dbn.common.util.Unsafe.cast;

final class PseudoConstantRegistry {
    private static final Map<Class<? extends PseudoConstant>, PseudoConstantData> REGISTRY = new ConcurrentHashMap<>();

    private PseudoConstantRegistry() {}

    static <T extends PseudoConstant<T>> PseudoConstantData<T> get(Class<T> clazz) {
		return cast(nvl(PseudoConstantData.LOCAL.get(), () -> REGISTRY.computeIfAbsent(clazz, c -> new PseudoConstantData<>(clazz))));
    }

    static <T extends PseudoConstant<T>> T get(Class<T> clazz, String id) {
        if (Strings.isEmpty(id)) return null;
        PseudoConstantData<T> data = get(clazz);
        return data.get(id);
    }

    public static <T extends PseudoConstant<T>> int register(T constant) {
        Class<T> clazz = cast(constant.getClass());
        PseudoConstantData<T> data = get(clazz);
        return data.register(constant);
    }

    static <T extends PseudoConstant<T>> T[] values(Class<T> clazz) {
        PseudoConstantData<T> data = get(clazz);
        return toArray(data.values(), clazz);
    }

    static <T extends PseudoConstant<T>> T[] list(Class<T> clazz, String csvIds) {
        if (Strings.isEmpty(csvIds)) return toArray(Collections.emptyList(), clazz);

        List<T> constants = new ArrayList<T>();
        String[] ids = csvIds.split(",");

        for (String id : ids) {
            if (Strings.isNotEmpty(id)) {
                T constant = get(clazz, id.trim());
                constants.add(constant);
            }
        }
        return toArray(constants, clazz);
    }

    private static <T extends PseudoConstant<T>> T[] toArray(Collection<T> constants, Class<T> clazz) {
        return constants.toArray((T[]) Array.newInstance(clazz, constants.size()));
    }
}
