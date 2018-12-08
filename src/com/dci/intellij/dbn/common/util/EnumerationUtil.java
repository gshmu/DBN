package com.dci.intellij.dbn.common.util;

public class EnumerationUtil {
    public static <T extends Enum<T>> boolean isOneOf(Enum<T> enumeration, Enum<T> ... values) {
        if (values != null && values.length > 0) {
            for (Enum value : values) {
                if (value == enumeration) return true;
            }
        }
        return false;
    }
}
