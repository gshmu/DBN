package com.dci.intellij.dbn.object.filter.quick;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class QuickFilterCache {
    private final Map<String, ?> cache = new HashMap<>();

    public <T extends DBObject> void add(DBObjectList<T> list, @Nullable ObjectQuickFilter<T> filter) {

    }

    private Map<String, ?> find(BrowserTreeNode node) {
        return null;
    }

}
