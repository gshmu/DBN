package com.dci.intellij.dbn.execution.logging;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import com.dci.intellij.dbn.common.util.CommonUtil;

public class DatabaseLogOutputReader extends Reader {
    private StringReader inner;

    public DatabaseLogOutputReader(String string) {
        setString(string);
    }

    public void setString(String string) {
        this.inner = new StringReader(CommonUtil.nvl(string, ""));
    }

    @Override
    public int read(char[] buffer, int off, int len) throws IOException {
        return inner.read(buffer, off, len);
    }

    @Override
    public void close() throws IOException {
        inner.close();
    }


}
