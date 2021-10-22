package com.dci.intellij.dbn.editor.code.diff;

import java.util.ArrayList;
import java.util.List;

public class MergeContent {
    private final List<SourceCodeDiffContent> contents = new ArrayList<>();

    public MergeContent(SourceCodeDiffContent leftContent, SourceCodeDiffContent targetContent, SourceCodeDiffContent rightContent) {
        contents.add(leftContent);
        contents.add(targetContent);
        contents.add(rightContent);
    }

    public List<String> getTitles() {
        ArrayList<String> titles = new ArrayList<String>();
        for (SourceCodeDiffContent content : contents) {
            titles.add(content.getTitle());
        }

        return titles;
    }

    public List<byte[]> getByteContents() {
        ArrayList<byte[]> byteContents = new ArrayList<byte[]>();
        for (SourceCodeDiffContent content : contents) {
            byteContents.add(content.getByteContent());
        }

        return byteContents;
    }

}
