package com.dci.intellij.dbn.connection.config.tns.ui;

import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.connection.config.tns.TnsName;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.util.List;

public class TnsNamesTable extends DBNTable<TnsNamesTableModel> {

    public TnsNamesTable(Project project, List<TnsName> tnsNames) {
        super(project, new TnsNamesTableModel(tnsNames), true);
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }

}
