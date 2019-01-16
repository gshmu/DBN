package com.dci.intellij.dbn.common.content.loader;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentElement;
import com.dci.intellij.dbn.common.content.DynamicContentStatus;
import com.dci.intellij.dbn.common.content.dependency.SubcontentDependencyAdapter;
import com.dci.intellij.dbn.common.util.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

public abstract class DynamicSubcontentCustomLoader<T extends DynamicContentElement> implements DynamicContentLoader<T> {
    public abstract T resolveElement(DynamicContent<T> dynamicContent, DynamicContentElement sourceElement);

    public void loadContent(DynamicContent<T> dynamicContent, boolean forceReload) throws DynamicContentLoadException, InterruptedException {
        List<T> list = null;
        SubcontentDependencyAdapter dependencyAdapter = (SubcontentDependencyAdapter) dynamicContent.getDependencyAdapter();
        for (Object object : dependencyAdapter.getSourceContent().getAllElements()) {
            dynamicContent.checkDisposed();
            DynamicContentElement sourceElement = (DynamicContentElement) object;
            T element = resolveElement(dynamicContent, sourceElement);
            if (element != null) {
                dynamicContent.checkDisposed();
                if (list == null) {
                    list = dynamicContent.is(DynamicContentStatus.CONCURRENT) ?
                            CollectionUtil.createConcurrentList() :
                            new ArrayList<T>();
                }
                list.add(element);
            }
        }
        dynamicContent.setElements(list);
    }

    public void reloadContent(DynamicContent<T> dynamicContent) throws DynamicContentLoadException, InterruptedException {
        SubcontentDependencyAdapter dependencyAdapter = (SubcontentDependencyAdapter) dynamicContent.getDependencyAdapter();
        dependencyAdapter.getSourceContent().reload();
        loadContent(dynamicContent, true);
    }
}
