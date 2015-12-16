package com.dci.intellij.dbn.editor.code.diff;

public class SourceCodeDiffContent {
    private String title;
    private byte[] byteContent;

    public SourceCodeDiffContent(String title, CharSequence content) {
        this.title = title;
        this.byteContent = content.toString().getBytes();
    }

    public String getTitle() {
        return title;
    }

    public byte[] getByteContent() {
        return byteContent;
    }
}
