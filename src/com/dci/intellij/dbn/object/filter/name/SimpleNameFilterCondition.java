package com.dci.intellij.dbn.object.filter.name;

import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.filter.ConditionOperator;
import com.dci.intellij.dbn.object.filter.NameFilterCondition;
import com.dci.intellij.dbn.object.type.DBObjectType;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.enumAttribute;

@Getter
@Setter
public class SimpleNameFilterCondition extends NameFilterCondition implements FilterCondition {
    private CompoundFilterCondition parent;

    public SimpleNameFilterCondition() {}

    public SimpleNameFilterCondition(ConditionOperator operator, String pattern) {
        super(operator, pattern);
    }

    @Override
    public ObjectNameFilterSettings getSettings() {
        return parent.getSettings();
    }

    @Override
    public boolean accepts(DBObject object) {
        return accepts(object.getName());
    }

    @Override
    public DBObjectType getObjectType() {
        return parent.getObjectType();
    }

    @Override
    public String getConditionString() {
        return "OBJECT_NAME " + getOperator() + " '" + getPattern() + "'";
    }

    public String toString() {
        return getObjectType().getName().toUpperCase() + "_NAME " + getOperator() + " '" + getPattern() + "'";
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    @Override
    public void readConfiguration(Element element) {
        setOperator(enumAttribute(element, "operator", ConditionOperator.class));
        setPattern(element.getAttributeValue("text"));
    }

    @Override
    public void writeConfiguration(Element element) {
        element.setAttribute("operator", getOperator().name());
        element.setAttribute("text", getPattern());
    }
}
