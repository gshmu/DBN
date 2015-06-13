package com.dci.intellij.dbn.object.filter.quick;

import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.filter.ConditionOperator;
import com.dci.intellij.dbn.object.filter.NameFilterCondition;

public class ObjectQuickFilterCondition extends NameFilterCondition{
    private ObjectQuickFilter filter;

    public ObjectQuickFilter getFilter() {
        return filter;
    }

    public ObjectQuickFilterCondition(ObjectQuickFilter filter, ConditionOperator operator, String pattern) {
        super(operator, pattern);
        this.filter = filter;
    }

    public boolean accepts(DBObject object) {
        return accepts(object.getName());
    }
}
