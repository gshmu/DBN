package com.dci.intellij.dbn.language.common;

public class QuoteDefinition {
    public static final QuoteDefinition DEFAULT_IDENTIFIER_QUOTE_DEFINITION = new QuoteDefinition(QuotePair.DEFAULT_IDENTIFIER_QUOTE_PAIR);
    private QuotePair[] quotePairs;

    public QuoteDefinition(QuotePair... quotePairs) {
        this.quotePairs = quotePairs;
    }

    public QuotePair getDefaultQuotes() {
        return quotePairs[0];
    }

    public QuotePair getQuote(char character) {
        for (QuotePair quotePair : quotePairs) {
            if (character == quotePair.beginChar() || character == quotePair.endChar()) {
                return quotePair;
            }
        }
        return null;
    }

    public boolean isQuoted(CharSequence charSequence) {
        for (QuotePair quotePair : quotePairs) {
            if (quotePair.isQuoted(charSequence)) {
                return true;
            }
        }
        return false;
    }

    public boolean isQuoteBegin(char character) {
        for (QuotePair quotePair : quotePairs) {
            if (quotePair.beginChar() == character) {
                return true;
            }
        }
        return false;
    }

    public boolean isQuoteEnd(char beginQuote, char character) {
        for (QuotePair quotePair : quotePairs) {
            if (quotePair.beginChar() == beginQuote && quotePair.endChar() == character) {
                return true;
            }
        }
        return false;
    }
}
