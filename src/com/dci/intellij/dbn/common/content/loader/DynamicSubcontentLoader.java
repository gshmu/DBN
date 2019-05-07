package com.dci.intellij.dbn.common.content.loader;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentElement;
import com.dci.intellij.dbn.common.content.DynamicContentStatus;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.content.dependency.SubcontentDependencyAdapter;
import com.dci.intellij.dbn.common.thread.ThreadMonitor;
import com.dci.intellij.dbn.common.thread.ThreadProperty;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This loader is to be used from building the elements of a dynamic content, based on a source content.
 * e.g. Constraints of a table are loaded from the complete actions of constraints of a Schema.
 */
public abstract class DynamicSubcontentLoader<
                T extends DynamicContentElement,
                M extends DBObjectMetadata>
        extends DynamicContentLoaderImpl<T, M>
        implements DynamicContentLoader<T, M> {

    private DynamicContentLoader<T, M> alternativeLoader = createAlternativeLoader();
    private boolean optimized;

    protected DynamicSubcontentLoader(@Nullable DynamicContentType parentContentType, @NotNull DynamicContentType contentType, boolean optimized) {
        super(parentContentType, contentType, true);
        this.optimized = optimized;
    }

    /**
     * Check if the source element matches the criteria of the dynamic content.
     * If it matches, it will be added as
     */
    public abstract boolean match(T sourceElement, DynamicContent dynamicContent);

    @Override
    public void loadContent(DynamicContent<T> dynamicContent, boolean force) throws SQLException {
        SubcontentDependencyAdapter dependencyAdapter = (SubcontentDependencyAdapter) dynamicContent.getDependencyAdapter();

        DynamicContent sourceContent = dependencyAdapter.getSourceContent();
        DynamicContentLoader<T, M> alternativeLoader = getAlternativeLoader();

        if (alternativeLoader != null && useAlternativeLoader(dependencyAdapter)) {
            sourceContent.loadInBackground();
            alternativeLoader.loadContent(dynamicContent, false);

        } else {
            //load from sub-content
            boolean matchedOnce = false;
            List<T> list = null;
            for (Object object : sourceContent.getAllElements()) {
                dynamicContent.checkDisposed();

                T element = (T) object;
                if (match(element, dynamicContent)) {
                    matchedOnce = true;
                    if (list == null) {
                        list = dynamicContent.is(DynamicContentStatus.CONCURRENT) ?
                                CollectionUtil.createConcurrentList() :
                                new ArrayList<T>();
                    }
                    list.add(element);
                }
                else if (matchedOnce && optimized) {
                    // the optimization check assumes that source content is sorted
                    // such as all matching elements are building a consecutive segment in the source content.
                    // If at least one match occurred and current element does not match any more,
                    // => there are no matching elements left in the source content, hence break the loop
                    break;
                }
            }
            dynamicContent.setElements(list);
            dynamicContent.set(DynamicContentStatus.MASTER, false);
        }
    }

    private boolean useAlternativeLoader(SubcontentDependencyAdapter dependencyAdapter) {
        if (dependencyAdapter.isSourceContentReady()) {
            return false;
        } else {
            //ThreadInfo thread = ThreadMonitor.current();
            if (/*thread.is(ThreadProperty.CODE_ANNOTATING) || */ThreadMonitor.getProcessCount(ThreadProperty.PROGRESS) > 10) {
                return false;
            } else {
                return true;
            }
        }
    }

    public final DynamicContentLoader<T, M> getAlternativeLoader() {
        return alternativeLoader;
    }

    @org.jetbrains.annotations.Nullable
    protected DynamicContentLoader<T, M> createAlternativeLoader() {
        return null;
    }
}
