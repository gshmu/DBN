package com.dci.intellij.dbn.execution.method;

import com.dci.intellij.dbn.common.list.MostRecentStack;
import com.dci.intellij.dbn.common.options.setting.SettingsSupport;
import com.dci.intellij.dbn.common.state.PersistentStateElement;
import com.dci.intellij.dbn.common.util.Cloneable;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jdom.CDATA;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.stringAttribute;

@Data
public class MethodExecutionArgumentValue implements PersistentStateElement, Cloneable<MethodExecutionArgumentValue>, ArgumentValueHolder<String> {
    private String name;

    @EqualsAndHashCode.Exclude
    private MostRecentStack<String> valueHistory = new MostRecentStack<>();

    public MethodExecutionArgumentValue(String name) {
        this.name = name;
    }

    public MethodExecutionArgumentValue(Element element) {
        readState(element);
    }

    public MethodExecutionArgumentValue(MethodExecutionArgumentValue source) {
        name = source.name;
        valueHistory.setValues(source.valueHistory.values());
    }

    public List<String> getValueHistory() {
        return valueHistory.values();
    }

    @Override
    public String getValue() {
        return valueHistory.get();
    }

    @Override
    public void setValue(String value) {
        valueHistory.stack(value);
    }

    @Override
    public void readState(Element element) {
        name = stringAttribute(element, "name");
        List<String> values = new ArrayList<String>();
        String value = CommonUtil.nullIfEmpty(element.getAttributeValue("value"));
        if (StringUtil.isNotEmpty(value)) {
            values.add(0, value);
        }

        for (Element child : element.getChildren()) {
            value = SettingsSupport.readCdata(child);
            if (StringUtil.isNotEmpty(value)) {
                values.add(value);
            }
        }
        valueHistory = new MostRecentStack<>(values);
    }

    @Override
    public void writeState(Element element) {
        element.setAttribute("name", name);
        for (String value : valueHistory) {
            Element valueElement = new Element("value");
            element.addContent(valueElement);

            CDATA cdata = new CDATA(value);
            valueElement.setContent(cdata);
        }
    }

    @Override
    public MethodExecutionArgumentValue clone() {
        return new MethodExecutionArgumentValue(this);
    }
}
