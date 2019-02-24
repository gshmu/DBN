package com.dci.intellij.dbn.common.dispose;

import com.dci.intellij.dbn.common.list.FiltrableList;
import com.dci.intellij.dbn.common.thread.Background;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public class DisposerUtil {

    public static void disposeInBackground(final Disposable disposable) {
        Background.run(() -> dispose(disposable));
    }


    public static void dispose(@Nullable Disposable disposable) {
        if (disposable != null) {
            Failsafe.lenient(() -> Disposer.dispose(disposable));
        }
    }

    public static void dispose(@Nullable Disposable[] array) {
        if (array != null && array.length> 0) {
            for(Disposable disposable : array) {
                dispose(disposable);
            }
        }
    }

    public static void disposeInBackground(Collection<? extends Disposable> collection) {
        Background.run(() -> dispose(collection));
    }
    
    public static void dispose(Collection<? extends Disposable> collection) {
        if (collection instanceof FiltrableList) {
            FiltrableList<? extends Disposable> filtrableList = (FiltrableList) collection;
            collection = filtrableList.getFullList();
        }
        if (collection != null && collection.size()> 0) {
            for(Disposable disposable : collection) {
                dispose(disposable);
            }
        }
    }

    public static void dispose(Map<?, ? extends Disposable> map) {
        if (map != null) {
            for (Disposable disposable : map.values()) {
                dispose(disposable);
            }
            map.clear();
        }
    }


    public static void register(Disposable parent, Collection<? extends Disposable> collection) {
        for (Disposable disposable : collection) {
            Disposer.register(parent, disposable);
        }
    }

    public static void register(Disposable parent, Object disposable) {
        if (disposable instanceof Disposable) {
            Disposer.register(parent, (Disposable) disposable);
        }
    }

    public static void disposeLater(final Disposable disposable) {
        SimpleLaterInvocator.invoke(() -> Disposer.dispose(disposable));
    }
}