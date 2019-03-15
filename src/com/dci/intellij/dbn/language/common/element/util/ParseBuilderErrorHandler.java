package com.dci.intellij.dbn.language.common.element.util;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.parser.ParserContext;
import gnu.trove.THashSet;

import java.util.Arrays;
import java.util.Set;

public class ParseBuilderErrorHandler {
    public static void updateBuilderError(Set<TokenType> expectedTokens, ParserContext context) {
        if (expectedTokens != null) {
            int offset = context.builder.getCurrentOffset();
            if (ParseBuilderErrorWatcher.show(offset, context.timestamp)) {
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

                String [] tokenDesc = tokenDescriptions.toArray(new String[0]);
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
                context.builder.error(buffer.toString());
            }
        }
    }
}
