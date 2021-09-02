package com.dci.intellij.dbn.object.filter;

import com.dci.intellij.dbn.common.state.PersistentStateElement;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.intellij.openapi.util.text.StringUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.StringTokenizer;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.getEnumAttribute;
import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.setEnumAttribute;

@Getter
@Setter
@EqualsAndHashCode
public abstract class NameFilterCondition implements PersistentStateElement {
    private ConditionOperator operator = ConditionOperator.EQUAL;
    private String pattern;

    protected NameFilterCondition() {
    }

    public NameFilterCondition(ConditionOperator operator, String pattern) {
        this.operator = operator;
        this.pattern = pattern;
    }

    @NotNull
    public ConditionOperator getOperator() {
        return CommonUtil.nvl(operator, ConditionOperator.EQUAL);
    }

    public boolean accepts(String name) {
        switch (operator) {
            case EQUAL: return isEqual(name, pattern);
            case NOT_EQUAL: return !isEqual(name, pattern);
            case LIKE: return isLike(name, pattern);
            case NOT_LIKE: return !isLike(name, pattern);
        }
        return false;
    }

    private static boolean isEqual(String name, String pattern) {
        return StringUtil.equalsIgnoreCase(name, pattern);
    }

    private static boolean isLike(String name, String pattern) {
        StringTokenizer tokenizer = new StringTokenizer(pattern, "*%");
        int startIndex = 0;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            int index = StringUtil.indexOfIgnoreCase(name, token, startIndex);
            if (index == -1 || (index > 0 && startIndex == 0 && !startsWithWildcard(pattern))) return false;
            startIndex = index + token.length();
        }

        return true;
    }

    private static boolean startsWithWildcard(String pattern) {
        return pattern.indexOf('*') == 0 || pattern.indexOf('%') == 0;
    }

    @Override
    public void readState(Element element) {
        operator = getEnumAttribute(element, "operator", ConditionOperator.class);
        pattern = element.getAttributeValue("pattern");
    }

    @Override
    public void writeState(Element element) {
        setEnumAttribute(element, "operator", operator);
        element.setAttribute("pattern", pattern);
    }
}
