package com.dci.intellij.dbn.language.sql;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.DBLanguageFileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class SQLFileType extends DBLanguageFileType {

    public static final SQLFileType INSTANCE = new SQLFileType();

    public SQLFileType() {
        super(SQLLanguage.INSTANCE, "sql", "SQL files (DBN)", DBContentType.CODE);
    }


    @Override
    @NotNull
    public String getName() {
        return "DBN-SQL";
    }

    @Override
    public Icon getIcon() {
        return Icons.FILE_SQL;
    }


}
