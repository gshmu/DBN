package com.dci.intellij.dbn.language.common;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionContextManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dci.intellij.dbn.common.dispose.Checks.isNotValid;
import static com.dci.intellij.dbn.common.util.Files.isDbLanguageFile;

public class DBSyntaxHighlighterProvider implements SyntaxHighlighterProvider {
    @Override
    @Nullable
    public SyntaxHighlighter create(@NotNull FileType fileType, @Nullable Project project, @Nullable VirtualFile file) {
        if (isNotValid(project)) return null;
        if (isNotValid(file)) return null;
        if (!isDbLanguageFile(file)) return null;

        DBLanguageFileType dbFileType = (DBLanguageFileType) fileType;
        DBLanguage language = (DBLanguage) dbFileType.getLanguage();
        DBLanguageDialect mainLanguageDialect = language.getMainLanguageDialect();

        FileConnectionContextManager contextManager = FileConnectionContextManager.getInstance(project);
        ConnectionHandler connection = contextManager.getConnection(file);

        DBLanguageDialect languageDialect = connection == null ?
                mainLanguageDialect :
                connection.getLanguageDialect(language);

        return languageDialect == null ?
                mainLanguageDialect.getSyntaxHighlighter() :
                languageDialect.getSyntaxHighlighter();
    }
}
