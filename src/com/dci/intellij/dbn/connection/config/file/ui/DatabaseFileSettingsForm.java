package com.dci.intellij.dbn.connection.config.file.ui;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.connection.config.file.DatabaseFilesBundle;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.AnActionButtonUpdater;
import com.intellij.ui.ToolbarDecorator;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;

public class DatabaseFileSettingsForm extends ConfigurationEditorForm<DatabaseFilesBundle> {
    private JPanel mainPanel;
    private DatabaseFilesTable databaseFilesTable;

    public DatabaseFileSettingsForm(DatabaseFilesBundle filesBundle) {
        super(filesBundle);
        databaseFilesTable = new DatabaseFilesTable(filesBundle);

        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(databaseFilesTable);
        decorator.setAddAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                databaseFilesTable.insertRow();
            }
        });
        decorator.setRemoveAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                databaseFilesTable.removeRow();
            }
        });
        decorator.setRemoveActionUpdater(new AnActionButtonUpdater() {
            @Override
            public boolean isEnabled(AnActionEvent e) {
                return databaseFilesTable.getSelectedRow() != 0;
            }
        });
        decorator.setMoveUpAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                databaseFilesTable.moveRowUp();
            }
        });

        decorator.setMoveUpActionUpdater(new AnActionButtonUpdater() {
            @Override
            public boolean isEnabled(AnActionEvent e) {
                return databaseFilesTable.getSelectedRow() > 1;
            }
        });
        decorator.setMoveDownAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                databaseFilesTable.moveRowDown();
            }
        });
        decorator.setMoveDownActionUpdater(new AnActionButtonUpdater() {
            @Override
            public boolean isEnabled(AnActionEvent e) {
                int selectedRow = databaseFilesTable.getSelectedRow();
                return selectedRow != 0 && selectedRow < databaseFilesTable.getModel().getRowCount() -1;
            }
        });
        decorator.setPreferredSize(new Dimension(-1, 300));
        JPanel panel = decorator.createPanel();
        mainPanel.add(panel, BorderLayout.CENTER);
        databaseFilesTable.getParent().setBackground(databaseFilesTable.getBackground());
        registerComponents(mainPanel);
    }
    
    public JPanel getComponent() {
        return mainPanel;
    }
    
    public void applyFormChanges() throws ConfigurationException {
        DatabaseFilesBundle settings = getConfiguration();
        DatabaseFilesTableModel model = databaseFilesTable.getModel();
        model.validate();
        settings.setFiles(model.getDatabaseFiles().getFiles());
    }

    public void resetFormChanges() {
        DatabaseFilesBundle settings = getConfiguration();
        databaseFilesTable.getModel().setDatabaseFiles(new DatabaseFilesBundle(settings.getFiles()));
    }
}
