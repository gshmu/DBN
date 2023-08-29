package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.routine.ParametricCallable;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Classes {

    public static <P, R, E extends Throwable> R withClassLoader(P param, ParametricCallable<P, R, E> callable) throws E{
        Thread thread = Thread.currentThread();
        ClassLoader currentClassLoader = thread.getContextClassLoader();
        try {
            ClassLoader paramClassLoader = param.getClass().getClassLoader();
            thread.setContextClassLoader(paramClassLoader);
            return callable.call(param);
        } finally {
            thread.setContextClassLoader(currentClassLoader);
        }
    }
}
