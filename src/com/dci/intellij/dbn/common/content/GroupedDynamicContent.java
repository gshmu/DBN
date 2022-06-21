package com.dci.intellij.dbn.common.content;

import java.util.List;

public interface GroupedDynamicContent<T extends DynamicContentElement> extends DynamicContent<T> {
    List<T> getChildElements(String parentName);
}
