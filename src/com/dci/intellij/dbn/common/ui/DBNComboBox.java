package com.dci.intellij.dbn.common.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.latent.Loader;
import com.dci.intellij.dbn.common.property.PropertyHolder;
import com.dci.intellij.dbn.common.property.PropertyHolderImpl;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DBNComboBox<T extends Presentable> extends JComboBox<T> implements PropertyHolder<ValueSelectorOption> {
    private Set<ValueSelectorListener<T>> listeners = new HashSet<ValueSelectorListener<T>>();
    private ListPopup popup;
    private PresentableFactory<T> valueFactory;
    private Loader<List<T>> valueLoader;

    private PropertyHolder<ValueSelectorOption> options = new PropertyHolderImpl<ValueSelectorOption>() {
        @Override
        protected ValueSelectorOption[] properties() {
            return ValueSelectorOption.values();
        }
    };

    private final MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            if (DBNComboBox.this.isEnabled()) {
                showPopup();
            }
        }
    };

    public DBNComboBox(T ... values) {
        this();
        setValues(values);
    }

    public DBNComboBox() {
        super(new DBNComboBoxModel<>());
        MouseUtil.removeMouseListeners(this, true);

        addMouseListener(mouseListener);
        Color background = UIUtil.getTextFieldBackground();
        for (Component component : getComponents()) {
            component.addMouseListener(mouseListener);
        }
        setBackground(background);

        setRenderer(new ColoredListCellRenderer<T>() {
            @Override
            protected void customizeCellRenderer(@NotNull JList<? extends T> list, T value, int index, boolean selected, boolean hasFocus) {
                if (value != null) {
                    append(DBNComboBox.this.getName(value));
                    setIcon(value.getIcon());
                }
                setBackground(background);
            }
        });
    }

    @Override
    public void setBackground(Color background) {
        super.setBackground(background);
        ComboBoxEditor editor = getEditor();
        if (editor != null) {
            editor.getEditorComponent().setBackground(background);
        }
    }

    @Override
    public void setPopupVisible(boolean visible) {
        if (visible && !isPopupVisible()) {
            displayPopup();
        }
    }

    @Override
    public boolean isPopupVisible() {
        return popup != null;
    }

    private void displayPopup() {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        for (T value : getModel().getItems()) {
            actionGroup.add(new SelectValueAction(value));
        }
        if (valueFactory != null) {
            actionGroup.add(ActionUtil.SEPARATOR);
            actionGroup.add(new AddValueAction());
        }
        popup = JBPopupFactory.getInstance().createActionGroupPopup(
                null,
                actionGroup,
                DataManager.getInstance().getDataContext(this),
                false,
                false,
                false,
                () -> {
                    DisposerUtil.dispose(popup);
                    popup = null;
                    GUIUtil.repaintAndFocus(DBNComboBox.this);
                },
                10,
                anAction -> {
                    if (anAction instanceof DBNComboBox.SelectValueAction) {
                        SelectValueAction action = (SelectValueAction) anAction;
                        return action.value.equals(getSelectedValue());
                    }
                    return false;
                });
        GUIUtil.showUnderneathOf(popup, this, 3, 200);
    }

    public void setValueFactory(PresentableFactory<T> valueFactory) {
        this.valueFactory = valueFactory;
    }

    public void setValueLoader(Loader<List<T>> valueLoader) {
        this.valueLoader = valueLoader;
        setValues(valueLoader.load());
    }

    public void addListener(ValueSelectorListener<T> listener) {
        listeners.add(listener);
    }

    public void removeListener(ValueSelectorListener<T> listener) {
        listeners.remove(listener);
    }


    public void clearValues() {
        selectValue(null);
        getModel().removeAllElements();
    }

    public String getOptionDisplayName(T value) {
        return getName(value);
    }

    public void reloadValues() {
        setValues(valueLoader.load());
    }

    public class SelectValueAction extends DumbAwareAction {
        private T value;

        SelectValueAction(T value) {
            super(getOptionDisplayName(value), null, options != null && options.is(ValueSelectorOption.HIDE_ICON) ? null : value.getIcon());
            this.value = value;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            selectValue(value);
            DBNComboBox.this.requestFocus();
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            Presentation presentation = e.getPresentation();
            presentation.setVisible(isVisible(value));
            presentation.setText(getOptionDisplayName(value), false);
        }
    }

    private class AddValueAction extends DumbAwareAction {
        AddValueAction() {
            super(valueFactory.getActionName(), null, Icons.ACTION_ADD);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            valueFactory.create(inputValue -> {
                if (inputValue != null) {
                    addValue(inputValue);
                    selectValue(inputValue);
                }
            });
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setVisible(valueFactory != null);
        }
    }

    @NotNull
    private String getName(T value) {
        if (value != null) {
            String description = value.getDescription();
            String name = value.getName();
            return options != null && options.is(ValueSelectorOption.HIDE_DESCRIPTION) || StringUtil.isEmpty(description) ? name : name + " (" + description + ")";
        } else {
            return "";
        }
    }

    public boolean isVisible(T value) {
        return true;
    }

    @Nullable
    public T getSelectedValue() {
        return (T) getSelectedItem();
    }

    public void setSelectedValue(@Nullable T value) {
        selectValue(value);
    }

    protected java.util.List<T> loadValues() {
        return new ArrayList<>();
    }

    public void setValues(T ... values) {
        setValues(Arrays.asList(values));
    }

    public void setValues(java.util.List<T> values) {
        DBNComboBoxModel<T> model = getModel();
        model.removeAllElements();
        addValues(values);
    }

    private void addValue(T value) {
        DBNComboBoxModel<T> model = getModel();
        model.addElement(value);
    }

    @Override
    public DBNComboBoxModel<T> getModel() {
        return (DBNComboBoxModel<T>) super.getModel();
    }

    @Override
    public void setModel(ComboBoxModel<T> aModel) {
        super.setModel(aModel);
    }

    public void addValues(Collection<T> values) {
        for (T value : values) {
            addValue(value);
        }
    }

    @Override
    public void setSelectedItem(Object anObject) {
        T oldValue = getSelectedValue();

        super.setSelectedItem(anObject);
        T newValue = getSelectedValue();
        for (ValueSelectorListener<T> listener : listeners) {
            listener.selectionChanged(oldValue, newValue);
        }
    }

    private void selectValue(T value) {
        T oldValue = getSelectedValue();
        DBNComboBoxModel<T> model = getModel();
        if (value != null) {
            value = model.containsItem(value) ? value : model.isEmpty() ? null : model.getElementAt(0);
        }
        if (!CommonUtil.safeEqual(oldValue, value) || (model.isEmpty() && value == null)) {
            setSelectedItem(value);
        }
    }

    void selectNext() {
        T selectedValue = getSelectedValue();
        if (selectedValue != null) {
            List<T> values = getModel().getItems();
            int index = values.indexOf(selectedValue);
            if (index < values.size() - 1) {
                T nextValue = values.get(index + 1);
                selectValue(nextValue);
            }
        }
    }

    void selectPrevious() {
        T selectedValue = getSelectedValue();
        if (selectedValue != null) {
            List<T> values = getModel().getItems();
            int index = values.indexOf(selectedValue);
            if (index > 0) {
                T previousValue = values.get(index - 1);
                selectValue(previousValue);
            }
        }
    }


    @Override
    public boolean set(ValueSelectorOption status, boolean value) {
        return options.set(status, value);
    }

    @Override
    public boolean is(ValueSelectorOption status) {
        return options.is(status);
    }
}
