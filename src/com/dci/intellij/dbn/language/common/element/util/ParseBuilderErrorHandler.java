package com.dci.intellij.dbn.language.common.element.util;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.TokenTypeCategory;
import com.dci.intellij.dbn.language.common.element.parser.ParserBuilder;
import com.dci.intellij.dbn.language.common.element.parser.ParserContext;

import java.util.Set;
import java.util.stream.Collectors;

public class ParseBuilderErrorHandler {
    public static void updateBuilderError(Set<TokenType> expectedTokens, ParserContext context) {
        if (expectedTokens != null) {
            int offset = context.builder.getCurrentOffset();
            if (ParseBuilderErrorWatcher.show(offset, context.timestamp)) {
                context.builder.error("Invalid or incomplete statement");

                expectedTokenError(1, context.builder, TokenTypeCategory.CHARACTER, expectedTokens);
                expectedTokenError(2, context.builder, TokenTypeCategory.OPERATOR, expectedTokens);
                expectedTokenError(3, context.builder, TokenTypeCategory.KEYWORD, expectedTokens);
                expectedTokenError(4, context.builder, TokenTypeCategory.FUNCTION, expectedTokens);
                expectedTokenError(5, context.builder, TokenTypeCategory.DATATYPE, expectedTokens);
                expectedTokenError(6, context.builder, TokenTypeCategory.IDENTIFIER, expectedTokens);

/*
                Set<String> tokenDescriptions = new THashSet<>(expectedTokens.size());
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

                String[] tokenDesc = tokenDescriptions.toArray(new String[0]);
                Arrays.sort(tokenDesc);

                StringBuilder buffer = new StringBuilder("expected");
                buffer.append(tokenDesc.length > 1 ? " one of the following: \n" : ": ");

                for (int i=0; i<tokenDesc.length; i++) {
                    buffer.append(tokenDesc[i]);
                    if (i < tokenDesc.length - 1) {
                        buffer.append("\n");
                    }
                }
                //buffer.append("\n");
                context.builder.markError("Invalid or incomplete statement");

                context.builder.error(buffer.toString());
*/
            }
        }
    }

    private static void expectedTokenError(int index, ParserBuilder builder, TokenTypeCategory category, Set<TokenType> expectedTokens) {
        boolean charOperator = category == TokenTypeCategory.CHARACTER || category == TokenTypeCategory.OPERATOR;
        String delimiter = charOperator ? " " : ", ";
        Set<TokenType> tokenTypes = expectedTokens
                .stream()
                .filter(tokenType -> tokenType.getCategory() == category)
                .collect(Collectors.toSet());

        if (!tokenTypes.isEmpty()) {
            String message;
            switch (category) {
                case IDENTIFIER: {
                    message = "identifier";
                    break;
                }
                case CHARACTER:
                case OPERATOR: {
                    message = category.getName() + ": " + tokenTypes
                            .stream()
                            .map(tokenType -> tokenType.getValue())
                            .distinct()
                            .sorted()
                            .collect(Collectors.joining(" "));
                    break;
                }
                default: {
                    message = category.getName() + ": " +
                            tokenTypes
                                    .stream()
                                    .map(tokenType -> tokenType.getValue().toUpperCase())
                                    .distinct()
                                    .limit(20)
                                    .sorted()
                                    .collect(Collectors.joining(" "));
                    if (tokenTypes.size() > 20) {
                        message = message + "...";
                    }
                }
            }
            builder.markError(message);
        }

    }
}
