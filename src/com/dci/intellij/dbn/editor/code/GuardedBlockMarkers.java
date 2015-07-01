package com.dci.intellij.dbn.editor.code;

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
}
