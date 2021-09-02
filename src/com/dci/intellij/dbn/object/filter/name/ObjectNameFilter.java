package com.dci.intellij.dbn.object.filter.name;

import com.dci.intellij.dbn.object.filter.ConditionOperator;
import com.dci.intellij.dbn.object.type.DBObjectType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class ObjectNameFilter extends CompoundFilterCondition {
    private DBObjectType objectType;

    @EqualsAndHashCode.Exclude
    private final ObjectNameFilterSettings settings;

    public ObjectNameFilter(ObjectNameFilterSettings settings) {
        this.settings = settings;
    }

    public ObjectNameFilter(ObjectNameFilterSettings settings, DBObjectType objectType, ConditionOperator operator, String text) {
        this.settings = settings;
        this.objectType = objectType;
        addCondition(operator, text);
    }

    public ObjectNameFilter(ObjectNameFilterSettings settings, DBObjectType objectType, SimpleNameFilterCondition condition) {
        this.settings = settings;
        this.objectType = objectType;
        addCondition(condition);
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        if (getConditions().isEmpty()) {
            settings.removeFilter(this);
        }
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    @Override
    public void readConfiguration(Element element) {
        super.readConfiguration(element);
        objectType = DBObjectType.get(element.getAttributeValue("object-type"));
    }

    @Override
    public void writeConfiguration(Element element) {
        super.writeConfiguration(element);
        element.setAttribute("object-type", objectType.getName());
    }
}
