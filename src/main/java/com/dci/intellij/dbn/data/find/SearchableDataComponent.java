package com.dci.intellij.dbn.data.find;

import com.dci.intellij.dbn.common.ui.form.DBNForm;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTable;
import org.jetbrains.annotations.NotNull;

public interface SearchableDataComponent extends DBNForm {
    void showSearchHeader();
    void hideSearchHeader();
    void cancelEditActions();
    String getSelectedText();

    @NotNull
    BasicTable<?> getTable();
}
