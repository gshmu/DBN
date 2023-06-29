package com.dci.intellij.dbn.common.ui.util;

import com.dci.intellij.dbn.common.lookup.Visitor;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.util.Strings;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.border.IdeaTitledBorder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Predicate;

import static com.dci.intellij.dbn.common.ui.util.Borders.*;
import static com.dci.intellij.dbn.common.util.Unsafe.cast;
import static com.dci.intellij.dbn.diagnostics.Diagnostics.conditionallyLog;
import static javax.swing.JSplitPane.VERTICAL_SPLIT;

public class UserInterface {

    public static void stopTableCellEditing(JComponent root) {
        visitRecursively(root, component -> {
            if (component instanceof JTable) {
                JTable table = (JTable) component;
                TableCellEditor cellEditor = table.getCellEditor();
                if (cellEditor != null) {
                    cellEditor.stopCellEditing();
                }
            }
        });
    }

    @Nullable
    public static Point getRelativeMouseLocation(Component component) {
        try {
            PointerInfo pointerInfo = MouseInfo.getPointerInfo();
            if (pointerInfo != null) {
                Point mouseLocation = pointerInfo.getLocation();
                return getRelativeLocation(mouseLocation, component);
            }
        } catch (IllegalComponentStateException e) {
            conditionallyLog(e);
        }
        return null;
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

    public static void updateTitledBorder(JPanel panel) {
        Border border = panel.getBorder();
        if (border instanceof TitledBorder) {
            TitledBorder titledBorder = (TitledBorder) border;
            String title = titledBorder.getTitle();
            int indent = Strings.isEmpty(title) ? 0 : 20;
            IdeaTitledBorder replacement = new IdeaTitledBorder(title, indent, Borders.EMPTY_INSETS);
/*
            titledBorder.setTitleColor(Colors.HINT_COLOR);
            titledBorder.setBorder(Borders.TOP_LINE_BORDER);
            border = new CompoundBorder(Borders.topInsetBorder(8), titledBorder);
*/
            border = new CompoundBorder(Borders.topInsetBorder(8), replacement);
            panel.setBorder(border);
        }
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

    public static void changePanelBackground(JPanel panel, Color background) {
        panel.setBackground(background);
        for (Component component : panel.getComponents()) {
            if (component instanceof JPanel) {
                JPanel childPanel = (JPanel) component;
                changePanelBackground(childPanel, background);
            }
        }
    }

    public static int ctrlDownMask() {
        return SystemInfo.isMac ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;
    }

    public static void visitRecursively(JComponent component, Visitor<JComponent> visitor) {
        visitor.visit(component);
        Component[] childComponents = component.getComponents();
        for (Component childComponent : childComponents) {
            if (childComponent instanceof JComponent) {
                visitRecursively((JComponent) childComponent, visitor);
            }

        }
    }

    public static <T extends JComponent> void visitRecursively(JComponent component, Class<T> type, Visitor<T> visitor) {
        if (type.isAssignableFrom(component.getClass())) visitor.visit((T) component);

        Component[] childComponents = component.getComponents();
        for (Component childComponent : childComponents) {
            if (childComponent instanceof JComponent) {
                visitRecursively((JComponent) childComponent, type, visitor);
            }

        }
    }

    public static void updateTitledBorders(JComponent component) {
        visitRecursively(component, JPanel.class, p -> updateTitledBorder(p));
    }

    public static void updateScrollPaneBorders(JComponent component) {
        visitRecursively(component, JScrollPane.class, sp -> sp.setBorder(isBorderlessPane(sp) ? null : COMPONENT_OUTLINE_BORDER));
    }

    private static boolean isBorderlessPane(JScrollPane sp) {
        Component component = getScrollPaneComponent(sp);
        return component instanceof JPanel || component instanceof Borderless;
    }

    public static Component getScrollPaneComponent(JScrollPane scrollPane) {
        return scrollPane.getViewport().getView();
    }

    @Nullable
    public static <T extends JComponent> T getParentOfType(JComponent component, Class<T> type) {
        Component parent = component.getParent();
        while (parent != null) {
            if (type.isAssignableFrom(parent.getClass())) return cast(parent);
            parent = parent.getParent();
        }
        return null;
    }

    public static <T extends JComponent> T getParent(JComponent component, Predicate<Component> check) {
        Component parent = component.getParent();
        while (parent != null) {
            if (check.test(parent)) return cast(parent);
            parent = parent.getParent();
        }
        return null;
    }

    public static Dimension adjust(Dimension dimension, int widthAdjustment, int heightAdjustment) {
        return new Dimension((int) dimension.getWidth() + widthAdjustment, (int) dimension.getHeight() + heightAdjustment);
    }

    @NotNull
    public static ToolbarDecorator createToolbarDecorator(JTable table) {
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(table);
        decorator.setAsUsualTopToolbar();
        decorator.setToolbarBorder(TOOLBAR_DECORATOR_BORDER);
        decorator.setPanelBorder(EMPTY_BORDER);
        return decorator;
    }

    public static String getText(JTextComponent textComponent) {
        return textComponent.getText().trim();
    }


    public static boolean isEmptyText(JTextComponent textComponent) {
        return textComponent.getText().trim().isEmpty();
    }

    public static void limitTextLength(JTextComponent textComponent, int maxLength) {
        textComponent.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                String text = textComponent.getText();
                if (text.length() == maxLength) {
                    e.consume();
                } else if (text.length() > maxLength) {
                    text = text.substring(0, maxLength);
                    textComponent.setText(text);
                    e.consume();
                }
            }
        });
    }

    public static void updateSplitPanes(JComponent component) {
        visitRecursively(component, JSplitPane.class, sp -> replaceSplitPane(sp));
    }

    private static void replaceSplitPane(JSplitPane pane) {
        Container parent = pane.getParent();
        if (parent.getComponents().length != 1 && !(parent instanceof Splitter)) {
            return;
        }

        JComponent component1 = (JComponent) pane.getTopComponent();
        JComponent component2 = (JComponent) pane.getBottomComponent();
        int orientation = pane.getOrientation();

        boolean vertical = orientation == VERTICAL_SPLIT;
        Splitter splitter = new JBSplitter(vertical);
        splitter.setFirstComponent(component1);
        splitter.setSecondComponent(component2);
        splitter.setShowDividerControls(pane.isOneTouchExpandable());
        splitter.setHonorComponentsMinimumSize(true);

        if (pane.getDividerLocation() > 0) {
            SwingUtilities.invokeLater(() -> {
                double proportion;
                if (pane.getOrientation() == VERTICAL_SPLIT) {
                    proportion = (double) pane.getDividerLocation() / (double) (parent.getHeight() - pane.getDividerSize());
                } else {
                    proportion = (double) pane.getDividerLocation() / (double) (parent.getWidth() - pane.getDividerSize());
                }

                if (proportion > 0.0 && proportion < 1.0) {
                    splitter.setProportion((float) proportion);
                }

            });
        }

        if (parent instanceof Splitter) {
            Splitter psplitter = (Splitter) parent;
            if (psplitter.getFirstComponent() == pane) {
                psplitter.setFirstComponent(splitter);
            } else {
                psplitter.setSecondComponent(splitter);
            }
        } else {
            parent.remove(0);
            parent.setLayout(new BorderLayout());
            parent.add(splitter, BorderLayout.CENTER);
        }
    }
}
