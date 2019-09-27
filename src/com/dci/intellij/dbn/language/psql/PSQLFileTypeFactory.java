package com.dci.intellij.dbn.language.psql;

import com.dci.intellij.dbn.language.common.DBLanguageFileTypeFactory;
import com.intellij.openapi.fileTypes.FileType;

@Deprecated
public class PSQLFileTypeFactory extends DBLanguageFileTypeFactory {

    @Override
    protected FileType getFileType() {
        return PSQLFileType.INSTANCE;
    }
}