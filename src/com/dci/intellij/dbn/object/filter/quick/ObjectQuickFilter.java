package com.dci.intellij.dbn.object.filter.quick;

import java.util.ArrayList;
import java.util.List;

import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.filter.ConditionJoinType;
import com.dci.intellij.dbn.object.filter.ConditionOperator;

public class ObjectQuickFilter extends Filter<DBObject> {
    private DBObjectType objectType;
    private List<ObjectQuickFilterCondition> conditions = new ArrayList<ObjectQuickFilterCondition>();
    private ConditionJoinType joinType = ConditionJoinType.AND;

    public ObjectQuickFilter(DBObjectType objectType) {
        this.objectType = objectType;
    }

    public DBObjectType getObjectType() {
        return objectType;
    }

    public void addCondition(ConditionOperator operator, String pattern) {
        conditions.add(new ObjectQuickFilterCondition(this, operator, pattern));
    }

    public ConditionJoinType getJoinType() {
        return joinType;
    }

    public void setJoinType(ConditionJoinType joinType) {
        this.joinType = joinType;
    }

    @Override
    public boolean accepts(DBObject object) {
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
}
