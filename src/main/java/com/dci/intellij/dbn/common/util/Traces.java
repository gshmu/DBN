package com.dci.intellij.dbn.common.util;

import lombok.experimental.UtilityClass;

import java.util.Objects;

import static com.dci.intellij.dbn.diagnostics.Diagnostics.conditionallyLog;

@UtilityClass
public final class Traces {

    public static boolean isCalledThrough(Class clazz) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        try {
            for (int i = 3; i < stackTraceElements.length; i++) {
                StackTraceElement stackTraceElement = stackTraceElements[i];
                String className = stackTraceElement.getClassName();
                if (Objects.equals(clazz.getName(), className) /*|| clazz.isAssignableFrom(Class.forName(className))*/) {
                    return true;
                }
            }
        } catch (Exception e) {
            conditionallyLog(e);
            return false;
        }
        return false;
    }

    public static boolean isCalledThrough(Class clazz, String methodName) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        try {
            for (int i = 3; i < stackTraceElements.length; i++) {
                StackTraceElement stackTraceElement = stackTraceElements[i];
                String className = stackTraceElement.getClassName();
                if (Objects.equals(clazz.getName(), className) /*|| clazz.isAssignableFrom(Class.forName(className))*/) {
                    String methName = stackTraceElement.getMethodName();
                    if (Objects.equals(methodName, methName)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            conditionallyLog(e);
            return false;
        }
        return false;
    }
}
