package com.dci.intellij.dbn.object.filter;

import java.util.StringTokenizer;

import com.intellij.openapi.util.text.StringUtil;

public abstract class NameFilterCondition {
    private ConditionOperator operator = ConditionOperator.EQUAL;
    private String pattern;

    public NameFilterCondition() {
    }

    public NameFilterCondition(ConditionOperator operator, String pattern) {
        this.operator = operator;
        this.pattern = pattern;
    }

    public ConditionOperator getOperator() {
        return operator;
    }

    public void setOperator(ConditionOperator operator) {
        this.operator = operator;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
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
        return name.equalsIgnoreCase(pattern);
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
}
