package com.dci.intellij.dbn.editor.session.action;

import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.session.SessionBrowserFilterState;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModel;

public class FilterHostsComboBoxAction extends AbstractFilterComboBoxAction {

    public FilterHostsComboBoxAction() {
        super("host", Icons.SB_FILTER_SERVER);
    }

    @Override
    String getFilterValue(@NotNull SessionBrowserFilterState modelFilter) {
        return modelFilter.getHost();
    }

    @Override
    void setFilterValue(SessionBrowserFilterState modelFilter, String filterValue) {
        modelFilter.setHost(filterValue);
    }

    @Override
    List<String> getDistinctValues(SessionBrowserModel model, SessionBrowserFilterState modelFilter) {
        return model.getDistinctHosts(modelFilter.getHost());
    }
}