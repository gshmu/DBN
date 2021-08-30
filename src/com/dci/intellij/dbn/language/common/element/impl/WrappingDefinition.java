package com.dci.intellij.dbn.language.common.element.impl;

import lombok.Getter;

@Getter
public class WrappingDefinition {
    private final TokenElementType beginElementType;
    private final TokenElementType endElementType;

    public WrappingDefinition(TokenElementType beginElementType, TokenElementType endElementType) {
        this.beginElementType = beginElementType;
        this.endElementType = endElementType;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if (obj instanceof WrappingDefinition) {
            WrappingDefinition definition = (WrappingDefinition) obj;
            return
                this.beginElementType.tokenType.equals(definition.beginElementType.tokenType) &&
                this.endElementType.tokenType.equals(definition.endElementType.tokenType);
        }
        return false;
    }
}
