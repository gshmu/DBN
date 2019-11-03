package com.dci.intellij.dbn.common.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class StringUtil extends com.intellij.openapi.util.text.StringUtil {
    @NotNull
    public static List<String> tokenize(@NotNull String string, @NotNull String separator) {
        List<String> tokens = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer(string, separator);
        while (tokenizer.hasMoreTokens()) {
            tokens.add(tokenizer.nextToken().trim());
        }
        return tokens;
    }

    public static String concatenate(List<String> tokens, String separator) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            buffer.append(token);
            if (i < tokens.size() - 1) {
                buffer.append(separator);
            }
        }
        return buffer.toString();
    }

    public static boolean containsOneOf(String string, String... tokens) {
        for (String token : tokens) {
            if (string.contains(token)) return true;
        }
        return false;
    }

    public static boolean isMixedCase(String string) {
        boolean upperCaseFound = false;
        boolean lowerCaseFound = false;
        for (int i=0; i<string.length(); i++) {
            char chr = string.charAt(i);

            if (!upperCaseFound && Character.isUpperCase(chr)) {
                upperCaseFound = true;
            } else if (!lowerCaseFound && Character.isLowerCase(chr)) {
                lowerCaseFound = true;
            }

            if (upperCaseFound && lowerCaseFound) return true;
        }

        return false;
    }


    public static boolean isNotEmptyOrSpaces(String string) {
        return !isEmptyOrSpaces(string);
    }

    public static boolean isOneOf(String string, String ... values) {
        for (String value : values) {
            if (value.equals(string)) return true;
        }
        return false;
    }

    public static boolean isOneOfIgnoreCase(String string, String ... values) {
        for (String value : values) {
            if (equalsIgnoreCase(value, string)) return true;
        }
        return false;
    }



    public static boolean isInteger(@Nullable String string) {
        try {
            if (isNotEmptyOrSpaces(string)) {
                Integer.parseInt(string);
                return true;
            }
        } catch (NumberFormatException ignore) {}

        return false;

    }

    public static boolean isNumber(@Nullable String string) {
        try {
            if (isNotEmptyOrSpaces(string)) {
                Double.parseDouble(string);
                return true;
            }
        } catch (NumberFormatException ignore) {}
        return false;
    }

    public static boolean isWord(String name) {
        boolean containsLetters = false;
        for (char c : name.toCharArray()) {
            boolean isLetter = Character.isLetter(c) || c == '_';
            containsLetters = containsLetters || isLetter;
            if (!isLetter && !Character.isDigit(c)) {
                return false;
            }
        }
        return containsLetters;
    }

    public static String removeCharacter(String content, char c) {
        int index = content.indexOf(c);
        if (index > -1) {
            int beginIndex = 0;
            int endIndex = index;
            StringBuilder buffer = new StringBuilder();
            while (endIndex > -1) {
                if (beginIndex < endIndex) buffer.append(content, beginIndex, endIndex);
                beginIndex = endIndex + 1;
                endIndex = content.indexOf(c, beginIndex);
            }
            if (beginIndex < content.length() - 1) {
                buffer.append(content.substring(beginIndex));
            }
            return buffer.toString();
        }
        return content;
    }

    public static @NotNull String trim(@Nullable String message) {
        return isEmptyOrSpaces(message) ? "" : message.trim();
    }

    public static String textWrap(String string, int maxRowLength, String wrapCharacters) {
        StringBuilder builder = new StringBuilder();
        if (string != null) {
            StringTokenizer tokenizer = new StringTokenizer(string, wrapCharacters, true);
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                int wrapIndex = builder.lastIndexOf("\n") + 1;
                if (wrapCharacters.contains(token)) {
                    builder.append(token);
                } else {
                    int tokenLength = token.length();
                    if (tokenLength >= maxRowLength) {
                        if (wrapIndex != builder.length()) {
                            builder.append("\n");
                        }
                        builder.append(token.trim());
                    } else {
                        if (builder.length() - wrapIndex + tokenLength > maxRowLength) {
                            builder.append("\n");
                        }
                        builder.append(token);
                    }
                }
            }
        }
        return builder.toString().trim();
    }

    public static int textMaxRowLength(String string) {
        int offset = 0;
        int maxLength = 0;
        while (true) {
            int index = string.indexOf('\n', offset);
            if (index == -1) {
                maxLength = Math.max(maxLength, string.length() - offset);
                break;
            } else {
                int length = index - offset;
                maxLength = Math.max(maxLength, length);
                offset = index + 1;
            }

        }
        return maxLength;
    }

    public static int indexOfIgnoreCase(@NotNull CharSequence where, @NotNull CharSequence what, int fromIndex) {
        int targetCount = what.length();
        int sourceCount = where.length();

        if (fromIndex >= sourceCount) {
            return targetCount == 0 ? sourceCount : -1;
        }

        if (fromIndex < 0) {
            fromIndex = 0;
        }

        if (targetCount == 0) {
            return fromIndex;
        }

        char first = what.charAt(0);
        int max = sourceCount - targetCount;

        for (int i = fromIndex; i <= max; i++) {
            /* Look for first character. */
            if (!charsEqualIgnoreCase(where.charAt(i), first)) {
                //noinspection StatementWithEmptyBody,AssignmentToForLoopParameter
                while (++i <= max && !charsEqualIgnoreCase(where.charAt(i), first)) ;
            }

            /* Found first character, now look at the rest of v2 */
            if (i <= max) {
                int j = i + 1;
                int end = j + targetCount - 1;
                //noinspection StatementWithEmptyBody
                for (int k = 1; j < end && charsEqualIgnoreCase(where.charAt(j), what.charAt(k)); j++, k++) ;

                if (j == end) {
                    /* Found whole string. */
                    return i;
                }
            }
        }

        return -1;
    }

    public static CharSequence toUpperCase(CharSequence s) {
        StringBuilder answer = null;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            char upcased = toUpperCase(c);
            if (answer == null && upcased != c) {
                answer = new StringBuilder(s.length());
                answer.append(s.subSequence(0, i));
            }

            if (answer != null) {
                answer.append(upcased);
            }
        }

        return answer == null ? s : answer.toString();
    }

    public static String intern(String value) {
        return value == null ? null : value.intern();
    }
}

