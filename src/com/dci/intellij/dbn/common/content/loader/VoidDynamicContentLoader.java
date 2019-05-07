package com.dci.intellij.dbn.common.content.loader;

import com.dci.intellij.dbn.common.content.DynamicContent;

import java.sql.SQLException;

public class VoidDynamicContentLoader implements DynamicContentLoader{

    public static final VoidDynamicContentLoader INSTANCE = new VoidDynamicContentLoader();

    private VoidDynamicContentLoader() {

    }

    @Override
    public void loadContent(DynamicContent dynamicContent, boolean forceReload) throws SQLException {
        // do nothing
    }
}
