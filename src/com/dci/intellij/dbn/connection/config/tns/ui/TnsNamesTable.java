package com.dci.intellij.dbn.connection.config.tns.ui;

import com.dci.intellij.dbn.common.ui.component.DBNComponent;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.connection.config.tns.TnsName;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

public class TnsNamesTable extends DBNTable<TnsNamesTableModel> {

    public TnsNamesTable(@NotNull DBNComponent parent, List<TnsName> tnsNames) {
        super(parent, new TnsNamesTableModel(tnsNames), true);
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }

}
