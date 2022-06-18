package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.connection.context.ConnectionProvider;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.sorting.DBObjectComparator;
import com.dci.intellij.dbn.object.filter.quick.ObjectQuickFilter;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public interface DBObjectList<T extends DBObject> extends BrowserTreeNode, DynamicContent<T>, Comparable<DBObjectList>, ConnectionProvider {

    PsiDirectory getPsiDirectory();

    DBObjectType getObjectType();

    void addObject(T object);

    boolean isInternal();

    boolean isHidden();

    boolean isDependency();

    @Nullable
    Filter<T> getConfigFilter();

    @Nullable
    ObjectQuickFilter<T> getQuickFilter();

    void setQuickFilter(@Nullable ObjectQuickFilter<T> quickFilter);

    List<T> getObjects();

    List<T> getObjects(String name);

    T getObject(String name);

    T getObject(String name, short overload);

    void collectObjects(Consumer<? super DBObject> consumer);

    void sort(DBObjectComparator<T> comparator);
}
