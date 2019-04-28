package com.dci.intellij.dbn.object.filter.quick;

import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.state.PersistentStateElement;
import com.dci.intellij.dbn.common.util.Cloneable;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.filter.ConditionJoinType;
import com.dci.intellij.dbn.object.filter.ConditionOperator;
import com.dci.intellij.dbn.object.type.DBObjectType;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

public class ObjectQuickFilter implements Filter<DBObject>, Cloneable<ObjectQuickFilter>, PersistentStateElement {
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

    public ObjectQuickFilterCondition addNewCondition(ConditionOperator operator) {
        return addCondition(operator, "", true);
    }
    public ObjectQuickFilterCondition addCondition(ConditionOperator operator, String pattern, boolean active) {
        ObjectQuickFilterCondition condition = new ObjectQuickFilterCondition(this, operator, pattern, active);
        conditions.add(condition);
        return condition;
    }

    public void removeCondition(ObjectQuickFilterCondition condition) {
        conditions.remove(condition);
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

    public boolean isEmpty() {
        return conditions.isEmpty();
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

    @Override
    public ObjectQuickFilter clone() {
        ObjectQuickFilter filterClone = new ObjectQuickFilter(objectType, joinType);
        for (ObjectQuickFilterCondition condition : conditions) {
            filterClone.addCondition(
                    condition.getOperator(),
                    condition.getPattern(),
                    condition.isActive());
        }
        return filterClone;
    }

    @Override
    public void readState(Element element) {
        joinType = ConditionJoinType.valueOf(element.getAttributeValue("join-type"));
        for (Element conditionElement : element.getChildren()) {
            ObjectQuickFilterCondition condition = new ObjectQuickFilterCondition(this);
            condition.readState(conditionElement);
            conditions.add(condition);
        }
    }

    @Override
    public void writeState(Element element) {
        element.setAttribute("join-type", joinType.name());
        for (ObjectQuickFilterCondition condition : conditions) {
            Element conditionElement = new Element("condition");
            element.addContent(conditionElement);
            condition.writeState(conditionElement);
        }


    }

    public void clear() {
        conditions.clear();
    }
}
