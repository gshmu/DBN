package com.dci.intellij.dbn.code.common.style.options;

import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.stringAttribute;

@Getter
@Setter
@EqualsAndHashCode
public class CodeStyleCaseOption implements PersistentConfiguration {
    private String name;
    private boolean ignoreMixedCase;
    private CodeStyleCase styleCase;

    public CodeStyleCaseOption(String id, CodeStyleCase styleCase, boolean ignoreMixedCase) {
        this.name = id;
        this.styleCase = styleCase;
        this.ignoreMixedCase = ignoreMixedCase;
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
        return string.startsWith("`") || string.startsWith("'") || string.startsWith("\"") || (ignoreMixedCase && StringUtil.isMixedCase(string));
    }

    /*********************************************************
     *                 PersistentConfiguration               *
     *********************************************************/
    @Override
    public void readConfiguration(Element element) {
        name = stringAttribute(element, "name");
        String style = stringAttribute(element, "value");
        styleCase =
                style.equals("upper") ? CodeStyleCase.UPPER :
                style.equals("lower") ? CodeStyleCase.LOWER :
                style.equals("capitalized") ? CodeStyleCase.CAPITALIZED :
                style.equals("preserve") ? CodeStyleCase.PRESERVE : CodeStyleCase.PRESERVE;
    }

    @Override
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
