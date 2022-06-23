package com.dci.intellij.dbn.common.content.loader;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentElement;
import com.dci.intellij.dbn.common.content.DynamicContentProperty;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.content.GroupedDynamicContent;
import com.dci.intellij.dbn.common.content.dependency.ContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.SubcontentDependencyAdapter;
import com.dci.intellij.dbn.common.thread.ThreadMonitor;
import com.dci.intellij.dbn.common.thread.ThreadProperty;
import com.dci.intellij.dbn.connection.DatabaseEntity;
import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Supplier;

/**
 * This loader is to be used from building the elements of a dynamic content, based on a source content.
 * e.g. Constraints of a table are loaded from the complete actions of constraints of a Schema.
 */
public abstract class DynamicSubcontentLoader<T extends DynamicContentElement, M extends DBObjectMetadata>
        extends DynamicContentLoaderImpl<T, M>
        implements DynamicContentLoader<T, M> {

    private final DynamicContentLoader<T, M> alternativeLoader = createAlternativeLoader();

    private DynamicSubcontentLoader(@NotNull DynamicContentType parentContentType, @NotNull DynamicContentType contentType) {
        super(parentContentType, contentType, true);
    }

    public static <T extends DynamicContentElement, M extends DBObjectMetadata> DynamicSubcontentLoader<T, M> create(
            @NotNull DynamicContentType parentContentType,
            @NotNull DynamicContentType contentType,
            @NotNull Supplier<DynamicContentLoader<T, M>> loader) {
        return new DynamicSubcontentLoader<T, M>(parentContentType, contentType) {
            @Nullable
            @Override
            protected DynamicContentLoader<T, M> createAlternativeLoader() {
                return loader.get();
            }
        };
    }

    @Override
    public void loadContent(DynamicContent<T> content, boolean force) throws SQLException {
        ContentDependencyAdapter dependency = content.getDependencyAdapter();
        if (dependency instanceof SubcontentDependencyAdapter) {
            SubcontentDependencyAdapter subcontentDependency = (SubcontentDependencyAdapter) dependency;

            DynamicContent<T> sourceContent = subcontentDependency.getSourceContent();
            DynamicContentLoader<T, M> alternativeLoader = getAlternativeLoader();

            if (alternativeLoader != null && useAlternativeLoader(subcontentDependency)) {
                sourceContent.loadInBackground();
                alternativeLoader.loadContent(content, false);

            } else if (sourceContent instanceof GroupedDynamicContent) {
                GroupedDynamicContent groupedContent = (GroupedDynamicContent) sourceContent;
                DatabaseEntity parent = content.ensureParentEntity();
                List<T> list = groupedContent.getChildElements(parent);
                content.setElements(list);
                content.set(DynamicContentProperty.MASTER, false);
            } else {
                throw new UnsupportedOperationException();
            }
        }

    }

    private boolean useAlternativeLoader(SubcontentDependencyAdapter dependencyAdapter) {
        if (dependencyAdapter.isSourceContentReady()) {
            return false;
        } else {
            //ThreadInfo thread = ThreadMonitor.current();
            if (/*thread.is(ThreadProperty.CODE_ANNOTATING) || */ThreadMonitor.getProcessCount(ThreadProperty.PROGRESS) > 20) {
                return false;
            } else {
                return true;
            }
        }
    }

    public final DynamicContentLoader<T, M> getAlternativeLoader() {
        return alternativeLoader;
    }

    @Nullable
    protected abstract DynamicContentLoader<T, M> createAlternativeLoader();
}
