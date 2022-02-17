package com.dci.intellij.dbn.common.dispose;

import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.util.Unsafe;
import com.dci.intellij.dbn.connection.DatabaseEntity;
import com.intellij.openapi.components.NamedComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.util.ReflectionUtil;
import lombok.extern.slf4j.Slf4j;

import java.awt.Component;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public final class Nullifier {
    private Nullifier() {}

    private static final Map<Class<?>, List<Field>> NULLIFIABLE_FIELDS = new ConcurrentHashMap<>();
    private static final Class[] NULLIFIABLE_CLASSES = new Class[] {
            Map.class,
            Collection.class,
            Latent.class,
            Editor.class,
            Document.class,
            PsiElement.class,
            VirtualFile.class,
            Configuration.class,
            DatabaseEntity.class,
            AutoCloseable.class,
            NamedComponent.class,
            EventListener.class,
    };

    public static void clearCollection(Collection<?> collection) {
        Unsafe.silent(() -> collection.clear());
    }

    public static void clearMap(Map<?, ?> map) {
        Unsafe.silent(() -> map.clear());
    }

    public static void nullify(Object object) {
        if (!(object instanceof Component)) {
            BackgroundDisposer.queue(() -> {
                List<Field> fields = nullifiableFields(object.getClass());
                for (Field field : fields) {
                    try {
                        nullifyField(object, field);
                    } catch (UnsupportedOperationException ignore) {
                    } catch (Throwable e) {
                        log.error("Failed to nullify field", e);
                    }
                }
            });
        } else {
            System.out.println();
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
                field.set(object, null);
            }
        }
    }

    private static List<Field> nullifiableFields(Class clazz) {
        return NULLIFIABLE_FIELDS.computeIfAbsent(clazz, c ->
                ReflectionUtil.
                        collectFields(c).
                        stream().
                        filter(field -> isNullifiable(field)).
                        collect(Collectors.toList()));
    }

    private static boolean isNullifiable(Field field) {
        int modifiers = field.getModifiers();
        if (Modifier.isStatic(modifiers) ||
                //Modifier.isFinal(modifiers) ||
                Modifier.isNative(modifiers)) {
            return false;
        }

        Sticky sticky = field.getAnnotation(Sticky.class);
        if (sticky != null) {
            return false;
        }

        for (Class<?> nullifiableClass : NULLIFIABLE_CLASSES) {
            if (nullifiableClass.isAssignableFrom(field.getType())) {
                return true;
            }
        }
        return false;
    }
}
