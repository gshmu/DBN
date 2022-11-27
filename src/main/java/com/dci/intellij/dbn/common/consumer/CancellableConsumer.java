package com.dci.intellij.dbn.common.consumer;


import com.dci.intellij.dbn.common.routine.Consumer;



public interface CancellableConsumer<T> extends Consumer<T> {
    void checkCancelled();

    static void checkCancelled(Consumer<?> consumer) {
        if (consumer instanceof CancellableConsumer) {
            CancellableConsumer<?> cancellableConsumer = (CancellableConsumer<?>) consumer;
            cancellableConsumer.checkCancelled();
        }
    }
}
