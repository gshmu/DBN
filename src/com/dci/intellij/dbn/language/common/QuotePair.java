package com.dci.intellij.dbn.language.common;

public class QuotePair {
    public static final QuotePair DEFAULT_IDENTIFIER_QUOTE_PAIR = new QuotePair('"', '"');
    private char beginChar;
    private char endChar;

    public QuotePair(char beginChar, char endChar) {
        this.beginChar = beginChar;
        this.endChar = endChar;
    }

    public char beginChar() {
        return beginChar;
    }

    public char endChar() {
        return endChar;
    }

    public boolean isQuoted(CharSequence charSequence) {
        char firstChar = charSequence.charAt(0);
        char lastChar = charSequence.charAt(charSequence.length() - 1);

        return firstChar == beginChar && lastChar == endChar;
    }

    public String replaceQuotes(String string, char character) {
        return string.replace(beginChar, character).replace(endChar, character);
    }
}
