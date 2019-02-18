package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.constant.PseudoConstant;
import com.dci.intellij.dbn.common.constant.PseudoConstantConverter;
import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.object.DBSchema;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public final class SchemaId extends PseudoConstant<SchemaId> implements Presentable {
    public SchemaId(String id) {
        super(id);
    }

    public static SchemaId get(String id) {
        return get(SchemaId.class, id);
    }

    @NotNull
    @Override
    public String getName() {
        return id();
    }

    public static SchemaId from(DBSchema schema) {
        return schema == null ? null : schema.getIdentifier();
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return Icons.DBO_SCHEMA;
    }

    public static class Converter extends PseudoConstantConverter<SchemaId> {
        public Converter() {
            super(SchemaId.class);
        }
    }

}
