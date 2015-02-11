package com.dci.intellij.dbn.editor.session;

import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.data.model.sortable.SortableDataModelState;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import gnu.trove.THashMap;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class SessionBrowserState extends SortableDataModelState implements FileEditorState {
    public static final SessionBrowserState VOID = new SessionBrowserState();
    private SessionBrowserFilterState filterState = new SessionBrowserFilterState();

    public boolean canBeMergedWith(FileEditorState fileEditorState, FileEditorStateLevel fileEditorStateLevel) {
        return fileEditorState instanceof SessionBrowserState;
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
        clone.setSortingState(getSortingState());
        if (contentTypesMap != null) {
            clone.contentTypesMap = new THashMap<String, String>(contentTypesMap);
        }

        return clone;
    }

    public SessionBrowserFilterState getFilterState() {
        return filterState;
    }
}