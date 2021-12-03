package com.dci.intellij.dbn.common.filter;

import com.dci.intellij.dbn.common.sign.Signed;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface Filter<T> extends Signed {
    Map<Class<?>, Field[]> FIELDS = new ConcurrentHashMap<>();

    Filter NO_FILTER = new Filter() {
        @Override
        public int getSignature() {
            return 0;
        }

        @Override
        public boolean accepts(Object object) {
            return true;
        }

        @Override
        public boolean acceptsAll(Collection objects) {
            return true;
        }
    };

    boolean accepts(T object);

    @Override
    @SneakyThrows
    default int getSignature() {
/*
        Field[] fields = FIELDS.computeIfAbsent(getClass(), clazz -> {
            Field[] declaredFields = getClass().getDeclaredFields();
            Arrays.stream(declaredFields).forEach(field -> field.setAccessible(true));
            return declaredFields;
        });
        Object[] fieldValues = new Object[fields.length];
        for (int i = 0, fieldsLength = fields.length; i < fieldsLength; i++) {
            fieldValues[i] = fields[i].get(this);
        }
        return Objects.hashCode(fieldValues);
*/
        return hashCode();
    }

    default boolean acceptsAll(Collection<T> objects) {
        for (T object : objects) {
            if (!accepts(object)) return false;
        }
        return true;
    }

    default boolean isEmpty() {
        return false;
    }
}
