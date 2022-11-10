package com.dci.intellij.dbn.editor.code.content;

import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.util.Commons;
import com.dci.intellij.dbn.common.util.Strings;
import com.intellij.diff.comparison.ByWord;
import com.intellij.diff.comparison.ComparisonPolicy;
import com.intellij.openapi.progress.ProgressIndicator;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.dci.intellij.dbn.editor.code.content.GuardedBlockMarker.END_OFFSET_IDENTIFIER;
import static com.dci.intellij.dbn.editor.code.content.GuardedBlockMarker.START_OFFSET_IDENTIFIER;

@Getter
@Setter
public class SourceCodeContent{
    private static final String EMPTY_CONTENT = "";
    private final SourceCodeOffsets offsets = new SourceCodeOffsets();
    protected CharSequence text = EMPTY_CONTENT;

    public SourceCodeContent() {
    }

    public SourceCodeContent(CharSequence text) {
        this.text = text;
    }

    public boolean isLoaded() {
        return text != EMPTY_CONTENT;
    }

    public void reset() {
        text = EMPTY_CONTENT;
    }

    public byte[] getBytes(Charset charset) {
        return text.toString().getBytes(charset);
    }

    public boolean matches(SourceCodeContent content, boolean soft) {
        if (soft) {
            try {
                ProgressIndicator progressIndicator = ProgressMonitor.getProgressIndicator();
                return ByWord.compare(text, content.text, ComparisonPolicy.IGNORE_WHITESPACES, progressIndicator).isEmpty();
            } catch (Exception ignore) {
            }
        }
        return Strings.equals(text, content.text);
    }

    public long length() {
        return text.length();
    }

    @Override
    public String toString() {
        return text.toString();
    }

    public void importContent(String content) {
        offsets.getGuardedBlocks().reset();
        StringBuilder builder = new StringBuilder(Commons.nvl(content, ""));
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
        List<GuardedBlockMarker> ranges = new ArrayList<>(offsets.getGuardedBlocks().getRanges());
        ranges.sort(Collections.reverseOrder());
        for (GuardedBlockMarker range : ranges) {
            builder.insert(range.getEndOffset(), END_OFFSET_IDENTIFIER);
            builder.insert(range.getStartOffset(), START_OFFSET_IDENTIFIER);
        }
        return builder.toString();
    }
}
