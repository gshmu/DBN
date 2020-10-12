package com.dci.intellij.dbn.common.ui.table;

import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.event.ProjectEventAdapter;

import javax.swing.table.TableModel;

public interface DBNTableModel extends TableModel, StatefulDisposable, ProjectEventAdapter {
}
