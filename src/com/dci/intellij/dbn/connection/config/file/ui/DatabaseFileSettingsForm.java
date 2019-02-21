package com.dci.intellij.dbn.connection.config.file.ui;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.connection.config.file.DatabaseFile;
import com.dci.intellij.dbn.connection.config.file.DatabaseFiles;
import com.dci.intellij.dbn.connection.config.ui.ConnectionDatabaseSettingsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.AnActionButtonUpdater;
import com.intellij.ui.ToolbarDecorator;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class DatabaseFileSettingsForm extends DBNFormImpl<ConnectionDatabaseSettingsForm> {
    private JPanel mainPanel;
    private DatabaseFilesTable table;
    private DatabaseFiles databaseFiles;

    public DatabaseFileSettingsForm(ConnectionDatabaseSettingsForm parent, DatabaseFiles databaseFiles) {
        super(parent);
        table = new DatabaseFilesTable(null, databaseFiles);
        Disposer.register(this, table);

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
        decorator.setRemoveActionUpdater(new AnActionButtonUpdater() {
            @Override
            public boolean isEnabled(AnActionEvent e) {
                return table.getSelectedRow() != 0;
            }
        });
        decorator.setMoveUpAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                table.moveRowUp();
            }
        });

        decorator.setMoveUpActionUpdater(new AnActionButtonUpdater() {
            @Override
            public boolean isEnabled(AnActionEvent e) {
                return table.getSelectedRow() > 1;
            }
        });
        decorator.setMoveDownAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                table.moveRowDown();
            }
        });
        decorator.setMoveDownActionUpdater(new AnActionButtonUpdater() {
            @Override
            public boolean isEnabled(AnActionEvent e) {
                int selectedRow = table.getSelectedRow();
                return selectedRow != 0 && selectedRow < table.getModel().getRowCount() -1;
            }
        });
        decorator.setPreferredSize(new Dimension(-1, 300));
        JPanel panel = decorator.createPanel();
        mainPanel.add(panel, BorderLayout.CENTER);
        table.getParent().setBackground(table.getBackground());
    }
    
    @NotNull
    @Override
    public JPanel getComponent() {
        return mainPanel;
    }

    public DatabaseFilesTable getTable() {
        return table;
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    public DatabaseFiles getDatabaseFiles() {
        GUIUtil.stopTableCellEditing(table);
        return table.getModel().getDatabaseFiles();
    }

    public String getMainFilePath() {
        return getMainFile().getPath();
    }
    public void setMainFilePath(String mainFile) {
        getMainFile().setPath(mainFile);
    }

    private DatabaseFile getMainFile() {
        return getDatabaseFiles().getMainFile();
    }

    public void setDatabaseFiles(DatabaseFiles databaseFiles) {
        this.databaseFiles = databaseFiles;
    }
}
