package com.dci.intellij.dbn.vfs;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.lang.Language;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

public interface DatabaseFile {
    public static final Key<VirtualFile> FILE_KEY = Key.create("FILE_KEY");
    public static final Key<String> PARSE_ROOT_ID_KEY = new Key<String>("DBN_PARSE_ROOT_ID");

    ConnectionHandler getConnectionHandler();

    PsiFile initializePsiFile(DatabaseFileViewProvider fileViewProvider, Language language);
}
