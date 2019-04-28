package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.common.content.DynamicContentImpl;
import com.dci.intellij.dbn.common.content.DynamicContentStatus;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.content.dependency.ContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoaderImpl;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectRelationType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DBObjectRelationListImpl<T extends DBObjectRelation> extends DynamicContentImpl<T> implements DBObjectRelationList<T>{
    private DBObjectRelationType objectRelationType;

    public DBObjectRelationListImpl(
            @NotNull DBObjectRelationType type,
            @NotNull GenericDatabaseElement parent,
            ContentDependencyAdapter dependencyAdapter,
            DynamicContentStatus... statuses) {
        super(parent, dependencyAdapter, statuses);
        this.objectRelationType = type;
        //DBObjectListLoaderRegistry.register(parent, type, loader);
    }

    @Override
    public DynamicContentLoader<T, DBObjectMetadata> getLoader() {
        DynamicContentType parentContentType = getParentElement().getDynamicContentType();
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
        List<DBObjectRelation> objectRelations = new ArrayList<DBObjectRelation>();
        for (DBObjectRelation objectRelation : getAllElements()) {
            if (objectRelation.getSourceObject().getName().equals(sourceName)) {
                objectRelations.add(objectRelation);
            }
        }
        return objectRelations;
    }

    @Override
    public List<DBObjectRelation> getRelationByTargetName(String targetName) {
        List<DBObjectRelation> objectRelations = new ArrayList<DBObjectRelation>();
        for (DBObjectRelation objectRelation : getAllElements()) {
            if (objectRelation.getTargetObject().getName().equals(targetName)) {
                objectRelations.add(objectRelation);
            }
        }
        return objectRelations;
    }


    /*********************************************************
     *                   DynamicContent                      *
     *********************************************************/

    @Override
    public Project getProject() {
        return getParentElement().getProject();
    }

   @Override
   public String getContentDescription() {
        if (getParentElement() instanceof DBObject) {
            DBObject object = (DBObject) getParentElement();
            return getName() + " of " + object.getQualifiedNameWithType();
        }
       return getName() + " from " + getConnectionHandler().getName() ;
    }

    @Override
    public void notifyChangeListeners() {}
}
