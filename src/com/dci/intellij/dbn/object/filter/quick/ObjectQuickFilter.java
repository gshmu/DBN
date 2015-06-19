package com.dci.intellij.dbn.object.filter.quick;

import java.util.ArrayList;
import java.util.List;

import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.util.Cloneable;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.filter.ConditionJoinType;
import com.dci.intellij.dbn.object.filter.ConditionOperator;

public class ObjectQuickFilter<T extends DBObject> extends Filter<T> implements Cloneable<ObjectQuickFilter> {
    private DBObjectType objectType;
    private ConditionJoinType joinType = ConditionJoinType.AND;
    private List<ObjectQuickFilterCondition> conditions = new ArrayList<ObjectQuickFilterCondition>();

    private ObjectQuickFilter(DBObjectType objectType, ConditionJoinType joinType) {
        this.objectType = objectType;
        this.joinType = joinType;
    }

    public ObjectQuickFilter(DBObjectType objectType) {
        this.objectType = objectType;
    }

    public DBObjectType getObjectType() {
        return objectType;
    }

    public ObjectQuickFilterCondition addCondition(ConditionOperator operator, String pattern) {
        ObjectQuickFilterCondition condition = new ObjectQuickFilterCondition(this, operator, pattern);
        conditions.add(condition);
        return condition;
    }

    public List<ObjectQuickFilterCondition> getConditions() {
        return conditions;
    }

    public ConditionJoinType getJoinType() {
        return joinType;
    }

    public void setJoinType(ConditionJoinType joinType) {
        this.joinType = joinType;
    }

    @Override
    public boolean accepts(T object) {
        if (conditions.size() > 0) {
            if (joinType == ConditionJoinType.AND) {
                for (ObjectQuickFilterCondition condition : conditions) {
                    if (!condition.accepts(object)) return false;
                }
                return true;
            } else if (joinType == ConditionJoinType.OR) {
                for (ObjectQuickFilterCondition condition : conditions) {
                    if (condition.accepts(object)) return true;
                }
                return false;
            }
        }
        return true;
    }

    @Override
    public ObjectQuickFilter clone() {
        ObjectQuickFilter filterClone = new ObjectQuickFilter(objectType, joinType);
        for (ObjectQuickFilterCondition condition : conditions) {
            filterClone.addCondition(condition.getOperator(), condition.getPattern());
        }
        return filterClone;
    }
}
