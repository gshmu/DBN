package com.dci.intellij.dbn.execution.method;

import java.util.ArrayList;
import java.util.List;
import org.jdom.CDATA;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Text;

import com.dci.intellij.dbn.common.list.MostRecentStack;
import com.dci.intellij.dbn.common.state.PersistentStateElement;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.StringUtil;

public class MethodExecutionVariable implements PersistentStateElement<Element>, Cloneable, ArgumentValueStore<String> {
    private MostRecentStack<String> valueHistory = new MostRecentStack<String>();

    public MethodExecutionVariable() {
    }

    public MethodExecutionVariable(Element element) {
        readState(element);
    }

    public MethodExecutionVariable(MethodExecutionVariable source) {
        valueHistory.setValues(source.valueHistory.values());
    }

    public List<String> getValueHistory() {
        return valueHistory.values();
    }

    public String getValue() {
        return valueHistory.get();
    }

    public void setValue(String value) {
        valueHistory.stack(value);
    }

    @Override
    public void readState(Element element) {
        List<String> values = new ArrayList<String>();
        String value = CommonUtil.nullIfEmpty(element.getAttributeValue("value"));
        if (StringUtil.isNotEmpty(value)) {
            values.add(0, value);
        }

        List<Element> valueElements = element.getChildren();
        for (Element valueElement : valueElements) {
            Content content = valueElement.getContent(0);
            if (content instanceof Text) {
                Text cdata = (Text) content;
                value = cdata.getText();
                if (StringUtil.isNotEmpty(value)) {
                    values.add(0, value);
                }
            }
        }
        valueHistory = new MostRecentStack<>(values);
    }

    @Override
    public void writeState(Element element) {
        for (String value : valueHistory) {
            Element valueElement = new Element("value");
            element.addContent(valueElement);

            CDATA cdata = new CDATA(value);
            valueElement.setContent(cdata);
        }
    }

    @Override
    protected MethodExecutionVariable clone() {
        return new MethodExecutionVariable(this);
    }
}
