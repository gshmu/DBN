package com.dci.intellij.dbn.code.common.style.options;

import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;

public class CodeStyleCaseOption implements PersistentConfiguration {
    private String name;
    private CodeStyleCase styleCase;

    public CodeStyleCaseOption(String id, CodeStyleCase styleCase) {
        this.name = id;
        this.styleCase = styleCase;
    }

    public CodeStyleCaseOption() {
    }

    public String getName() {
        return name;
    }

    public CodeStyleCase getStyleCase() {
        return styleCase;
    }

    public void setStyleCase(CodeStyleCase styleCase) {
        this.styleCase = styleCase;
    }

    public String changeCase(String string) {
        if (string != null) {
            switch (styleCase) {
                case UPPER: return string.toUpperCase();
                case LOWER: return string.toLowerCase();
                case CAPITALIZED: return NamingUtil.capitalize(string);
                case PRESERVE: return string;
            }
        }
        return string;
    }

    /*********************************************************
     *                   JDOMExternalizable                  *
     *********************************************************/
    public void readConfiguration(Element element) throws InvalidDataException {
        name = element.getAttributeValue("name");
        String style = element.getAttributeValue("value");
        styleCase =
                style.equals("upper") ? CodeStyleCase.UPPER :
                style.equals("lower") ? CodeStyleCase.LOWER :
                style.equals("capitalized") ? CodeStyleCase.CAPITALIZED :
                style.equals("preserve") ? CodeStyleCase.PRESERVE : CodeStyleCase.PRESERVE;
    }

    public void writeConfiguration(Element element) throws WriteExternalException {
        String value =
                styleCase == CodeStyleCase.UPPER ? "upper" :
                styleCase == CodeStyleCase.LOWER ? "lower" :
                styleCase == CodeStyleCase.CAPITALIZED ? "capitalized" :
                styleCase == CodeStyleCase.PRESERVE ? "preserve" :  null;

        element.setAttribute("name", name);
        element.setAttribute("value", value);
    }
}
