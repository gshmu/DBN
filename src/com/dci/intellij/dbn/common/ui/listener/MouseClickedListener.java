package com.dci.intellij.dbn.common.ui.listener;

import com.dci.intellij.dbn.common.routine.ParametricRunnable;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MouseClickedListener extends MouseAdapter {
    private final ParametricRunnable<MouseEvent, RuntimeException> handler;

    private MouseClickedListener(ParametricRunnable<MouseEvent, RuntimeException> handler) {
        this.handler = handler;
    }

    public static MouseClickedListener create(ParametricRunnable<MouseEvent, RuntimeException> handler) {
        return new MouseClickedListener(handler);
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        handler.run(e);
    }
}
