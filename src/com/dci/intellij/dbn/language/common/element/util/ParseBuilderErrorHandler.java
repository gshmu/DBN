package com.dci.intellij.dbn.language.common.element.util;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.parser.ParserBuilder;
import com.dci.intellij.dbn.language.common.element.parser.ParserContext;
import gnu.trove.THashSet;

import java.util.Arrays;
import java.util.Set;

public class ParseBuilderErrorHandler {
    private ElementType elementType;
    private String errDescription;

    public ParseBuilderErrorHandler(ElementType elementType) {
        this.elementType = elementType;
    }

    public String getErrDescription() {
        if (errDescription == null) {
            Set<TokenType> tokenTypes = elementType.getLookupCache().getFirstPossibleTokens();
            Set<String> tokenDescriptions = new THashSet<String>(tokenTypes.size());

            for (TokenType tokenType : tokenTypes) {
                String description = tokenType.getValue();
                        /*tokenType.getValue() != null && tokenType.getValue().trim().length() > 0 ?
                                tokenType.getValue() + ' ' + tokenType.getObjectType() :
                                tokenType.getObjectType();*/

                tokenDescriptions.add(description);
            }

            String [] tokenDesc = tokenDescriptions.toArray(new String[tokenDescriptions.size()]);
            Arrays.sort(tokenDesc);

            StringBuilder buffer = new StringBuilder("expected");
            buffer.append(tokenDesc.length > 1 ? " one of the following: " : ": ");

            for (int i=0; i<tokenDesc.length; i++) {
                buffer.append(tokenDesc[i]);
                if (i < tokenDesc.length - 1) {
                    buffer.append(" ");
                }
            }
            errDescription = buffer.toString();
        }
        return errDescription;
    }


    public static void updateBuilderError(Set<TokenType> expectedTokens, ParserContext context) {
        ParserBuilder builder = context.getBuilder();
        int offset = builder.getCurrentOffset();
        if (ParseBuilderErrorWatcher.show(offset, context.getTimestamp())) {

            Set<String> tokenDescriptions = new THashSet<String>(expectedTokens.size());
            for (TokenType tokenType : expectedTokens) {
                if (tokenType.isFunction()) {
                    tokenDescriptions.add("function");
                    continue;
                }
                String value = tokenType.getValue();
                String description =
                        tokenType.isIdentifier() ? "identifier" :
                        StringUtil.isNotEmptyOrSpaces(value) ? value.toUpperCase() : tokenType.getTypeName();

                tokenDescriptions.add(description);
            }

            String [] tokenDesc = tokenDescriptions.toArray(new String[tokenDescriptions.size()]);
            Arrays.sort(tokenDesc);

            StringBuilder buffer = new StringBuilder("expected");
            buffer.append(tokenDesc.length > 1 ? " one of the following: " : ": ");

            for (int i=0; i<tokenDesc.length; i++) {
                buffer.append(tokenDesc[i]);
                if (i < tokenDesc.length - 1) {
                    buffer.append(" ");
                }
            }
            //buffer.append("\n");
            builder.error(buffer.toString());
        }
    }
}
