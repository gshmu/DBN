package com.dci.intellij.dbn.common.options.ui;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.ValueSelectorListener;
import com.dci.intellij.dbn.common.ui.list.CheckBoxList;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.ProjectSupplier;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.ui.DocumentAdapter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public abstract class ConfigurationEditorForm<E extends Configuration> extends DBNFormImpl<ConfigurationEditorForm> {
    public static final String DBN_REGISTERED = "DBN_REGISTERED";
    private ItemListener itemListener;
    private ActionListener actionListener;
    private DocumentListener documentListener;
    private TableModelListener tableModelListener;
    private E configuration;

    protected ConfigurationEditorForm(E configuration) {
        super(getProject(configuration));
        this.configuration = configuration;
    }

    protected static Project getProject(Configuration configuration) {
        if (configuration instanceof ProjectSupplier) {
            ProjectSupplier projectSupplier = (ProjectSupplier) configuration;
            return projectSupplier.getProject();
        }
        return null;
    }

    public final E getConfiguration() {
        return FailsafeUtil.get(configuration);
    }

    public abstract void applyFormChanges() throws ConfigurationException;
    public void applyFormChanges(E configuration) throws ConfigurationException {throw new UnsupportedOperationException("Not implemented by default");}
    public abstract void resetFormChanges();

    protected DocumentListener createDocumentListener() {
        return new DocumentAdapter() {
            protected void textChanged(DocumentEvent e) {
                getConfiguration().setModified(true);
            }
        };
    }

    protected ActionListener createActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getConfiguration().setModified(true);
            }
        };
    }

    protected ItemListener createItemListener() {
        return new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                getConfiguration().setModified(true);
            }
        };
    }

    protected TableModelListener createTableModelListener() {
        return new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                getConfiguration().setModified(true);
            }
        };
    }

    protected void registerComponents(JComponent ... components) {
        for (JComponent component : components){
            registerComponent(component);
        }
    }

    protected void registerComponent(JComponent component) {
        if (component.getClientProperty(DBN_REGISTERED) == null) {
            component.putClientProperty(DBN_REGISTERED, true);
            if (component instanceof AbstractButton) {
                AbstractButton abstractButton = (AbstractButton) component;
                if (actionListener == null) actionListener = createActionListener();
                abstractButton.addActionListener(actionListener);
            }
            else if (component instanceof DBNComboBox) {
                DBNComboBox comboBox = (DBNComboBox) component;
                comboBox.addListener(new ValueSelectorListener() {
                    @Override
                    public void selectionChanged(Object oldValue, Object newValue) {
                        if (!CommonUtil.safeEqual(oldValue, newValue)) {
                            getConfiguration().setModified(true);
                        }
                    }
                });
            }
            else if (component instanceof CheckBoxList) {
                CheckBoxList checkBoxList = (CheckBoxList) component;
                if (actionListener == null) actionListener = createActionListener();
                checkBoxList.addActionListener(actionListener);
            } else if (component instanceof JTextField) {
                JTextField textField = (JTextField) component;
                if (documentListener == null) documentListener = createDocumentListener();
                textField.getDocument().addDocumentListener(documentListener);
            } else if (component instanceof JComboBox) {
                JComboBox comboBox = (JComboBox) component;
                if (itemListener == null) itemListener = createItemListener();
                comboBox.addItemListener(itemListener);
            } else if (component instanceof JTable) {
                JTable table = (JTable) component;
                if (tableModelListener == null) tableModelListener = createTableModelListener();
                table.getModel().addTableModelListener(tableModelListener);
            } else {
                for (Component childComponent : component.getComponents()) {
                    if (childComponent instanceof JComponent) {
                        registerComponent((JComponent) childComponent);
                    }
                }
            }
        }

    }

    public void focus() {}

    public void dispose() {
        super.dispose();
        configuration = null;
    }
}
