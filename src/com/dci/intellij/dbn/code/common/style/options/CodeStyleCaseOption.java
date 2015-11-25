package com.dci.intellij.dbn.code.common.style.options;

import org.jdom.Element;

import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.common.util.StringUtil;

public class CodeStyleCaseOption implements PersistentConfiguration {
    private String name;
    private boolean ignoreMixedCase;
    private CodeStyleCase styleCase;

    public CodeStyleCaseOption(String id, CodeStyleCase styleCase, boolean ignoreMixedCase) {
        this.name = id;
        this.styleCase = styleCase;
        this.ignoreMixedCase = ignoreMixedCase;
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

    public String format(String string) {
        if (string != null) {
            switch (styleCase) {
                case UPPER: return ignore(string) ? string : string.toUpperCase();
                case LOWER: return ignore(string) ? string : string.toLowerCase();
                case CAPITALIZED: return ignore(string) ? string : NamingUtil.capitalize(string);
                case PRESERVE: return string;
            }
        }
        return null;
    }

    boolean ignore(String string) {
        return ignoreMixedCase && StringUtil.isMixedCase(string);
    }

    /*********************************************************
     *                 PersistentConfiguration               *
     *********************************************************/
    public void readConfiguration(Element element) {
        name = element.getAttributeValue("name");
        String style = element.getAttributeValue("value");
        styleCase =
                style.equals("upper") ? CodeStyleCase.UPPER :
                style.equals("lower") ? CodeStyleCase.LOWER :
                style.equals("capitalized") ? CodeStyleCase.CAPITALIZED :
                style.equals("preserve") ? CodeStyleCase.PRESERVE : CodeStyleCase.PRESERVE;
    }

    public void writeConfiguration(Element element) {
        String value =
                styleCase == CodeStyleCase.UPPER ? "upper" :
                styleCase == CodeStyleCase.LOWER ? "lower" :
                styleCase == CodeStyleCase.CAPITALIZED ? "capitalized" :
                styleCase == CodeStyleCase.PRESERVE ? "preserve" :  "preserve";

        element.setAttribute("name", name);
        element.setAttribute("value", value);
    }
}
