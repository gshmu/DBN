package com.dci.intellij.dbn.editor.session.action;

import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.session.SessionBrowserFilterState;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModel;

public class FilterStatusesComboBoxAction extends AbstractFilterComboBoxAction {

    public FilterStatusesComboBoxAction() {
        super("status", Icons.SB_FILTER_STATUS);
    }

    @Override
    String getFilterValue(@NotNull SessionBrowserFilterState modelFilter) {
        return modelFilter.getStatus();
    }

    @Override
    void setFilterValue(SessionBrowserFilterState modelFilter, String filterValue) {
        modelFilter.setStatus(filterValue);
    }

    @Override
    List<String> getDistinctValues(SessionBrowserModel model, SessionBrowserFilterState modelFilter) {
        return model.getDistinctStatuses(modelFilter.getStatus());
    }
}