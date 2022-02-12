package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.common.content.DynamicContentImpl;
import com.dci.intellij.dbn.common.content.DynamicContentStatus;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.content.dependency.ContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoaderImpl;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.connection.DatabaseEntity;
import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.type.DBObjectRelationType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class DBObjectRelationListImpl<T extends DBObjectRelation> extends DynamicContentImpl<T> implements DBObjectRelationList<T>{
    private final DBObjectRelationType objectRelationType;

    public DBObjectRelationListImpl(
            @NotNull DBObjectRelationType type,
            @NotNull DatabaseEntity parent,
            ContentDependencyAdapter dependencyAdapter,
            DynamicContentStatus... statuses) {
        super(parent, dependencyAdapter, statuses);
        this.objectRelationType = type;
        //DBObjectListLoaderRegistry.register(parent, type, loader);
    }

    @Override
    public DynamicContentLoader<T, DBObjectMetadata> getLoader() {
        DynamicContentType<?> parentContentType = getParentEntity().getDynamicContentType();
        return DynamicContentLoaderImpl.resolve(parentContentType, objectRelationType);
    }

    @Override
    @NotNull
    public List<T> getObjectRelations() {
        return getAllElements();
    }

    @Override
    @Nullable
    public Filter getFilter() {
        return null;
    }

    @Override
    public DynamicContentType getContentType() {
        return objectRelationType;
    }

    @Override
    public DBObjectRelationType getObjectRelationType() {
        return objectRelationType;
    }

    @NotNull
    @Override
    public String getName() {
        return
                objectRelationType.getSourceType().getName() + " " +
                objectRelationType.getTargetType().getListName();
    }

    public String toString() {
        return objectRelationType + " - " + super.toString();
    }

    @Override
    public List<DBObjectRelation> getRelationBySourceName(String sourceName) {
        List<DBObjectRelation> objectRelations = new ArrayList<>();
        for (DBObjectRelation objectRelation : getAllElements()) {
            if (Objects.equals(objectRelation.getSourceObject().getName(), sourceName)) {
                objectRelations.add(objectRelation);
            }
        }
        return objectRelations;
    }

    @Override
    public List<DBObjectRelation> getRelationByTargetName(String targetName) {
        List<DBObjectRelation> objectRelations = new ArrayList<>();
        for (DBObjectRelation objectRelation : getAllElements()) {
            if (Objects.equals(objectRelation.getTargetObject().getName(), targetName)) {
                objectRelations.add(objectRelation);
            }
        }
        return objectRelations;
    }


    /*********************************************************
     *                   DynamicContent                      *
     *********************************************************/

    @NotNull
    @Override
    public Project getProject() {
        return getParentEntity().getProject();
    }

   @Override
   public String getContentDescription() {
        if (getParentEntity() instanceof DBObject) {
            DBObject object = (DBObject) getParentEntity();
            return getName() + " of " + object.getQualifiedNameWithType();
        }
       return getName() + " from " + this.getConnection().getName() ;
    }

    @Override
    public void notifyChangeListeners() {}
}
