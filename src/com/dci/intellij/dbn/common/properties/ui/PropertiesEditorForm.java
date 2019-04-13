package com.dci.intellij.dbn.common.properties.ui;

import com.dci.intellij.dbn.common.ui.DBNForm;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.ToolbarDecorator;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class PropertiesEditorForm extends DBNFormImpl<DBNForm> {
    private JPanel mainPanel;
    private PropertiesEditorTable table;

    public PropertiesEditorForm(DBNForm parentForm, Map<String, String> properties, boolean showMoveButtons) {
        super(parentForm);
        table = new PropertiesEditorTable(properties);
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(table);
        decorator.setAddAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                table.insertRow();
            }
        });

        decorator.setRemoveAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                table.removeRow();
            }
        });

        if (showMoveButtons) {
            decorator.setMoveUpAction(new AnActionButtonRunnable() {
                @Override
                public void run(AnActionButton anActionButton) {
                    table.moveRowUp();
                }
            });

            decorator.setMoveDownAction(new AnActionButtonRunnable() {
                @Override
                public void run(AnActionButton anActionButton) {
                    table.moveRowDown();
                }
            });
        }

        JPanel propertiesPanel = decorator.createPanel();
        Container parent = table.getParent();
        parent.setBackground(table.getBackground());
        mainPanel.add(propertiesPanel, BorderLayout.CENTER);
/*
        propertiesTableScrollPane.setViewportView(propertiesTable);
        propertiesTableScrollPane.setPreferredSize(new Dimension(200, 80));
*/
    }

    public PropertiesEditorTable getTable() {
        return table;
    }

    public void setProperties(Map<String, String> properties) {
        table.setProperties(properties);
    } 

    @NotNull
    @Override
    public JPanel ensureComponent() {
        return mainPanel;
    }

    public Map<String, String> getProperties() {
        return table.getModel().exportProperties();
    }
}
