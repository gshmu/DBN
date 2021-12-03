package com.dci.intellij.dbn.object.filter.generic;

import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.object.DBSchema;

public class NonEmptySchemaFilter implements Filter<DBSchema> {
    public static final Filter<DBSchema> INSTANCE = new NonEmptySchemaFilter();

    @Override
    public boolean accepts(DBSchema schema) {
        return !schema.isEmptySchema();
    }
}
