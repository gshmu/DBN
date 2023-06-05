package com.dci.intellij.dbn.language.sql;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.DBLanguageFileType;

import javax.swing.*;

public class SQLFileType extends DBLanguageFileType {

    public static final SQLFileType INSTANCE = new SQLFileType();
    public static final String[] SQL_FILE_EXTENSIONS = {"sql", "ddl", "vw"}; // same as in plugin.xml (keep in sync)

    public SQLFileType() {
        super(
            SQLLanguage.INSTANCE,
            SQL_FILE_EXTENSIONS,
            "SQL file (DBN)",
            DBContentType.CODE);
    }

    @Override
    public Icon getIcon() {
        return Icons.FILE_SQL;
    }


}
