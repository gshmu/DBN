package com.dci.intellij.dbn.editor.session;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.data.model.sortable.SortableDataModelState;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import gnu.trove.THashMap;

public class SessionBrowserState extends SortableDataModelState implements FileEditorState {
    public static final SessionBrowserState VOID = new SessionBrowserState();
    private SessionBrowserFilterState filterState = new SessionBrowserFilterState();

    public boolean canBeMergedWith(FileEditorState fileEditorState, FileEditorStateLevel fileEditorStateLevel) {
        return fileEditorState instanceof SessionBrowserState && fileEditorStateLevel == FileEditorStateLevel.FULL;
    }

    public void readState(@NotNull Element element) {
        Element sortingElement = element.getChild("sorting");
        sortingState.readState(sortingElement);

        Element filterElement = element.getChild("filter");
        if (filterElement != null) {
            filterState.setUser(SettingsUtil.getString(filterElement, "user", null));
            filterState.setHost(SettingsUtil.getString(filterElement, "host", null));
            filterState.setStatus(SettingsUtil.getString(filterElement, "status", null));
        }
    }

    public void writeState(Element element) {
        Element sortingElement = new Element("sorting");
        element.addContent(sortingElement);
        sortingState.writeState(sortingElement);

        Element filterElement = new Element("filter");
        element.addContent(filterElement);
        SettingsUtil.setString(filterElement, "user", filterState.getUser());
        SettingsUtil.setString(filterElement, "host", filterState.getHost());
        SettingsUtil.setString(filterElement, "status", filterState.getStatus());
    }

    public SessionBrowserState clone() {
        SessionBrowserState clone = new SessionBrowserState();
        clone.setReadonly(isReadonly());
        clone.setRowCount(getRowCount());
        clone.setSortingState(getSortingState().clone());
        clone.setFilterState(getFilterState().clone());
        if (contentTypesMap != null) {
            clone.contentTypesMap = new THashMap<String, String>(contentTypesMap);
        }

        return clone;
    }

    public void setFilterState(SessionBrowserFilterState filterState) {
        this.filterState = filterState;
    }

    public SessionBrowserFilterState getFilterState() {
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

        return filterState.equals(that.filterState);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + filterState.hashCode();
        return result;
    }
}