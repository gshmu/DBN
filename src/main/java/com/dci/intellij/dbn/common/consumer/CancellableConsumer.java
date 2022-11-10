package com.dci.intellij.dbn.common.consumer;


import com.dci.intellij.dbn.common.util.Consumer;
import com.intellij.openapi.progress.ProcessCanceledException;



public interface CancellableConsumer<T> extends Consumer<T> {
    void checkCancelled() throws ProcessCanceledException;

    static void checkCancelled(Consumer<?> consumer) {
        if (consumer instanceof CancellableConsumer) {
            CancellableConsumer<?> cancellableConsumer = (CancellableConsumer<?>) consumer;
            cancellableConsumer.checkCancelled();
        }
    }
}
