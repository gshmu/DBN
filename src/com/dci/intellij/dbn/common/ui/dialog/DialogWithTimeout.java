package com.dci.intellij.dbn.common.ui.dialog;

import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import java.util.Timer;
import java.util.TimerTask;

public abstract class DialogWithTimeout extends DBNDialog{
    private DialogWithTimeoutForm form;
    private Timer timeoutTimer;
    private int secondsLeft;

    protected DialogWithTimeout(Project project, String title, boolean canBeParent, int timeoutSeconds) {
        super(project, title, canBeParent);
        secondsLeft = timeoutSeconds;
        form = new DialogWithTimeoutForm(secondsLeft);
        timeoutTimer = new Timer("Timeout dialog task [" + getProject().getName() + "]");
        timeoutTimer.schedule(new TimeoutTask(), TimeUtil.ONE_SECOND, TimeUtil.ONE_SECOND);
    }

    @Override
    protected void init() {
        form.setContentComponent(createContentComponent());
        super.init();
    }

    private class TimeoutTask extends TimerTask {
        public void run() {
            if (secondsLeft > 0) {
                secondsLeft = secondsLeft -1;
                form.updateTimeLeft(secondsLeft);
                if (secondsLeft == 0) {
                    new SimpleLaterInvocator() {
                        @Override
                        public void execute() {
                            doDefaultAction();
                        }
                    }.start();

                }
            }
        }
    }

    @Nullable
    @Override
    protected final JComponent createCenterPanel() {
        return form.getComponent();
    }

    protected abstract JComponent createContentComponent();

    public abstract void doDefaultAction();

    @Override
    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            timeoutTimer.cancel();
            timeoutTimer.purge();
            form.dispose();
            form = null;
        }
    }

}
