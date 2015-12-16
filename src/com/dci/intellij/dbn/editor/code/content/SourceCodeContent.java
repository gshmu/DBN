package com.dci.intellij.dbn.editor.code.content;

import java.nio.charset.Charset;

public interface SourceCodeContent {
    CharSequence getText();

    void setText(CharSequence text);

    boolean isLoaded();

    void reset();

    byte[] getBytes(Charset charset);

    boolean isSameAs(SourceCodeContent content);
}
