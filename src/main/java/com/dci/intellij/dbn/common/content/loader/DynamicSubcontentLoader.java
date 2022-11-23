package com.dci.intellij.dbn.common.content.loader;

import com.dci.intellij.dbn.common.content.*;
import com.dci.intellij.dbn.common.content.dependency.ContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.SubcontentDependencyAdapter;
import com.dci.intellij.dbn.connection.DatabaseEntity;
import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * This loader is to be used from building the elements of a dynamic content, based on a source content.
 * e.g. Constraints of a table are loaded from the complete actions of constraints of a Schema.
 */
@Getter
public class DynamicSubcontentLoader<T extends DynamicContentElement, M extends DBObjectMetadata>
        extends DynamicContentLoaderImpl<T, M>
        implements DynamicContentLoader<T, M> {

    private DynamicContentLoader<T, M> alternativeLoader;

    private DynamicSubcontentLoader(@NotNull DynamicContentType parentContentType, @NotNull DynamicContentType contentType) {
        super(parentContentType, contentType, true);
    }

    public static <T extends DynamicContentElement, M extends DBObjectMetadata> DynamicSubcontentLoader<T, M> create(
            @NotNull DynamicContentType parentContentType,
            @NotNull DynamicContentType contentType,
            @Nullable DynamicContentLoader<T, M> alternativeLoader) {

        DynamicSubcontentLoader<T, M> loader = new DynamicSubcontentLoader<>(parentContentType, contentType);
        loader.alternativeLoader = alternativeLoader;
        return loader;
    }

    @Override
    public void loadContent(DynamicContent<T> content) throws SQLException {
        ContentDependencyAdapter dependency = content.getDependencyAdapter();
        if (dependency instanceof SubcontentDependencyAdapter) {
            SubcontentDependencyAdapter subcontentDependency = (SubcontentDependencyAdapter) dependency;

            DynamicContent<T> sourceContent = subcontentDependency.getSourceContent();
            DynamicContentLoader<T, M> alternativeLoader = getAlternativeLoader();

            boolean useAlternativeLoader = alternativeLoader != null && !sourceContent.isLoaded() && subcontentDependency.canUseAlternativeLoader();

            if (useAlternativeLoader) {
                sourceContent.loadInBackground();
                alternativeLoader.loadContent(content);

            } else if (sourceContent instanceof GroupedDynamicContent) {
                GroupedDynamicContent<T> groupedContent = (GroupedDynamicContent<T>) sourceContent;
                DatabaseEntity parent = content.ensureParentEntity();
                List<T> list = groupedContent.getChildElements(parent);
                content.setElements(list);
                content.set(DynamicContentProperty.MASTER, false);
            } else {
                content.setElements(Collections.emptyList());
                content.set(DynamicContentProperty.MASTER, false);
            }
        }

    }
}
