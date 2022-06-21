package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.common.content.DynamicContentElement;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.type.DBObjectRelationType;

public interface DBObjectRelation<S extends DBObject, T extends DBObject> extends DynamicContentElement {
    DBObjectRelationType getRelationType();
    S getSourceObject();
    T getTargetObject();

    @Override
    default DynamicContentType getDynamicContentType() {
        return getRelationType();
    }
}
