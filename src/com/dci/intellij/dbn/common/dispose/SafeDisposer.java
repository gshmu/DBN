package com.dci.intellij.dbn.common.dispose;

import com.dci.intellij.dbn.common.list.FilteredList;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.util.Unsafe;
import com.dci.intellij.dbn.vfs.DBVirtualFile;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.ui.UIUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Component;
import java.awt.Container;
import java.util.Collection;
import java.util.Map;
import java.util.Timer;

@Slf4j
public final class SafeDisposer {

    private SafeDisposer() {}

    public static void register(@Nullable Disposable parent, @NotNull Object object) {
        if (object instanceof Disposable) {
            Disposable disposable = (Disposable) object;
            register(parent, disposable);
        }
    }

    public static void register(@Nullable Disposable parent, @NotNull Disposable disposable) {
        if (parent != null) {
            if (Failsafe.check(parent)) {
                Disposer.register(parent, disposable);
            } else {
                // dispose if parent already disposed
                Disposer.dispose(disposable);
            }
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
            if (Failsafe.check(disposable)) {
                if (registered) {
                    Disposer.dispose(disposable);
                } else {
                    disposable.dispose();
                }
            }
        } catch (Throwable e) {
            log.warn("Failed to dispose entity {}", disposable, e);
        }
    }

    public static <T> T replace(T oldElement, T newElement, boolean registered) {
        dispose(oldElement, registered);
        return newElement;
    }

    public static void dispose(@Nullable Object object, boolean registered) {
        if (object != null) {
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
    }

    public static void disposeCollection(@Nullable Collection<?> collection) {
        if (collection != null && !collection.isEmpty()) {
            BackgroundDisposer.queue(() -> {
                Collection<?> disposeCollection;
                if (collection instanceof FilteredList) {
                    FilteredList<?> filteredList = (FilteredList<?>) collection;
                    disposeCollection = filteredList.getBase();
                } else {
                    disposeCollection = collection;
                }
                if (!disposeCollection.isEmpty()) {
                    for (Object object : disposeCollection) {
                        if (object instanceof Disposable) {
                            Disposable disposable = (Disposable) object;
                            dispose(disposable, false);
                        }
                    }
                    Nullifier.clearCollection(collection);
                }
            });
        }
    }

    public static void disposeArray(@Nullable Object[] array) {
        if (array != null && array.length != 0) {
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
    }

    public static void disposeMap(@Nullable Map<?, ?> map) {
        if (map != null && !map.isEmpty()) {
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
    }

    public static void dispose(@Nullable Component component) {
        if (component != null) {
            Dispatch.runConditional(() -> {
                GUIUtil.removeListeners(component);
                UIUtil.dispose(component);
                if (component instanceof Container) {
                    Container container = (Container) component;
                    Component[] components = container.getComponents();
                    if (components.length > 0) {
                        for (Component child : components) {
                            dispose(child);
                        }
                        Unsafe.silent(() -> container.removeAll());
                    }
                }
            });
        }
    }

    public static void dispose(@Nullable Timer timer) {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    public static void dispose(@Nullable DBVirtualFile virtualFile) {
        if (virtualFile != null) {
            virtualFile.invalidate();
        }
    }

}
