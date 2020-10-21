package com.dci.intellij.dbn.editor.code.content;

import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.intellij.diff.comparison.ByWord;
import com.intellij.diff.comparison.ComparisonPolicy;
import com.intellij.openapi.progress.ProgressIndicator;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.dci.intellij.dbn.editor.code.content.GuardedBlockMarker.END_OFFSET_IDENTIFIER;
import static com.dci.intellij.dbn.editor.code.content.GuardedBlockMarker.START_OFFSET_IDENTIFIER;

public class SourceCodeContent{
    private static final String EMPTY_CONTENT = "";
    protected CharSequence text = EMPTY_CONTENT;
    private final SourceCodeOffsets offsets = new SourceCodeOffsets();

    public SourceCodeContent() {
    }

    public SourceCodeContent(CharSequence text) {
        this.text = text;
    }

    public CharSequence getText() {
        return text;
    }

    public void setText(CharSequence text) {
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
        return StringUtil.equals(text, content.text);
    }

    public long length() {
        return text.length();
    }

    @Override
    public String toString() {
        return text.toString();
    }

    @NotNull
    public SourceCodeOffsets getOffsets() {
        return offsets;
    }

    public void importContent(String content) {
        offsets.getGuardedBlocks().reset();
        StringBuilder builder = new StringBuilder(CommonUtil.nvl(content, ""));
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
}
