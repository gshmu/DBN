package com.dci.intellij.dbn.common.dispose;

import com.dci.intellij.dbn.common.Reference;
import com.dci.intellij.dbn.common.list.FiltrableList;
import com.dci.intellij.dbn.common.thread.Background;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.ReflectionUtil;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
    
    public static <T extends Disposable> void dispose(Collection<T> collection) {
        if (collection instanceof FiltrableList) {
            FiltrableList<T> filtrableList = (FiltrableList) collection;
            collection = filtrableList.getFullList();
        }
        if (collection != null && collection.size()> 0) {
            Collection<T> disposableCollection = new ArrayList<>(collection);
            collection.clear();
            for(Disposable disposable : disposableCollection) {
                dispose(disposable);
            }
        }
    }

    public static <T extends Disposable> void dispose(Map<?, T> map) {
        if (map != null) {
            Collection<T> disposableCollection = new ArrayList<>(map.values());
            map.clear();
            for (Disposable disposable : disposableCollection) {
                dispose(disposable);
            }
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

    public static void nullify(Object object) {
        List<Field> fields = ReflectionUtil.collectFields(object.getClass());
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object fieldValue = field.get(object);
                if (fieldValue != null) {
                    if (fieldValue instanceof Collection<?>) {
                        Collection collection = (Collection) fieldValue;
                        collection.clear();
                    } else if (fieldValue instanceof Map) {
                        Map map = (Map) fieldValue;
                        map.clear();
                    } else {
                        int modifiers = field.getModifiers();
                        if (!Modifier.isFinal(modifiers) &&
                                !Modifier.isStatic(modifiers) &&
                                !Modifier.isNative(modifiers) &&
                                !Modifier.isTransient(modifiers) &&
                                !field.getType().isPrimitive() &&
                                !WeakReference.class.isAssignableFrom(field.getType()) &&
                                !Reference.class.isAssignableFrom(field.getType())) {
                            field.set(object, null);
                        }
                    }

                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

    }
}