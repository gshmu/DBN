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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Timer;

import static com.dci.intellij.dbn.common.dispose.BackgroundDisposer.queue;

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
        Unsafe.silent(() -> {
            if (Failsafe.check(disposable)) {
                if (registered) {
                    Disposer.dispose(disposable);
                } else {
                    disposable.dispose();
                }
            }
        });
    }

    public static <T> T replace(T oldElement, T newElement, boolean registered) {
        dispose(oldElement, registered, true);
        return newElement;
    }

    public static void dispose(@Nullable Object object, boolean registered, boolean background) {
        if (object instanceof Disposable) {
            Disposable disposable = (Disposable) object;
            if (background) {
                queue(() -> dispose(disposable, registered, false));
            } else {
                dispose(disposable, registered);
            }
        }
    }

    public static void dispose(@Nullable Collection<?> collection, boolean clear, boolean background) {
        if (collection != null) {
            if (background) {
                queue(() -> dispose(collection, true, false));
            } else {
                Collection<?> disposeCollection;
                if (collection instanceof FilteredList) {
                    FilteredList<?> filteredList = (FilteredList<?>) collection;
                    disposeCollection = filteredList.getBase();
                } else {
                    disposeCollection = collection;
                }
                if (!disposeCollection.isEmpty()) {
                    disposeCollection = new ArrayList<>(disposeCollection);
                    for (Object object : disposeCollection) {
                        if (object instanceof Disposable) {
                            Disposable disposable = (Disposable) object;
                            dispose(disposable, false);
                        }
                    }
                    if (clear) {
                        Nullifier.clearCollection(collection);
                    }
                }
            }
        }
    }

    public static void dispose(@Nullable Object[] array, boolean registered, boolean background) {
        if (array != null) {
            if (background) {
                queue(() -> dispose(array, registered, false));
            } else {
                for (int i = 0; i < array.length; i++) {
                    Object object = array[i];
                    if (object instanceof Disposable) {
                        Disposable disposable = (Disposable) object;
                        dispose(disposable, registered);
                    }
                    array[i] = null;
                }
            }
        }
    }

    public static void dispose(@Nullable Map<?, ?> map, boolean background) {
        if (map != null && !map.isEmpty()) {
            dispose(map.values(), true, background);
            Nullifier.clearMap(map);
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
