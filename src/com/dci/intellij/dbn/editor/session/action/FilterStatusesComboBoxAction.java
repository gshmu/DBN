package com.dci.intellij.dbn.editor.session.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.session.SessionBrowserFilterState;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FilterStatusesComboBoxAction extends AbstractFilterComboBoxAction {

    public FilterStatusesComboBoxAction() {
        super("Status", Icons.SB_FILTER_STATUS);
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