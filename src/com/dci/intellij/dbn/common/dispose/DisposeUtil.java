package com.dci.intellij.dbn.common.dispose;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.latent.MapLatent;
import com.dci.intellij.dbn.common.list.FiltrableList;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.thread.Background;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.util.Unsafe;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.NamedComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ReflectionUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Timer;

public class DisposeUtil {
    private static final MapLatent<Class<?>, List<Field>, RuntimeException> CLASS_FIELDS = MapLatent.create(clazz -> ReflectionUtil.collectFields(clazz));

    private static final Logger LOGGER = LoggerFactory.createLogger();

    public static void disposeInBackground(Object ... disposable) {
        // trigger background in dispatch thread
        Dispatch.run(() -> Background.run(() -> dispose(disposable)));
    }

    public static void dispose(@Nullable Object ... objects) {
        if (objects != null && objects.length > 0) {
            for (Object disposable : objects) {
                dispose(disposable);
            }
        }
    }

    private static void dispose(@Nullable Object object) {
        if (object != null) {
            if (object instanceof Disposable) {
                Disposable disposable = (Disposable) object;
                dispose(disposable);
            }
            if (object instanceof Component) {
                Component component = (Component) object;
                dispose(component);
            }

        }

    }

    private static void dispose(@Nullable Disposable disposable) {
        if (disposable != null) {
            try {
                disposable.dispose();
            } catch (ProcessCanceledException ignore) {}
        }
    }

    public static void disposeInBackground(Collection<?> collection) {
        Background.run(() -> dispose(collection));
    }
    
    public static void dispose(Collection<?> collection) {
        if (collection instanceof FiltrableList) {
            FiltrableList<?> filtrableList = (FiltrableList<?>) collection;
            collection = filtrableList.getFullList();
        }
        if (collection != null && collection.size()> 0) {
            Collection<?> disposableCollection = new ArrayList<>(collection);
            clearCollection(collection);
            for (Object object : disposableCollection) {
                dispose(object);
            }
        }
    }

    private static void clearCollection(Collection<?> collection) {
        Unsafe.silent(() -> collection.clear());
    }

    private static void clearMap(Map<?, ?> map) {
        Unsafe.silent(() -> map.clear());
    }

    public static void dispose(Map<?, ?> map) {
        if (map != null) {
            Collection<?> disposableCollection = new ArrayList<>(map.values());
            clearMap(map);
            for (Object disposable : disposableCollection) {
                dispose(disposable);
            }
        }
    }

    public static void nullify(Object object) {
        nullify(object, null);
    }

    public static void nullify(Object object, @Nullable Runnable callback) {
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

    private static void dispose(@Nullable Component component) {
        if (component != null) {
            Dispatch.runConditional(() -> {
                GUIUtil.removeListeners(component);
                UIUtil.dispose(component);
                if (component instanceof Container) {
                    Container container = (Container) component;
                    dispose((Object[]) container.getComponents());
                    container.removeAll();
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
}