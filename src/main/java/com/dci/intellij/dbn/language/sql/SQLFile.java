package com.dci.intellij.dbn.language.sql;

import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.intellij.psi.FileViewProvider;

public class SQLFile extends DBLanguagePsiFile {
    SQLFile(FileViewProvider fileViewProvider) {
        super(fileViewProvider, SQLFileType.INSTANCE, SQLLanguage.INSTANCE);
    }
}