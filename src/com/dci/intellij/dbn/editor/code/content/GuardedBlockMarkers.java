package com.dci.intellij.dbn.editor.code.content;

import com.intellij.openapi.editor.RangeMarker;

import java.util.ArrayList;
import java.util.List;

public class GuardedBlockMarkers {
    private List<GuardedBlockMarker> ranges = new ArrayList<GuardedBlockMarker>();

    public void addMarker(int startOffset, int endOffset) {
        ranges.add(new GuardedBlockMarker(startOffset, endOffset));
    }

    public List<GuardedBlockMarker> getRanges() {
        return ranges;
    }

    public void apply(List<RangeMarker> rangeMarkers) {
        ranges.clear();
        for (RangeMarker rangeMarker : rangeMarkers) {
            addMarker(
                rangeMarker.getStartOffset(),
                rangeMarker.getEndOffset());
        }
    }

    public boolean isEmpty() {
        return ranges.isEmpty();
    }
}
