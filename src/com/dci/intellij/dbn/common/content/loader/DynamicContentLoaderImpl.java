package com.dci.intellij.dbn.common.content.loader;

import com.dci.intellij.dbn.common.content.DynamicContentElement;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.util.Commons;
import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class DynamicContentLoaderImpl<
                T extends DynamicContentElement,
                M extends DBObjectMetadata>
        implements DynamicContentLoader<T, M>{

    private static final Map<DynamicContentType, Map<DynamicContentType, DynamicContentLoader>> LOADERS = new ConcurrentHashMap<>();

    private static final DynamicContentType NULL = new DynamicContentType() {
        @Override
        public boolean matches(DynamicContentType contentType) {
            return contentType == this;
        }

        @Override
        public String toString() {
            return "NULL";
        }
    };

    public DynamicContentLoaderImpl(@Nullable DynamicContentType parentContentType, @NotNull DynamicContentType contentType, boolean register) {
        if (register) {
            register(parentContentType, contentType, this);
        }
    }

    private static void register(
            @Nullable DynamicContentType parentContentType,
            @NotNull DynamicContentType contentType,
            @NotNull DynamicContentLoader loader) {

        parentContentType = Commons.nvl(parentContentType, NULL);
        Map<DynamicContentType, DynamicContentLoader> childLoaders = LOADERS.computeIfAbsent(parentContentType, k -> new HashMap<>());
        DynamicContentLoader contentLoader = childLoaders.get(contentType);
        if (contentLoader == null) {
            childLoaders.put(contentType, loader);
        } else if (contentLoader != loader){
            log.error("Duplicate content loader registration for parentContentType={} and contentType={}", parentContentType, contentType);
        }
    }

    @NotNull
    public static <T extends DynamicContentElement, M extends DBObjectMetadata> DynamicContentLoader<T, M> resolve(
            @Nullable DynamicContentType<?> parentContentType,
            @NotNull DynamicContentType<?> contentType) {

        DynamicContentType<?> lookupParentContentType = Commons.nvl(parentContentType, NULL);
        while (lookupParentContentType != null) {
            Map<DynamicContentType, DynamicContentLoader> loaderMap = LOADERS.get(lookupParentContentType);
            if (loaderMap != null) {
                DynamicContentType<?> lookupContentType = contentType;
                while (lookupContentType != null) {
                    DynamicContentLoader loader = loaderMap.get(lookupContentType);
                    if (loader != null) {
                        return loader;
                    }
                    DynamicContentType<?> genericContentType = lookupContentType.getGenericType();
                    lookupContentType = genericContentType == lookupContentType ? null : genericContentType;
                }
            }
            DynamicContentType<?> genericParentContentType = lookupParentContentType.getGenericType();
            lookupParentContentType =
                    genericParentContentType == NULL ? null :
                    genericParentContentType == lookupParentContentType ? NULL :
                    genericParentContentType;
        }

        throw new UnsupportedOperationException("No entry found for content type "+ lookupParentContentType + " / " + contentType);
    }
}
