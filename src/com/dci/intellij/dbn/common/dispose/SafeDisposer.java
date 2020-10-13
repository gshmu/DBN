package com.dci.intellij.dbn.common.dispose;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.latent.MapLatent;
import com.dci.intellij.dbn.common.list.FiltrableList;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.thread.Background;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.thread.ThreadMonitor;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.util.Unsafe;
import com.dci.intellij.dbn.vfs.DBVirtualFile;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.NamedComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ReflectionUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Timer;

public interface SafeDisposer {
    MapLatent<Class<?>, List<Field>, RuntimeException> CLASS_FIELDS = MapLatent.create(clazz -> ReflectionUtil.collectFields(clazz));
    Logger LOGGER = LoggerFactory.createLogger();

    static void register(@Nullable Disposable parent, @NotNull Disposable disposable) {
        if (Failsafe.check(parent)) {
            Disposer.register(parent, disposable);
        }
    }

    static void dispose(@Nullable Disposable disposable) {
        dispose(disposable, true);
    }

    static void dispose(@Nullable Disposable disposable, boolean registered) {
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

    static <T extends Disposable> T replace(T oldElement, T newElement, boolean registered) {
        dispose(oldElement, registered, true);
        return newElement;
    }

    static void dispose(@Nullable Disposable disposable, boolean registered, boolean background) {
        if (disposable != null) {
            if (background && !ThreadMonitor.isBackgroundProcess()) {
                Background.run(() -> dispose(disposable, registered, false));
            } else {
                dispose(disposable, registered);
            }
        }
    }

    static void dispose(@Nullable Collection<?> collection, boolean registered, boolean background) {
        if (collection != null) {
            if (background && !ThreadMonitor.isBackgroundProcess()) {
                Background.run(() -> dispose(collection, registered, false));
            } else {
                Collection<?> disposeCollection;
                if (collection instanceof FiltrableList) {
                    FiltrableList<?> filtrableList = (FiltrableList<?>) collection;
                    disposeCollection = new ArrayList<>(filtrableList.getFullList());
                } else {
                    disposeCollection = new ArrayList<>(collection);
                }
                if (disposeCollection.size()> 0) {
                    clearCollection(collection);

                    for (Object object : disposeCollection) {
                        if (object instanceof Disposable) {
                            Disposable disposable = (Disposable) object;
                            dispose(disposable, registered);
                        }
                    }
                }
            }
        }
    }

    static void dispose(@Nullable Map<?, ?> map, boolean registered, boolean background) {
        if (map != null && !map.isEmpty()) {
            Collection<?> collection = new ArrayList<>(map.values());
            dispose(collection, registered, background);
            clearMap(map);
        }
    }

    static void clearCollection(Collection<?> collection) {
        Unsafe.silent(() -> collection.clear());
    }

    static void clearMap(Map<?, ?> map) {
        Unsafe.silent(() -> map.clear());
    }

    static void dispose(@Nullable Component component) {
        if (component != null) {
            Dispatch.runConditional(() -> {
                GUIUtil.removeListeners(component);
                UIUtil.dispose(component);
                if (component instanceof Container) {
                    Container container = (Container) component;
                    Component[] components = container.getComponents();
                    Arrays.stream(components).forEach(child -> dispose(child));
                    container.removeAll();
                }
            });
        }
    }

    static void dispose(@Nullable Timer timer) {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    static void dispose(@Nullable DBVirtualFile virtualFile) {
        if (virtualFile != null) {
            virtualFile.invalidate();
        }
    }

    static void nullify(Object object) {
        nullify(object, null);
    }

    static void nullify(Object object, @Nullable Runnable callback) {
        try {
            List<Field> fields = CLASS_FIELDS.get(object.getClass());
            for (Field field : fields) {
                try {
                    field.setAccessible(true);
                    Object fieldValue = field.get(object);
                    if ( fieldValue != null) {
                        if (fieldValue instanceof Collection<?>) {
                            Collection collection = (Collection) fieldValue;
                            clearCollection(collection);
                        } else if (fieldValue instanceof Map) {
                            Map map = (Map) fieldValue;
                            clearMap(map);
                        } else if (fieldValue instanceof Latent){
                            Latent latent = (Latent) fieldValue;
                            latent.reset();
                        } else if (fieldValue instanceof MapLatent){
                            MapLatent latent = (MapLatent) fieldValue;
                            latent.reset();
                        } else {
                            int modifiers = field.getModifiers();
                            if (!Modifier.isFinal(modifiers) &&
                                    !Modifier.isStatic(modifiers) &&
                                    !Modifier.isNative(modifiers) &&
                                    !Modifier.isTransient(modifiers) &&
                                    (//fieldValue instanceof Disposable ||
                                     //fieldValue instanceof Component ||
                                     fieldValue instanceof Editor ||
                                     fieldValue instanceof Document ||
                                     fieldValue instanceof VirtualFile ||
                                     fieldValue instanceof Configuration ||
                                     fieldValue instanceof AutoCloseable ||
                                     fieldValue instanceof NamedComponent)) {

                                field.set(object, null);
                            }
                        }

                    }
                } catch (UnsupportedOperationException ignore) {
                } catch (Throwable e) {
                    LOGGER.error("Failed to nullify field", e);
                }
            }
        } finally {
            if (callback != null) callback.run();
        }
    }
}
