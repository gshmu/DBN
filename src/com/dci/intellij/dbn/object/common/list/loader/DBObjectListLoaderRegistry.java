package com.dci.intellij.dbn.object.common.list.loader;

import com.dci.intellij.dbn.common.content.DynamicContentElement;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoaderImpl;
import com.dci.intellij.dbn.connection.DatabaseEntity;
import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBVirtualObject;
import com.dci.intellij.dbn.object.type.DBObjectType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Deprecated
public class DBObjectListLoaderRegistry {
    private static final Map<DynamicContentType, DynamicContentLoader> ROOT_LOADERS = new HashMap<>();
    private static final Map<DynamicContentType, Map<DynamicContentType, DynamicContentLoader>> CHILD_LOADERS = new HashMap<>();

    public static void register(@NotNull DatabaseEntity parent, @NotNull DynamicContentType contentType, @NotNull DynamicContentLoader loader) {
        if (parent instanceof DBObject) {
            DBObject parentObject = (DBObject) parent;
            DBObjectType parentObjectType = parentObject.getObjectType();
            Map<DynamicContentType, DynamicContentLoader> childLoaders = CHILD_LOADERS.computeIfAbsent(parentObjectType, k -> new HashMap<>());
            DynamicContentLoader contentLoader = childLoaders.get(contentType);
            if (contentLoader == null) {
                childLoaders.put(contentType, loader);
            } else if (contentLoader != loader){
                System.out.println("Duplicate loader");
            }
        } else {
            DynamicContentLoader contentLoader = ROOT_LOADERS.get(contentType);
            if (contentLoader == null) {
                ROOT_LOADERS.put(contentType, loader);
            } else if (contentLoader != loader){
                System.out.println("Duplicate loader");
            }
        }
    }

    @NotNull
    public static <T extends DynamicContentElement, M extends DBObjectMetadata> DynamicContentLoader<T, M> get(
            @NotNull DatabaseEntity parent,
            @NotNull DynamicContentType contentType) {

        if (parent instanceof DBVirtualObject) {
            return DynamicContentLoader.VOID_CONTENT_LOADER;
        }
        else {
            DynamicContentLoader<T, M> loader = find(parent, contentType);
            DynamicContentType parentContentType = null;
            if (parent instanceof DBObject) {
                parentContentType = parent.getDynamicContentType();
            }

            DynamicContentLoader<T, M> registeredLoader = DynamicContentLoaderImpl.resolve(parentContentType, contentType);
            if (loader != registeredLoader) {
                System.out.println();
            }
            return loader;
        }
    }

    public static <T extends DynamicContentElement, M extends DBObjectMetadata> DynamicContentLoader<T, M> find(
            @NotNull DatabaseEntity parent,
            @NotNull DynamicContentType contentType) {

        if (parent instanceof DBObject) {
            DBObject parentObject = (DBObject) parent;
            DBObjectType parentObjectType = parentObject.getObjectType();
            Map<DynamicContentType, DynamicContentLoader> childLoaders = CHILD_LOADERS.get(parentObjectType);
            return childLoaders.get(contentType);
        } else {
            return ROOT_LOADERS.get(contentType);
        }
    }
}
