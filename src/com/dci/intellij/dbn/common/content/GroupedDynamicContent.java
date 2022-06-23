package com.dci.intellij.dbn.common.content;

import com.dci.intellij.dbn.connection.DatabaseEntity;

import java.util.List;

public interface GroupedDynamicContent<T extends DynamicContentElement> extends DynamicContent<T> {
    List<T> getChildElements(DatabaseEntity parentEntity);
}
