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
    private String userFilter;
    private String hostFilter;
    private String statusFilter;


    public boolean canBeMergedWith(FileEditorState fileEditorState, FileEditorStateLevel fileEditorStateLevel) {
        return false;
    }

    public void readState(@NotNull Element element) {
        Element sortingElement = element.getChild("sorting");
        sortingState.readState(sortingElement);

        Element filterElement = element.getChild("filter");
        if (filterElement != null) {
            userFilter = SettingsUtil.getString(filterElement, "user", null);
            hostFilter = SettingsUtil.getString(filterElement, "host", null);
            statusFilter = SettingsUtil.getString(filterElement, "status", null);
        }
    }

    public void writeState(Element targetElement) {
        Element sortingElement = new Element("sorting");
        targetElement.addContent(sortingElement);
        sortingState.writeState(sortingElement);

        Element filterElement = new Element("filter");
        targetElement.addContent(filterElement);
        SettingsUtil.setString(filterElement, "user", userFilter);
        SettingsUtil.setString(filterElement, "host", hostFilter);
        SettingsUtil.setString(filterElement, "status", statusFilter);
    }

    public String getUserFilter() {
        return userFilter;
    }

    public void setUserFilter(String userFilter) {
        this.userFilter = userFilter;
    }

    public String getHostFilter() {
        return hostFilter;
    }

    public void setHostFilter(String hostFilter) {
        this.hostFilter = hostFilter;
    }

    public String getStatusFilter() {
        return statusFilter;
    }

    public void setStatusFilter(String statusFilter) {
        this.statusFilter = statusFilter;
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


}