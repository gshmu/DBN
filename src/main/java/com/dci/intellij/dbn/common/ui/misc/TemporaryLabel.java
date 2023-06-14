package com.dci.intellij.dbn.common.ui.misc;

import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.util.Timers;

import javax.swing.*;

public class TemporaryLabel extends JLabel {

    public void show(int timeout) {
        changeVisibility(true);
        Timers.executeLater("TemporaryLabelTimeout", timeout, () -> changeVisibility(false));
    }

    private void changeVisibility(boolean aFlag) {
        Dispatch.run(() -> setVisible(aFlag));
    }
}
