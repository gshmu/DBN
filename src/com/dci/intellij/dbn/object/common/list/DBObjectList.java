package com.dci.intellij.dbn.object.common.list;

import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.filter.quick.ObjectQuickFilter;

public interface DBObjectList<T extends DBObject> extends BrowserTreeNode, DynamicContent<T>, Comparable<DBObjectList>, ConnectionProvider {
    @NotNull String getName();
    DBObjectType getObjectType();
    void addObject(T object);

    boolean isFiltered();

    ObjectQuickFilter getQuickFilter();
    void setQuickFilter(ObjectQuickFilter quickFilter);

    List<T> getObjects();
    List<T> getObjects(String name);
    T getObject(String name);
    T getObject(String name, int overload);
    T getObject(String name, String parentName);
}
