package com.dci.intellij.dbn.common.dispose;

import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.util.Unsafe;
import com.intellij.openapi.components.NamedComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ReflectionUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class Nullifier {
    private Nullifier() {}

    private static final Map<Class<?>, List<Field>> CLASS_FIELDS = new ConcurrentHashMap<>();

    public static void clearCollection(Collection<?> collection) {
        Unsafe.silent(() -> collection.clear());
    }

    public static void clearMap(Map<?, ?> map) {
        Unsafe.silent(() -> map.clear());
    }

    public static void nullify(Object object) {
        nullify(object, true);
    }

    private static void nullify(Object object, boolean background) {
        if (background) {
            BackgroundDisposer.queue(() -> nullify(object, false));
        } else {
            List<Field> fields = CLASS_FIELDS.computeIfAbsent(object.getClass(), clazz -> ReflectionUtil.collectFields(clazz));
            for (Field field : fields) {
                try {
                    Sticky sticky = field.getAnnotation(Sticky.class);
                    if (sticky == null) {
                        nullifyField(object, field);
                    }
                } catch (UnsupportedOperationException ignore) {
                } catch (Throwable e) {
                    log.error("Failed to nullify field", e);
                }
            }
        }
    }

    private static void nullifyField(Object object, Field field) throws IllegalAccessException {
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
        }    }
}
