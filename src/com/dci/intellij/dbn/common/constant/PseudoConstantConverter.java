package com.dci.intellij.dbn.common.constant;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.util.xmlb.Converter;

public class PseudoConstantConverter<T extends PseudoConstant> extends Converter<T> {
    private Class<T> typeClass;

    public PseudoConstantConverter(Class<T> typeClass) {
        this.typeClass = typeClass;
    }

    @Nullable
    @Override
    public T fromString(@NotNull String value) {
        return PseudoConstant.get(typeClass, value);
    }

    @NotNull
    @Override
    public String toString(@NotNull T constant) {
        return constant.id();
    }
}
