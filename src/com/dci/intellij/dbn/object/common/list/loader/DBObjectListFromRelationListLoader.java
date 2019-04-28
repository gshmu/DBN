package com.dci.intellij.dbn.object.common.list.loader;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentElement;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.content.loader.DynamicSubcontentCustomLoader;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectRelation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DBObjectListFromRelationListLoader<
                T extends DynamicContentElement,
                M extends DBObjectMetadata>
        extends DynamicSubcontentCustomLoader<T, M> {

    private DBObjectListFromRelationListLoader(@Nullable DynamicContentType parentContentType, @NotNull DynamicContentType contentType) {
        super(parentContentType, contentType);
    }

    public static <T extends DynamicContentElement, M extends DBObjectMetadata> DBObjectListFromRelationListLoader<T, M> create(
            @Nullable DynamicContentType parentContentType,
            @NotNull DynamicContentType contentType) {
        return new DBObjectListFromRelationListLoader<>(parentContentType, contentType);
    }

    @Override
    public T resolveElement(DynamicContent<T> dynamicContent, DynamicContentElement sourceElement) {
        DBObjectList objectList = (DBObjectList) dynamicContent;
        DBObjectRelation objectRelation = (DBObjectRelation) sourceElement;
        DBObject object = (DBObject) objectList.getParent();

        if (CommonUtil.safeEqual(object, objectRelation.getSourceObject())) {
            return (T) objectRelation.getTargetObject();
        }
        if (CommonUtil.safeEqual(object, objectRelation.getTargetObject())) {
            return (T) objectRelation.getSourceObject();
        }

        return null;
    }
}
