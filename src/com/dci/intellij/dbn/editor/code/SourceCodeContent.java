package com.dci.intellij.dbn.editor.code;

import static com.dci.intellij.dbn.editor.code.GuardedBlockMarker.END_OFFSET_IDENTIFIER;
import static com.dci.intellij.dbn.editor.code.GuardedBlockMarker.START_OFFSET_IDENTIFIER;

public class SourceCodeContent extends GuardedBlockUtil{
    private String sourceCode;
    private SourceCodeOffsets offsets = new SourceCodeOffsets();

    public SourceCodeContent(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public SourceCodeOffsets getOffsets() {
        return offsets;
    }

    public void importContent(String content) {
        StringBuilder builder = new StringBuilder(content);
        int startIndex = builder.indexOf(START_OFFSET_IDENTIFIER);


        while (startIndex > -1) {
            builder.replace(startIndex, startIndex + START_OFFSET_IDENTIFIER.length(), "");
            int endIndex = builder.indexOf(END_OFFSET_IDENTIFIER);
            if (endIndex == -1) {
                throw new IllegalArgumentException("Unbalanced guarded block markers");
            }
            builder.replace(endIndex, endIndex + END_OFFSET_IDENTIFIER.length(), "");
            offsets.addGuardedBlock(startIndex, endIndex);

            startIndex = builder.indexOf(START_OFFSET_IDENTIFIER);
        }

        sourceCode = builder.toString();
    }
}
