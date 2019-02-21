package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectRelationType;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.common.util.CommonUtil.nvl;

public abstract class DBObjectRelationImpl<S extends DBObject, T extends DBObject> implements DBObjectRelation<S, T> {

    private DBObjectRelationType objectRelationType;
    private DBObjectRef<S> sourceObject;
    private DBObjectRef<T> targetObject;

    public DBObjectRelationImpl(DBObjectRelationType objectRelationType, S sourceObject, T targetObject) {
        this.objectRelationType = objectRelationType;
        assert sourceObject.getObjectType() == objectRelationType.getSourceType();
        assert targetObject.getObjectType() == objectRelationType.getTargetType();
        this.sourceObject = DBObjectRef.from(sourceObject);
        this.targetObject = DBObjectRef.from(targetObject);
    }



    @Override
    public DBObjectRelationType getObjectRelationType() {
        return objectRelationType;
    }

    @Override
    public S getSourceObject() {
        return DBObjectRef.get(sourceObject);
    }

    @Override
    public T getTargetObject() {
        return DBObjectRef.get(targetObject);
    }

    public String toString() {
        String sourceObjectName = sourceObject.getQualifiedNameWithType();
        String targetObjectName = targetObject.getQualifiedNameWithType();
        return nvl(sourceObjectName, "UNKNOWN") + " => " + nvl(targetObjectName, "UNKNOWN");
    }

    /*********************************************************
    *               DynamicContentElement                   *
    *********************************************************/
    @NotNull
    @Override
    public String getName() {
        String sourceObjectName = sourceObject.getQualifiedNameWithType();
        String targetObjectName = targetObject.getQualifiedNameWithType();
        return nvl(sourceObjectName, "UNKNOWN") + "." + nvl(targetObjectName, "UNKNOWN");
    }

    @Override
    public int getOverload() {
        return 0;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean isDisposed() {
        return getSourceObject() == null || getTargetObject() == null;
    }

    @Override
    public void reload() {
    }

    @Override
    public void refresh() {

    }

    @Override
    public int compareTo(@NotNull Object o) {
        DBObjectRelationImpl remote = (DBObjectRelationImpl) o;
        return sourceObject.compareTo(remote.sourceObject);
    }

}
