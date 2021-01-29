package com.dci.intellij.dbn.common.routine;

import com.dci.intellij.dbn.common.util.Measured;
import com.dci.intellij.dbn.common.util.Unsafe;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class AsyncTaskExecutor {
    private final ExecutorService executor;
    private final Set<Future> tasks = new HashSet<>();
    private @Getter boolean finished;

    private final String topic;
    private final long timeout;
    private final TimeUnit timeUnit;

    public AsyncTaskExecutor(ExecutorService executor, String topic, long timeout, TimeUnit timeUnit) {
        this.executor = executor;
        this.topic = topic;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    public void submit(String identifier, Runnable runnable) {
        tasks.add(executor.submit(() ->
                Unsafe.silent(() ->
                        Measured.run(topic + " " + identifier, () -> runnable.run()))));
    }

    public void awaitCompletion() {
        Measured.run(topic + " OVERALL", () -> {
            Unsafe.silent(() -> executor.invokeAll(
                    tasks.stream().
                        map(future -> (Callable<Object>) () -> future.get()).
                        collect(Collectors.toList()),
                    timeout,
                    timeUnit));
            // TODO does this really wait for tasks to complete
            finished = true;
        });
    }
}
