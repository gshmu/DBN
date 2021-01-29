package com.dci.intellij.dbn.common.routine;

import com.dci.intellij.dbn.common.util.Unsafe;
import lombok.Getter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class AsyncTaskExecutor {
    private final ExecutorService executor;
    private final Set<Future> tasks = new HashSet<>();
    private @Getter boolean finished;

    private final long timeout;
    private final TimeUnit timeUnit;

    public AsyncTaskExecutor(ExecutorService executor, long timeout, TimeUnit timeUnit) {
        this.executor = executor;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    public void submit(Runnable runnable) {
        tasks.add(executor.submit(() -> Unsafe.warned(() -> runnable.run())));
    }

    public void awaitCompletion() {
        Unsafe.warned(() -> {
            List<Future<Object>> futures = executor.invokeAll(
                    tasks.stream().
                            map(future -> (Callable<Object>) () -> future.get()).
                            collect(Collectors.toList()),
                    timeout,
                    timeUnit);

            futures.forEach(future -> future.cancel(true));
        });
        // TODO does this really wait for tasks to complete
        System.out.println("ACTIVE TASKS " + ((ThreadPoolExecutor)executor).getActiveCount());
        finished = true;
    }
}
