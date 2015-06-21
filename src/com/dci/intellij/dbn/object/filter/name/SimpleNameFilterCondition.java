package com.dci.intellij.dbn.object.filter.name;

import org.jdom.Element;

import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.filter.ConditionOperator;
import com.dci.intellij.dbn.object.filter.NameFilterCondition;

public class SimpleNameFilterCondition extends NameFilterCondition implements FilterCondition {
    private CompoundFilterCondition parent;
    public SimpleNameFilterCondition() {
    }


    public SimpleNameFilterCondition(ConditionOperator operator, String pattern) {
        super(operator, pattern);
    }

    public ObjectNameFilterSettings getSettings() {
        return parent.getSettings();
    }

    public boolean accepts(DBObject object) {
        return accepts(object.getName());
    }

    public void setParent(CompoundFilterCondition parent) {
        this.parent = parent;
    }

    public CompoundFilterCondition getParent() {
        return parent;
    }

    public DBObjectType getObjectType() {
        return parent.getObjectType();
    }

    public String getConditionString() {
        return "OBJECT_NAME " + getOperator() + " '" + getPattern() + "'";
    }

    public String toString() {
        return getObjectType().getName().toUpperCase() + "_NAME " + getOperator() + " '" + getPattern() + "'";
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    public void readConfiguration(Element element) {
        setOperator(ConditionOperator.valueOf(element.getAttributeValue("operator")));
        setPattern(element.getAttributeValue("text"));
    }

    public void writeConfiguration(Element element) {
        element.setAttribute("operator", getOperator().name());
        element.setAttribute("text", getPattern());
    }
}
