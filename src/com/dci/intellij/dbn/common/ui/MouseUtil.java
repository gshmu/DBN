package com.dci.intellij.dbn.common.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MouseUtil {
    public static void processMouseEvent(MouseEvent e, MouseListener listener) {
        int id = e.getID();
        switch (id) {
            case MouseEvent.MOUSE_PRESSED:
                listener.mousePressed(e);
                break;
            case MouseEvent.MOUSE_RELEASED:
                listener.mouseReleased(e);
                break;
            case MouseEvent.MOUSE_CLICKED:
                listener.mouseClicked(e);
                break;
            case MouseEvent.MOUSE_EXITED:
                listener.mouseExited(e);
                break;
            case MouseEvent.MOUSE_ENTERED:
                listener.mouseEntered(e);
                break;
        }
    }

    public static boolean isNavigationEvent(MouseEvent event) {
        int button = event.getButton();
        return button == MouseEvent.BUTTON2 || (event.isControlDown() && button == MouseEvent.BUTTON1);
    }

    public static void removeMouseListeners(JComponent component, boolean recursive) {
        MouseListener[] mouseListeners = component.getMouseListeners();
        for (MouseListener mouseListener : mouseListeners) {
            component.removeMouseListener(mouseListener);
        }
        if (recursive) {
            Component[] components = component.getComponents();
            for (Component childComponent : components) {
                if (childComponent instanceof JComponent)
                removeMouseListeners((JComponent) childComponent, true);
            }
        }
    }
}
