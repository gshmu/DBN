package com.dci.intellij.dbn.editor.session;

import com.dci.intellij.dbn.common.options.setting.Settings;
import com.dci.intellij.dbn.common.state.PersistentStateElement;
import com.dci.intellij.dbn.common.util.Cloneable;
import com.dci.intellij.dbn.data.model.sortable.SortableDataModelState;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SessionBrowserState extends SortableDataModelState implements FileEditorState, PersistentStateElement, Cloneable<SessionBrowserState> {
    public static final SessionBrowserState VOID = new SessionBrowserState();

    private SessionBrowserFilter filterState = new SessionBrowserFilter();
    private int refreshInterval = 0;

    @Override
    public boolean canBeMergedWith(@NotNull FileEditorState fileEditorState, @NotNull FileEditorStateLevel fileEditorStateLevel) {
        return false;
    }

    @Override
    public void readState(@NotNull Element element) {
        refreshInterval = Settings.getInteger(element, "refresh-interval", refreshInterval);

        Element sortingElement = element.getChild("sorting");
        sortingState.readState(sortingElement);

        Element filterElement = element.getChild("filter");
        if (filterElement != null) {
            filterState.setFilterValue(SessionBrowserFilterType.USER, Settings.getString(filterElement, "user", null));
            filterState.setFilterValue(SessionBrowserFilterType.HOST, Settings.getString(filterElement, "host", null));
            filterState.setFilterValue(SessionBrowserFilterType.STATUS, Settings.getString(filterElement, "status", null));
        }
    }

    @Override
    public void writeState(Element element) {
        Settings.setInteger(element, "refresh-interval", refreshInterval);

        Element sortingElement = new Element("sorting");
        element.addContent(sortingElement);
        sortingState.writeState(sortingElement);

        Element filterElement = new Element("filter");
        element.addContent(filterElement);
        Settings.setString(filterElement, "user", filterState.getFilterValue(SessionBrowserFilterType.USER));
        Settings.setString(filterElement, "host", filterState.getFilterValue(SessionBrowserFilterType.HOST));
        Settings.setString(filterElement, "status", filterState.getFilterValue(SessionBrowserFilterType.STATUS));
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
            clone.contentTypesMap = new HashMap<>(contentTypesMap);
        }

        return clone;
    }
}