package com.dci.intellij.dbn.editor.code.content;

import java.nio.charset.Charset;

import com.dci.intellij.dbn.common.util.StringUtil;

public class BasicSourceCodeContent implements SourceCodeContent {
    private static final String EMPTY_CONTENT = "";
    protected CharSequence text = EMPTY_CONTENT;

    @Override
    public CharSequence getText() {
        return text;
    }

    @Override
    public void setText(CharSequence text) {
        this.text = text;
    }

    @Override
    public boolean isLoaded() {
        return text != EMPTY_CONTENT;
    }

    @Override
    public void reset() {
        text = EMPTY_CONTENT;
    }

    @Override
    public byte[] getBytes(Charset charset) {
        return text.toString().getBytes(charset);
    }

    @Override
    public boolean isSameAs(SourceCodeContent content) {
        return StringUtil.equals(text, content.getText());
    }

    public long length() {
        return text.length();
    }

    @Override
    public String toString() {
        return text.toString();
    }
}
