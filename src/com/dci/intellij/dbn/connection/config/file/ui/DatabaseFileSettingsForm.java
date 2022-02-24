package com.dci.intellij.dbn.connection.config.file.ui;

import com.dci.intellij.dbn.common.ui.form.DBNFormBase;
import com.dci.intellij.dbn.common.ui.util.UserInterface;
import com.dci.intellij.dbn.connection.config.file.DatabaseFile;
import com.dci.intellij.dbn.connection.config.file.DatabaseFiles;
import com.dci.intellij.dbn.connection.config.ui.ConnectionDatabaseSettingsForm;
import com.intellij.ui.ToolbarDecorator;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class DatabaseFileSettingsForm extends DBNFormBase {
    private JPanel mainPanel;
    private DatabaseFiles databaseFiles;
    private final DatabaseFilesTable table;

    public DatabaseFileSettingsForm(ConnectionDatabaseSettingsForm parent, DatabaseFiles databaseFiles) {
        super(parent);
        table = new DatabaseFilesTable(this, databaseFiles);

        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(table);
        decorator.setAddAction(anActionButton -> table.insertRow());
        decorator.setRemoveAction(anActionButton -> table.removeRow());
        decorator.setRemoveActionUpdater(e -> table.getSelectedRow() != 0);
        decorator.setMoveUpAction(anActionButton -> table.moveRowUp());
        decorator.setMoveUpActionUpdater(e -> table.getSelectedRow() > 1);
        decorator.setMoveDownAction(anActionButton -> table.moveRowDown());
        decorator.setMoveDownActionUpdater(e -> {
            int selectedRow = table.getSelectedRow();
            return selectedRow != 0 && selectedRow < table.getModel().getRowCount() -1;
        });
        decorator.setPreferredSize(new Dimension(-1, 300));
        JPanel panel = decorator.createPanel();
        mainPanel.add(panel, BorderLayout.CENTER);
        table.getParent().setBackground(table.getBackground());
    }
    
    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    public DatabaseFilesTable getTable() {
        return table;
    }

    public DatabaseFiles getDatabaseFiles() {
        UserInterface.stopTableCellEditing(table);
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
