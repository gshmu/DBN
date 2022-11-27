package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.routine.Consumer;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import com.dci.intellij.dbn.common.util.Guarded;
import com.intellij.openapi.progress.ProcessCanceledException;

public class Callback{
    private Runnable before;
    private Runnable success;
    private Consumer<Exception> failure;
    private Runnable after;

    public static Callback create() {
        return new Callback();
    }

    public void before(Runnable before) {
        this.before = before;
    }

    public void onSuccess(Runnable success) {
        this.success = success;
    }

    public void onFailure(Consumer<Exception> failure) {
        this.failure = failure;
    }

    public void after(Runnable after) {
        this.after = after;
    }

    public void background(ThrowableRunnable<Exception> action) {
        Background.run(() -> surround(action));
    }

    public void surround(ThrowableRunnable<Exception> action) {
        try {
            Guarded.run(before);
            action.run();
            Guarded.run(success);
        } catch (ProcessCanceledException ignore) {
        } catch (Exception e) {
            if (failure != null) Guarded.run(() -> failure.accept(e));
        } finally {
            Guarded.run(after);
        }
    }


}
