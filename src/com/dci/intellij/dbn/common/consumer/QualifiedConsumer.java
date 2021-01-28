package com.dci.intellij.dbn.common.consumer;

import com.intellij.openapi.progress.ProcessCanceledException;

import java.util.Arrays;
import java.util.Collection;

public interface QualifiedConsumer<T> extends com.intellij.util.Consumer<T> {

    default void consume(T[] array) {
        checkCancelled();
        if (array != null && array.length > 0) {
            Arrays.stream(array).forEach(element -> {
                checkCancelled();
                consume(element);
            });
        }
    }

    default void consume(Collection<T> objects) {
        checkCancelled();
        if (objects != null && !objects.isEmpty()) {
            objects.forEach(element -> {
                checkCancelled();
                consume(element);
            });
        }
    }

    default void checkCancelled() throws ProcessCanceledException {};
}
