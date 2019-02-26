package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.filter.quick.ObjectQuickFilter;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface DBObjectList<T extends DBObject> extends BrowserTreeNode, DynamicContent<T>, Comparable<DBObjectList>, ConnectionProvider {
    @Override
    @NotNull String getName();

    PsiDirectory getPsiDirectory();

    DBObjectType getObjectType();
    void addObject(T object);

    boolean isFiltered();

    boolean isInternal();

    @Nullable
    Filter<T> getConfigFilter();

    @Nullable
    ObjectQuickFilter getQuickFilter();

    void setQuickFilter(@Nullable ObjectQuickFilter quickFilter);

    List<T> getObjects();
    List<T> getObjects(String name);
    T getObject(String name);
    T getObject(String name, int overload);
    T getObject(String name, String parentName);
}
