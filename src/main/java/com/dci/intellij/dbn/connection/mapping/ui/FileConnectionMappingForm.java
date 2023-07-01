package com.dci.intellij.dbn.connection.mapping.ui;

import com.dci.intellij.dbn.common.ui.form.DBNFormBase;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.connection.mapping.FileConnectionContext;
import com.dci.intellij.dbn.connection.mapping.FileConnectionContextManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class FileConnectionMappingForm extends DBNFormBase {
    private JBScrollPane mappingsTableScrollPane;
    private JPanel mainPanel;

    private final DBNTable<FileConnectionMappingTableModel> mappingsTable;

    public FileConnectionMappingForm(@Nullable Disposable parent) {
        super(parent);
        Project project = ensureProject();
        FileConnectionContextManager manager = FileConnectionContextManager.getInstance(project);
        List<FileConnectionContext> mappings = new ArrayList<>(manager.getRegistry().getMappings().values());
        FileConnectionMappingTableModel model = new FileConnectionMappingTableModel(mappings);
        mappingsTable = new FileConnectionMappingTable(this, model);

        mappingsTable.accommodateColumnsSize();
        mappingsTableScrollPane.setViewportView(mappingsTable);

    }


    @Override
    protected JComponent getMainComponent() {
        return mainPanel;
    }
}
