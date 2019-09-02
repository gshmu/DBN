package com.dci.intellij.dbn.common.thread;

public final class DelayedActionRunner {
    private long lastInvocation;
    private long delay;

    public DelayedActionRunner(long delay) {
        this.delay = delay;
    }

    public void run(Runnable action) {
        lastInvocation = System.currentTimeMillis();
        long thisInvocation = lastInvocation;
        Background.run(() -> {
            Thread.sleep(delay);
            if (thisInvocation == lastInvocation) {
                action.run();
            }
        });
    }
}
