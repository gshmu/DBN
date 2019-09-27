package com.dci.intellij.dbn.language.psql;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.DBLanguageFileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class PSQLFileType extends DBLanguageFileType {

    public static final PSQLFileType INSTANCE = new PSQLFileType();

    private PSQLFileType() {
        super(PSQLLanguage.INSTANCE, "psql", "PSQL files (DBN)", DBContentType.CODE);
    }


    @Override
    @NotNull
    public String getName() {
        return "DBN-PSQL";
    }

    @Override
    public Icon getIcon() {
        return Icons.FILE_PLSQL;
    }


}