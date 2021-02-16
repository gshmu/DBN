package com.dci.intellij.dbn.editor.session;

import com.dci.intellij.dbn.common.options.setting.SettingsSupport;
import com.dci.intellij.dbn.common.state.PersistentStateElement;
import com.dci.intellij.dbn.common.util.Cloneable;
import com.dci.intellij.dbn.data.model.sortable.SortableDataModelState;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import gnu.trove.THashMap;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class SessionBrowserState extends SortableDataModelState implements FileEditorState, PersistentStateElement, Cloneable<SessionBrowserState> {
    public static final SessionBrowserState VOID = new SessionBrowserState();
    private SessionBrowserFilter filterState = new SessionBrowserFilter();
    private int refreshInterval = 0;

    @Override
    public boolean canBeMergedWith(FileEditorState fileEditorState, FileEditorStateLevel fileEditorStateLevel) {
        return false;
    }

    @Override
    public void readState(@NotNull Element element) {
        refreshInterval = SettingsSupport.getInteger(element, "refresh-interval", refreshInterval);

        Element sortingElement = element.getChild("sorting");
        sortingState.readState(sortingElement);

        Element filterElement = element.getChild("filter");
        if (filterElement != null) {
            filterState.setFilterValue(SessionBrowserFilterType.USER, SettingsSupport.getString(filterElement, "user", null));
            filterState.setFilterValue(SessionBrowserFilterType.HOST, SettingsSupport.getString(filterElement, "host", null));
            filterState.setFilterValue(SessionBrowserFilterType.STATUS, SettingsSupport.getString(filterElement, "status", null));
        }
    }

    @Override
    public void writeState(Element element) {
        SettingsSupport.setInteger(element, "refresh-interval", refreshInterval);

        Element sortingElement = new Element("sorting");
        element.addContent(sortingElement);
        sortingState.writeState(sortingElement);

        Element filterElement = new Element("filter");
        element.addContent(filterElement);
        SettingsSupport.setString(filterElement, "user", filterState.getFilterValue(SessionBrowserFilterType.USER));
        SettingsSupport.setString(filterElement, "host", filterState.getFilterValue(SessionBrowserFilterType.HOST));
        SettingsSupport.setString(filterElement, "status", filterState.getFilterValue(SessionBrowserFilterType.STATUS));
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    @Override
    public SessionBrowserState clone() {
        SessionBrowserState clone = new SessionBrowserState();
        clone.refreshInterval = refreshInterval;
        clone.setReadonly(isReadonly());
        clone.setRowCount(getRowCount());
        clone.setSortingState(getSortingState().clone());
        clone.filterState = filterState.clone();
        if (contentTypesMap != null) {
            clone.contentTypesMap = new THashMap<String, String>(contentTypesMap);
        }

        return clone;
    }

    public void setFilterState(SessionBrowserFilter filterState) {
        this.filterState = filterState;
    }

    public SessionBrowserFilter getFilterState() {
        return filterState;
    }

    /*****************************************************************
     *                     equals / hashCode                         *
     *****************************************************************/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SessionBrowserState that = (SessionBrowserState) o;
        if (this.refreshInterval != that.refreshInterval) return false;
        return filterState.equals(that.filterState);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + filterState.hashCode();
        return result;
    }
}