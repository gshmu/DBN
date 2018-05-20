package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.common.content.DynamicContentImpl;
import com.dci.intellij.dbn.common.content.DynamicContentStatus;
import com.dci.intellij.dbn.common.content.dependency.ContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectRelationType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DBObjectRelationListImpl<T extends DBObjectRelation> extends DynamicContentImpl<T> implements DBObjectRelationList<T>{
    private DBObjectRelationType objectRelationType;
    private String name;

    public DBObjectRelationListImpl(DBObjectRelationType type, @NotNull GenericDatabaseElement parent, String name, DynamicContentLoader<T> loader, ContentDependencyAdapter dependencyAdapter, DynamicContentStatus ... statuses) {
        super(parent, loader, dependencyAdapter, statuses);
        this.objectRelationType = type;
        this.name = name;
    }

    @NotNull
    public List<T> getObjectRelations() {
        return getAllElements();
    }

    @Nullable
    public Filter getFilter() {
        return null;
    }

    public DBObjectRelationType getObjectRelationType() {
        return objectRelationType;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name + " - " + super.toString();
    }

    public List<DBObjectRelation> getRelationBySourceName(String sourceName) {
        List<DBObjectRelation> objectRelations = new ArrayList<DBObjectRelation>();
        for (DBObjectRelation objectRelation : getAllElements()) {
            if (objectRelation.getSourceObject().getName().equals(sourceName)) {
                objectRelations.add(objectRelation);
            }
        }
        return objectRelations;
    }

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

    public Project getProject() {
        return getParentElement().getProject();
    }

   public String getContentDescription() {
        if (getParentElement() instanceof DBObject) {
            DBObject object = (DBObject) getParentElement();
            return name + " of " + object.getQualifiedNameWithType();
        }
       return name + " from " + getConnectionHandler().getName() ;
    }

    public void notifyChangeListeners() {}
}
