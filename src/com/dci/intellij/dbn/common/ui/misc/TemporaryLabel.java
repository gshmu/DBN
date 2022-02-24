package com.dci.intellij.dbn.common.ui.misc;

import com.dci.intellij.dbn.common.thread.Dispatch;
import com.intellij.util.ui.TimerUtil;

import javax.swing.*;

public class TemporaryLabel extends JLabel {

    public void show(int timeout) {
        Dispatch.run(() -> {
            setVisible(true);
            Timer timer = TimerUtil.createNamedTimer("TemporaryLabelTimeout", timeout, e -> setVisible(false));
            timer.setRepeats(false);
            timer.start();
        });
    }
}
