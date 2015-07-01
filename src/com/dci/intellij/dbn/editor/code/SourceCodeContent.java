package com.dci.intellij.dbn.editor.code;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.dci.intellij.dbn.editor.code.GuardedBlockMarker.END_OFFSET_IDENTIFIER;
import static com.dci.intellij.dbn.editor.code.GuardedBlockMarker.START_OFFSET_IDENTIFIER;

public class SourceCodeContent{
    private CharSequence text = "";
    private SourceCodeOffsets offsets = new SourceCodeOffsets();

    public SourceCodeContent() {
    }

    public SourceCodeContent(CharSequence text) {
        this.text = text;
    }

    public CharSequence getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public byte[] getBytes(Charset charset) {
        return text.toString().getBytes(charset);
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

        text = builder.toString();
    }

    public String exportContent() {
        StringBuilder builder = new StringBuilder(text);
        List<GuardedBlockMarker> ranges = new ArrayList<GuardedBlockMarker>(offsets.getGuardedBlocks().getRanges());
        Collections.sort(ranges, Collections.reverseOrder());
        for (GuardedBlockMarker range : ranges) {
            builder.insert(range.getEndOffset(), END_OFFSET_IDENTIFIER);
            builder.insert(range.getStartOffset(), START_OFFSET_IDENTIFIER);
        }
        return builder.toString();
    }
}
