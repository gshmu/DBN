package com.dci.intellij.dbn.object.filter.quick;

import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.options.setting.Settings;
import com.dci.intellij.dbn.common.state.PersistentStateElement;
import com.dci.intellij.dbn.common.util.Cloneable;
import com.dci.intellij.dbn.common.util.Lists;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.filter.ConditionJoinType;
import com.dci.intellij.dbn.object.filter.ConditionOperator;
import com.dci.intellij.dbn.object.type.DBObjectType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class ObjectQuickFilter<T extends DBObject> implements Filter<T>, Cloneable<ObjectQuickFilter<T>>, PersistentStateElement {
    private final DBObjectType objectType;
    private final List<ObjectQuickFilterCondition> conditions = new ArrayList<>();
    private ConditionJoinType joinType = ConditionJoinType.AND;

    private ObjectQuickFilter(DBObjectType objectType, ConditionJoinType joinType) {
        this.objectType = objectType;
        this.joinType = joinType;
    }

    public ObjectQuickFilter(DBObjectType objectType) {
        this.objectType = objectType;
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

    public boolean isEmpty() {
        return conditions.isEmpty() || Lists.noneMatch(conditions, condition -> condition.isActive());
    }

    @Override
    public boolean accepts(DBObject object) {
        if (conditions.size() > 0) {
            if (joinType == ConditionJoinType.AND) {
                for (ObjectQuickFilterCondition condition : conditions) {
                    if (condition.isActive() && !condition.accepts(object)) return false;
                }
                return true;
            } else if (joinType == ConditionJoinType.OR) {
                for (ObjectQuickFilterCondition condition : conditions) {
                    if (condition.isActive() && condition.accepts(object)) return true;
                }
                return false;
            }
        }
        return true;
    }

    @Override
    public ObjectQuickFilter<T> clone() {
        ObjectQuickFilter<T> filterClone = new ObjectQuickFilter<>(objectType, joinType);
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
        joinType = Settings.enumAttribute(element, "join-type", ConditionJoinType.AND);
        for (Element child : element.getChildren()) {
            ObjectQuickFilterCondition condition = new ObjectQuickFilterCondition(this);
            condition.readState(child);
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
