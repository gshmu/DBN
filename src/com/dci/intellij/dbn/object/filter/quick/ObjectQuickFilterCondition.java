package com.dci.intellij.dbn.object.filter.quick;

import org.jdom.Element;

import com.dci.intellij.dbn.common.state.PersistentStateElement;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.filter.ConditionOperator;
import com.dci.intellij.dbn.object.filter.NameFilterCondition;
import static com.dci.intellij.dbn.common.options.setting.SettingsUtil.getBooleanAttribute;
import static com.dci.intellij.dbn.common.options.setting.SettingsUtil.setBooleanAttribute;

public class ObjectQuickFilterCondition extends NameFilterCondition implements PersistentStateElement<Element>{
    private ObjectQuickFilter filter;
    private boolean active = true;

    public ObjectQuickFilterCondition(ObjectQuickFilter filter, ConditionOperator operator, String pattern, boolean active) {
        super(operator, pattern);
        this.filter = filter;
        this.active = active;
    }

    public ObjectQuickFilterCondition(ObjectQuickFilter filter) {
        this.filter = filter;
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

    public int index() {
        return filter.getConditions().indexOf(this);
    }

    @Override
    public void readState(Element element) {
        super.readState(element);
        active = getBooleanAttribute(element, "active", true);
    }

    @Override
    public void writeState(Element element) {
        super.writeState(element);
        setBooleanAttribute(element, "active", active);
    }

}
