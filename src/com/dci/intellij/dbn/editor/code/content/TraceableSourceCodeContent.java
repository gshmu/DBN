package com.dci.intellij.dbn.editor.code.content;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.util.ChangeTimestamp;
import static com.dci.intellij.dbn.editor.code.content.GuardedBlockMarker.END_OFFSET_IDENTIFIER;
import static com.dci.intellij.dbn.editor.code.content.GuardedBlockMarker.START_OFFSET_IDENTIFIER;

public class TraceableSourceCodeContent extends BasicSourceCodeContent {
    private SourceCodeOffsets offsets = new SourceCodeOffsets();
    private ChangeTimestamp timestamp;

    public TraceableSourceCodeContent() {
        timestamp = new ChangeTimestamp();
    }

    public TraceableSourceCodeContent(CharSequence text, ChangeTimestamp timestamp) {
        this.text = text;
        this.timestamp = timestamp;
    }

    public ChangeTimestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ChangeTimestamp timestamp) {
        this.timestamp = timestamp;
    }

    @NotNull
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

        setText(builder.toString());
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

    public boolean isOlderThan(@Nullable ChangeTimestamp timestamp) {
        return timestamp != null && this.timestamp.isBefore(timestamp);
    }

    public boolean isOlderThan(@Nullable TraceableSourceCodeContent content) {
        return content != null && isOlderThan(content.getTimestamp());
    }

}
