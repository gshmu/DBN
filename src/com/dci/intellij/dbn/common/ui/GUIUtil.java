package com.dci.intellij.dbn.common.ui;

import com.dci.intellij.dbn.common.Colors;
import com.dci.intellij.dbn.common.lookup.Visitor;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.util.Unsafe;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.popup.list.ListPopupImpl;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.text.JTextComponent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.IllegalComponentStateException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.event.InputEvent;
import java.lang.reflect.Method;
import java.util.EventListener;

public class GUIUtil{

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

    @Nullable
    public static Point getRelativeMouseLocation(Component component) {
        try {
            PointerInfo pointerInfo = MouseInfo.getPointerInfo();
            if (pointerInfo != null) {
                Point mouseLocation = pointerInfo.getLocation();
                return getRelativeLocation(mouseLocation, component);
            }
        } catch (IllegalComponentStateException ignore) {}
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


    public static boolean isDarkLookAndFeel() {
        return UIUtil.isUnderDarcula();
    }

    public static void updateTitledBorders(JPanel panel) {
        Border border = panel.getBorder();
        if (border instanceof TitledBorder) {
            TitledBorder titledBorder = (TitledBorder) border;
            //titledBorder.setTitleColor(com.intellij.util.ui.GUIUtil.getLabelForeground());
            titledBorder.setTitleColor(Colors.HINT_COLOR);
            //titledBorder.setBorder(Borders.getLineBorder(JBColor.border()));
            titledBorder.setBorder(Borders.TOP_LINE_BORDER);
            border = new CompoundBorder(Borders.topInsetBorder(8), titledBorder);
            panel.setBorder(border);

        }
    }

    public static void removeListeners(Component comp) {
        Method[] methods = comp.getClass().getMethods();
        for (Method method : methods) {
            String name = method.getName();
            if (name.startsWith("remove") && name.endsWith("Listener")) {
                Class[] params = method.getParameterTypes();
                if (params.length == 1) {
                    EventListener[] listeners = Unsafe.silent(new EventListener[0], () -> comp.getListeners(params[0]));
                    for (EventListener listener : listeners) {
                        Unsafe.silent(() -> method.invoke(comp, listener));
                    }
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

    public static void showCompletionPopup(
            JComponent toolbarComponent,
            JList list,
            String title,
            @NotNull JTextComponent textField,
            String adText) {

        PopupChooserBuilder builder = JBPopupFactory.getInstance().createListPopupBuilder(list);
        if (title != null) {
            builder.setTitle(title);
        }
        JBPopup popup = builder.
                setMovable(false).
                setResizable(false).
                setRequestFocus(true).
                setItemChoosenCallback(() -> {
                    String selectedValue = (String)list.getSelectedValue();
                    if (selectedValue != null) {
                        textField.setText(selectedValue);
                        IdeFocusManager.getGlobalInstance().requestFocus(textField, false);
                    }
                }).
                createPopup();

        if (adText != null) {
            popup.setAdText(adText, SwingConstants.LEFT);
        }

        if (toolbarComponent != null) {
            popup.showUnderneathOf(toolbarComponent);
        }
        else {
            popup.showUnderneathOf(textField);
        }
    }

    public static void visit(JComponent component, Visitor<Component> visitor) {
        visitor.visit(component);
        Component[] childComponents = component.getComponents();
        for (Component childComponent : childComponents) {
            if (childComponent instanceof JComponent) {
                visit((JComponent) childComponent, visitor);
            }

        }
    }

    @Deprecated
    public static void replaceSplitters(JComponent root) {
        //GuiUtils.replaceJSplitPaneWithIDEASplitter(root);
    }


    public static void updateSplitterProportion(JComponent root, float proportion) {
        if (true) return;
        visit(root, component -> {
            if (component instanceof Splitter) {
                Splitter splitter = (Splitter) root;
                splitter.setProportion(proportion);
            } else if (component instanceof JSplitPane) {
                JSplitPane pane = (JSplitPane) component;
                Container parent = pane.getParent();
                int dividerSize = pane.getDividerSize();
                int orientation = pane.getOrientation();
                Dimension preferredSize = parent.getPreferredSize();
                int dividerLocation = (int) (orientation == JSplitPane.VERTICAL_SPLIT ?
                                        proportion * (preferredSize.getHeight() - dividerSize) :
                                        proportion * (preferredSize.getWidth() - dividerSize));
                pane.setDividerLocation(dividerLocation);
            }
        });
    }
}
