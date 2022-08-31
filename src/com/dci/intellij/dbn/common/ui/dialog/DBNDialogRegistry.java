package com.dci.intellij.dbn.common.ui.dialog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static com.dci.intellij.dbn.common.ui.dialog.DBNDialogListener.Action.CLOSE;

public class DBNDialogRegistry {
    private static final Map<Object, DBNDialog> cache = new ConcurrentHashMap<>();

    public static <T extends DBNDialog> T ensure(Object key, Supplier<T> provider) {
        DBNDialog dialog = cache.computeIfAbsent(key, k -> provider.get());
        dialog.addDialogListener(action -> {
            if (action == CLOSE) cache.remove(key);
        });
        return (T) dialog;
    }

}
