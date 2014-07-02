package com.dci.intellij.dbn.data.find;

import com.dci.intellij.dbn.data.ui.table.basic.BasicTable;

public interface SearchableDataComponent {
    void showSearchHeader();
    void hideSearchHeader();
    void cancelEditActions();
    String getSelectedText();
    BasicTable getTable();
}
