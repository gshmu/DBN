package com.dci.intellij.dbn.object.filter.quick;

import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.filter.ConditionOperator;
import com.dci.intellij.dbn.object.filter.NameFilterCondition;

public class ObjectQuickFilterCondition extends NameFilterCondition {
    private ObjectQuickFilter filter;
    private boolean active = true;

    public ObjectQuickFilterCondition(ObjectQuickFilter filter, ConditionOperator operator, String pattern, boolean active) {
        super(operator, pattern);
        this.filter = filter;
        this.active = active;
    }

    public ObjectQuickFilter getFilter() {
        return filter;
    }

    public void setFilter(ObjectQuickFilter filter) {
        this.filter = filter;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean accepts(DBObject object) {
        return !active || accepts(object.getName());
    }
}
