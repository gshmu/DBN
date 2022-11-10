package com.dci.intellij.dbn.common.dispose;

import com.dci.intellij.dbn.common.compatibility.Compatibility;
import com.dci.intellij.dbn.common.event.ApplicationEvents;
import com.dci.intellij.dbn.common.thread.Background;
import com.dci.intellij.dbn.common.thread.ThreadMonitor;
import com.dci.intellij.dbn.common.thread.ThreadProperty;
import com.intellij.ide.AppLifecycleListener;
import com.intellij.openapi.application.ApplicationListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.dci.intellij.dbn.common.thread.ThreadMonitor.isDisposerProcess;

@Slf4j
public class BackgroundDisposer {
    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    private volatile boolean running;
    private volatile boolean exiting = false;

    private static final BackgroundDisposer INSTANCE = new BackgroundDisposer();

    private BackgroundDisposer() {
        ApplicationEvents.subscribe(null, AppLifecycleListener.TOPIC, new AppLifecycleListener() {
            @Override
            public void appWillBeClosed(boolean isRestart) {
                exiting = true;
            }
        });

        ApplicationManager.getApplication().addApplicationListener(new ApplicationListener() {
            @Override
            @Compatibility
            public void applicationExiting() {
                exiting = true;
            }
        });

    }

    public static void queue(Runnable runnable) {
        if (isDisposerProcess()) {
            runnable.run();
        } else {
            INSTANCE.push(runnable);
        }

    }

    private void push(Runnable runnable) {
        if (!exiting) {
            queue.offer(runnable);
            if (!running && !exiting) {
                synchronized (BackgroundDisposer.class) {
                    if (!running && !exiting) {
                        running = true;
                        startDisposer();

                    }
                }
            }
        }
    }

    private void startDisposer() {
        Background.run(() -> {
            try {
                ThreadMonitor.wrap(ThreadProperty.DISPOSER, () -> {
                    while (!exiting) {
                        Runnable task = queue.poll(10, TimeUnit.SECONDS);
                        if (task != null) {
                            try {
                                task.run();
                            } catch (ProcessCanceledException ignore){
                            } catch (Exception e) {
                                log.error("Background disposer failed", e);
                            }
                        }
                    }
                });
            } finally {
                running = false;
            }
        });
    }
}
