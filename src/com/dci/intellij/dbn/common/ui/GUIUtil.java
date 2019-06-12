package com.dci.intellij.dbn.common.ui;

import com.dci.intellij.dbn.common.Colors;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.popup.list.ListPopupImpl;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.InputEvent;
import java.lang.reflect.Method;
import java.util.EventListener;

public class GUIUtil{
    public static final Font REGULAR_FONT = UIUtil.getLabelFont();
    public static final Font BOLD_FONT = new Font(REGULAR_FONT.getName(), Font.BOLD, REGULAR_FONT.getSize());

    public static void updateSplitterProportion(JComponent root, float proportion) {
        Dispatch.runConditional(() -> {
            if (root instanceof Splitter) {
                Splitter splitter = (Splitter) root;
                splitter.setProportion(proportion);
            } else {
                Component[] components = root.getComponents();
                for (Component component : components) {
                    if (component instanceof JComponent) {
                        updateSplitterProportion((JComponent) component, proportion);
                    }
                }
            }
        });
    }

    public static void stopTableCellEditing(final JComponent root) {
        if (root instanceof JTable) {
            JTable table = (JTable) root;
            TableCellEditor cellEditor = table.getCellEditor();
            if (cellEditor != null) {
                cellEditor.stopCellEditing();
            }
        } else {
            Component[] components = root.getComponents();
            for (Component component : components) {
                if (component instanceof JComponent) {
                    stopTableCellEditing((JComponent) component);
                }
            }
        }
    }
    
    public static Point getRelativeMouseLocation(Component component) {
        PointerInfo pointerInfo = MouseInfo.getPointerInfo();
        if (pointerInfo == null) {
            return new Point();
        } else {
            Point mouseLocation = pointerInfo.getLocation();
            return getRelativeLocation(mouseLocation, component);
        }
    }
    
    public static Point getRelativeLocation(Point locationOnScreen, Component component) {
        Point componentLocation = component.getLocationOnScreen();
        Point relativeLocation = locationOnScreen.getLocation();
        relativeLocation.move(
                (int) (locationOnScreen.getX() - componentLocation.getX()), 
                (int) (locationOnScreen.getY() - componentLocation.getY()));
        return relativeLocation;
    }

    public static boolean isChildOf(Component component, Component child) {
        Component parent = child == null ? null : child.getParent();
        while (parent != null) {
            if (parent == component) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    public static boolean isFocused(Component component, boolean recursive) {
        if (component.isFocusOwner()) return true;
        if (recursive && component instanceof JComponent) {
            JComponent parentComponent = (JComponent) component;
            for (Component childComponent : parentComponent.getComponents()) {
                if (isFocused(childComponent, recursive)) {
                    return true;
                }
            }
        }
        return false;
    }


    public static boolean isDarkLookAndFeel() {
        return UIUtil.isUnderDarcula();
    }

    public static void updateBorderTitleForeground(JPanel panel) {
        Border border = panel.getBorder();
        if (border instanceof TitledBorder) {
            TitledBorder titledBorder = (TitledBorder) border;
            //titledBorder.setTitleColor(com.intellij.util.ui.GUIUtil.getLabelForeground());
            titledBorder.setTitleColor(Colors.HINT_COLOR);
        }
    }

    public static void removeListeners(Component comp) {
        Method[] methods = comp.getClass().getMethods();
        for (Method method : methods) {
            String name = method.getName();
            if (name.startsWith("remove") && name.endsWith("Listener")) {

                Class[] params = method.getParameterTypes();
                if (params.length == 1) {
                    EventListener[] listeners;
                    try {
                        listeners = comp.getListeners(params[0]);
                    } catch (Exception e) {
                        // It is possible that someone could instructions a listener
                        // that doesn't extend from EventListener.  If so, ignore it
                        System.out.println("Listener " + params[0] + " does not extend EventListener");
                        continue;
                    }
                    for (EventListener listener : listeners) {
                        try {
                            method.invoke(comp, listener);
                            //System.out.println("removed Listener " + name + " for comp " + comp + "\n");
                        } catch (Exception e) {
                            System.out.println("Cannot invoke removeListener method " + e);
                            // Continue on.  The reason for removing all listeners is to
                            // make sure that we don't have a listener holding on to something
                            // which will keep it from being garbage collected. We want to
                            // continue freeing listeners to make sure we can free as much
                            // memory has possible
                        }
                    }
                } else {
                    // The only Listener method that I know of that has more than
                    // one argument is removePropertyChangeListener.  If it is
                    // something other than that, flag it and move on.
                    if (!name.equals("removePropertyChangeListener"))
                        System.out.println("    Wrong number of Args " + name);
                }
            }
        }
    }

    public static void showUnderneathOf(@NotNull JBPopup popup, @NotNull Component sourceComponent, int verticalShift, int maxHeight) {
        JComponent popupContent = popup.getContent();
        Dimension preferredSize = popupContent.getPreferredSize();
        int width = Math.max((int) preferredSize.getWidth(), sourceComponent.getWidth());
        int height = (int) Math.min(maxHeight, preferredSize.getHeight());

        if (popup instanceof ListPopupImpl) {
            ListPopupImpl listPopup = (ListPopupImpl) popup;
            JList list = listPopup.getList();
            int listHeight = (int) list.getPreferredSize().getHeight();
            if (listHeight > height) {
                height = Math.min(maxHeight, listHeight);
            }
        }

        popupContent.setPreferredSize(new Dimension(width, height));

        popup.show(new RelativePoint(sourceComponent, new Point(0, sourceComponent.getHeight() + verticalShift)));
    }

    public static Font getEditorFont() {
        EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
        return new Font(scheme.getEditorFontName(), Font.PLAIN, UIUtil.getLabelFont().getSize());
    }

    public static void repaint(JComponent component) {
        Dispatch.run(() -> {
            component.revalidate();
            component.repaint();
        });
    }

    public static void repaintAndFocus(JComponent component) {
        Dispatch.run(() -> {
            component.revalidate();
            component.repaint();
            component.requestFocus();
        });
    }

    public static void setPanelBackground(JPanel panel, Color background) {
        panel.setBackground(background);
        for (Component component : panel.getComponents()) {
            if (component instanceof JPanel) {
                JPanel childPanel = (JPanel) component;
                setPanelBackground(childPanel, background);
            }
        }
    }

    public static int ctrlDownMask() {
        return SystemInfo.isMac ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;
    }
}
