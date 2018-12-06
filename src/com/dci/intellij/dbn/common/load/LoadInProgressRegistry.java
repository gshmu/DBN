package com.dci.intellij.dbn.common.load;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.intellij.openapi.progress.ProcessCanceledException;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class LoadInProgressRegistry<T extends Disposable> extends DisposableBase implements Disposable{
    private final List<T> nodes = new CopyOnWriteArrayList<>();

    private LoadInProgressRegistry(Disposable parentDisposable) {
        DisposerUtil.register(parentDisposable, this);
    }

    public void register(T node) {
        boolean startTimer = nodes.size() == 0;
        nodes.add(node);
        if (startTimer) {
            Timer reloader = new Timer("DBN - Object Browser (load in progress reload timer)");
            reloader.schedule(new RefreshTask(), 0, LoadIcon.ROLL_INTERVAL);
        }
    }

    private class RefreshTask extends TimerTask {
        public void run() {
            for (T node : nodes) {
                try {
                    if (node.isDisposed()) {
                        nodes.remove(node);
                    } else {
                        LoadInProgressRegistry.this.notify(node);
                    }
                } catch (ProcessCanceledException e) {
                    nodes.remove(node);
                }
            }

            if (nodes.isEmpty()) {
                cancel();
            }
        }
    }

    protected abstract void notify(T node);

    public static <T extends Disposable> LoadInProgressRegistry<T> create(Disposable parentDisposable, Notifier<T> notifier) {
        return new LoadInProgressRegistry<T>(parentDisposable) {
            @Override
            protected void notify(T node) {
                notifier.notify(node);
            }
        };
    }

    @FunctionalInterface
    public interface Notifier<T extends Disposable> {
        void notify(T node);
    }

    @Override
    public void dispose() {
        super.dispose();
        nodes.clear();
    }
}
