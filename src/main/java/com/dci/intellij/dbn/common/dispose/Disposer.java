package com.dci.intellij.dbn.common.dispose;

import com.dci.intellij.dbn.common.list.FilteredList;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.util.Guarded;
import com.dci.intellij.dbn.vfs.DBVirtualFile;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.ui.tabs.JBTabs;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.dci.intellij.dbn.common.dispose.Checks.isNotValid;
import static com.dci.intellij.dbn.common.thread.ThreadMonitor.isDispatchThread;

@Slf4j
public final class Disposer {
    private static final List<Class<?>> DISPATCH_CANDIDATES = Arrays.asList(
            JBPopup.class,
            JBTabs.class
            /*, ...*/);

    private Disposer() {}

    public static void register(@Nullable Disposable parent, @NotNull Object object) {
        if (object instanceof Disposable) {
            Disposable disposable = (Disposable) object;
            register(parent, disposable);
        }
    }

    public static void register(@Nullable Disposable parent, @NotNull Disposable disposable) {
        if (parent == null) return;

        if (Checks.isValid(parent)) {
            com.intellij.openapi.util.Disposer.register(parent, disposable);
        } else {
            // dispose if parent already disposed
            com.intellij.openapi.util.Disposer.dispose(disposable);
        }
    }

    public static void dispose(@Nullable Object object) {
        if (object instanceof Disposable) {
            Disposable disposable = (Disposable) object;
            dispose(disposable);
        }
    }

    public static void dispose(@Nullable Disposable disposable) {
        dispose(disposable, true);
    }

    public static void dispose(@Nullable Disposable disposable, boolean registered) {
        try {
            Guarded.run(() -> {
                if (isNotValid(disposable)) return;

                if (isDispatchCandidate(disposable) && !isDispatchThread()) {
                    Dispatch.run(() -> dispose(disposable, registered));
                    return;
                }

                if (registered) {
                    com.intellij.openapi.util.Disposer.dispose(disposable);
                } else {
                    disposable.dispose();
                }
            });
        } catch (Throwable e) {
            log.warn("Failed to dispose entity {}", disposable, e);
        }
    }

    public static <T> T replace(T oldElement, T newElement, boolean registered) {
        dispose(oldElement, registered);
        return newElement;
    }

    public static void dispose(@Nullable Object object, boolean registered) {
        if (object == null) return;

        if (object instanceof Disposable) {
            Disposable disposable = (Disposable) object;
            BackgroundDisposer.queue(() -> dispose(disposable, registered));

        } else if (object instanceof Collection) {
            disposeCollection((Collection<?>) object);

        } else if (object instanceof Map) {
            disposeMap((Map) object);

        } else if (object.getClass().isArray()) {
            disposeArray((Object[]) object);
        }
    }

    public static void disposeCollection(@Nullable Collection<?> collection) {
        if (collection == null) return;

        if (collection instanceof FilteredList) {
            FilteredList<?> filteredList = (FilteredList<?>) collection;
            collection = filteredList.getBase();
        }

        if (collection.isEmpty()) return;

        Collection<?> disposeCollection = collection;
        BackgroundDisposer.queue(() -> {
            for (Object object : disposeCollection) {
                if (object instanceof Disposable) {
                    Disposable disposable = (Disposable) object;
                    dispose(disposable, false);
                }
            }
            Nullifier.clearCollection(disposeCollection);
        });
    }

    public static void disposeArray(@Nullable Object[] array) {
        if (array == null || array.length == 0) return;

        BackgroundDisposer.queue(() -> {
            for (int i = 0; i < array.length; i++) {
                Object object = array[i];
                if (object instanceof Disposable) {
                    Disposable disposable = (Disposable) object;
                    dispose(disposable, false);
                }
                array[i] = null;
            }
        });
    }

    public static void disposeMap(@Nullable Map<?, ?> map) {
        if (map == null || map.isEmpty()) return;

        BackgroundDisposer.queue(() -> {
            for (Object object : map.values()) {
                if (object instanceof Disposable) {
                    Disposable disposable = (Disposable) object;
                    dispose(disposable, false);
                }
            }
            Nullifier.clearMap(map);
        });
    }

    public static void dispose(@Nullable Timer timer) {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    public static void dispose(@Nullable DBVirtualFile virtualFile) {
        if (virtualFile == null) return;

        virtualFile.invalidate();
    }

    private static boolean isDispatchCandidate(Object object) {
        for (Class<?> candidate : DISPATCH_CANDIDATES) {
            if (candidate.isAssignableFrom(object.getClass())) {
                return true;
            }
        }
        return false;
    }
}
