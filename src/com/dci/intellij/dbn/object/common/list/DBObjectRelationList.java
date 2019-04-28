package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.object.type.DBObjectRelationType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface DBObjectRelationList<T extends DBObjectRelation> extends DynamicContent<T> {
    DBObjectRelationType getObjectRelationType();
    @NotNull List<T> getObjectRelations();
    List<DBObjectRelation> getRelationBySourceName(String sourceName);
    List<DBObjectRelation> getRelationByTargetName(String targetName);
}
