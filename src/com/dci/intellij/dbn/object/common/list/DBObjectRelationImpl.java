package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.object.type.DBObjectRelationType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.common.util.Commons.nvl;

@Getter
public abstract class DBObjectRelationImpl<S extends DBObject, T extends DBObject> extends StatefulDisposable.Base implements DBObjectRelation<S, T> {

    private final DBObjectRelationType relationType;
    private final DBObjectRef<S> sourceObject;
    private final DBObjectRef<T> targetObject;

    public DBObjectRelationImpl(DBObjectRelationType relationType, S sourceObject, T targetObject) {
        this.relationType = relationType;
        assert sourceObject.getObjectType() == relationType.getSourceType();
        assert targetObject.getObjectType() == relationType.getTargetType();
        this.sourceObject = DBObjectRef.of(sourceObject);
        this.targetObject = DBObjectRef.of(targetObject);
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
    public short getOverload() {
        return 0;
    }

    @Override
    public String getDescription() {
        return null;
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


    @Override
    protected void disposeInner() {
        nullify();
    }
}
