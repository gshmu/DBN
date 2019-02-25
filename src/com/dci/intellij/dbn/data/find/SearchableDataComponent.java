package com.dci.intellij.dbn.data.find;

import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTable;
import org.jetbrains.annotations.NotNull;

public interface SearchableDataComponent {
    void showSearchHeader();
    void hideSearchHeader();
    void cancelEditActions();
    String getSelectedText();

    @NotNull
    BasicTable getTable();
}
