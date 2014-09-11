package com.dci.intellij.dbn.language.common;

public enum TokenTypeCategory {
    UNKNOWN("unknown"),
    KEYWORD("keyword"),
    FUNCTION("function"),
    PARAMETER("parameter"),
    DATATYPE("datatype"),
    EXCEPTION("exception"),
    OPERATOR("operator"),
    CHARACTER("character"),
    IDENTIFIER("identifier"),
    CHAMELEON("chameleon"),
    WHITESPACE("whitespace"),
    COMMENT("comment")
    ;

    private String name;
    TokenTypeCategory(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static TokenTypeCategory getCategory(String categoryName) {
        for (TokenTypeCategory identifier : TokenTypeCategory.values()) {
            if (identifier.getName().equals(categoryName)) return identifier;
        }
        return UNKNOWN;
    }
}
