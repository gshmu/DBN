package com.dci.intellij.dbn.diagnostics;

import com.dci.intellij.dbn.common.notification.NotificationSupport;
import lombok.Getter;
import lombok.Setter;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static com.dci.intellij.dbn.common.notification.NotificationGroup.DIAGNOSTICS;

@Getter
@Setter
public class DeveloperMode {
    private volatile boolean enabled;
    private volatile Timer timer;
    private volatile long timerStart;
    private int timeout = 10;

    private void start() {
        cancel();
        timer = new Timer("DBN - Developer Mode Disable Timer");
        timer.schedule(createTimerTask(), TimeUnit.MINUTES.toMillis(timeout));
        timerStart = System.currentTimeMillis();
    }

    private TimerTask createTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                enabled = false;
                cancel();
            }
        };
    }

    private void cancel() {
        Timer timer = this.timer;
        if (timer != null) {
            timer.cancel();
            this.timer = null;
            this.timerStart = 0;
        }
    }

    public synchronized void setEnabled(boolean enabled) {
        boolean changed = this.enabled != enabled;
        this.enabled = enabled;
        cancel();

        if (enabled) {
            start();
            NotificationSupport.sendInfoNotification(null, DIAGNOSTICS, "Developer Mode activated for " + timeout + " minutes");
        } else if (changed) {
            NotificationSupport.sendInfoNotification(null, DIAGNOSTICS, "Developer Mode deactivated");
        }
    }

    public String getTimeoutText() {
        if (timerStart == 0) return "";

        long lapsed = System.currentTimeMillis() - timerStart;
        long lapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(lapsed);
        long remainingSeconds = Math.max(0, TimeUnit.MINUTES.toSeconds(timeout) - lapsedSeconds);

        return remainingSeconds < 60 ?
                " (timing out in " + remainingSeconds + " seconds) " :
                " (timing out in " + TimeUnit.SECONDS.toMinutes(remainingSeconds) + " minutes) ";
    }    
}
