package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;

import java.util.List;

public interface DBObjectList<T extends DBObject> extends BrowserTreeNode, DynamicContent<T> {
    String getName();
    DBObjectType getObjectType();
    boolean isHidden();
    void setHidden(boolean hidden);
    void addObject(T object);
    List<T> getObjects();
    List<T> getObjects(String name);
    T getObject(String name);
    T getObject(String name, String parentName);
}
