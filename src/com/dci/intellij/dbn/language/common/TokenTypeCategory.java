package com.dci.intellij.dbn.language.common;

import lombok.Getter;

@Getter
public enum TokenTypeCategory {
    UNKNOWN("unknown"),
    KEYWORD("keyword"),
    FUNCTION("function"),
    PARAMETER("parameter"),
    DATATYPE("datatype"),
    OBJECT("object"),
    EXCEPTION("exception"),
    OPERATOR("operator"),
    CHARACTER("character"),
    IDENTIFIER("identifier"),
    CHAMELEON("chameleon"),
    WHITESPACE("whitespace"),
    COMMENT("comment"),
    NUMERIC("numeric"),
    LITERAL("literal")
    ;

    private final String name;

    TokenTypeCategory(String name) {
        this.name = name;
    }

    public static TokenTypeCategory getCategory(String categoryName) {
        for (TokenTypeCategory identifier : TokenTypeCategory.values()) {
            if (identifier.name.equals(categoryName)) return identifier;
        }
        return UNKNOWN;
    }
}
