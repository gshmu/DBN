package com.dci.intellij.dbn.language.psql;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.DBLanguageFileType;

import javax.swing.*;

public class PSQLFileType extends DBLanguageFileType {

    public static final PSQLFileType INSTANCE = new PSQLFileType();
    public static final String[] PSQL_FILE_EXTENSIONS = {"psql", "plsql", "trg", "prc", "fnc", "pkg", "pks", "pkb", "tpe", "tps", "tpb"}; // same as in plugin.xml (keep in sync)

    private PSQLFileType() {
        super(PSQLLanguage.INSTANCE,
                PSQL_FILE_EXTENSIONS,
                "PSQL file (DBN)",
                DBContentType.CODE);
    }

    @Override
    public Icon getIcon() {
        return Icons.FILE_PLSQL;
    }


}