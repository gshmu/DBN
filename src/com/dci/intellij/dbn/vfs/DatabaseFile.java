package com.dci.intellij.dbn.vfs;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.intellij.psi.PsiFile;

public interface DatabaseFile {
    ConnectionHandler getConnectionHandler();

    PsiFile initializePsiFile(DatabaseFileViewProvider fileViewProvider, DBLanguage language);
}
