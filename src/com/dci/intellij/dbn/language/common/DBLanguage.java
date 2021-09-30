package com.dci.intellij.dbn.language.common;

import com.dci.intellij.dbn.code.common.style.options.CodeStyleCustomSettings;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.language.psql.PSQLLanguage;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.tree.IFileElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class DBLanguage<D extends DBLanguageDialect> extends Language implements DBFileElementTypeProvider {

    private final Latent<D[]> languageDialects = Latent.basic(() -> createLanguageDialects());
    private final Latent<IFileElementType> fileElementType = Latent.basic(() -> createFileElementType(DBLanguage.this));
    private final Latent<SharedTokenTypeBundle> sharedTokenTypes = Latent.basic(() -> new SharedTokenTypeBundle(this));

    protected DBLanguage(final @NonNls String id, final @NonNls String... mimeTypes){
        super(id, mimeTypes);
    }

    @Override
    public final IFileElementType getFileElementType() {
        return fileElementType.get();
    }

    protected abstract IFileElementType createFileElementType(DBLanguage<D> language);


    public SharedTokenTypeBundle getSharedTokenTypes() {
        return sharedTokenTypes.get();
    }

    protected abstract D[] createLanguageDialects();
    public abstract D getMainLanguageDialect();

    public D getLanguageDialect(Project project, VirtualFile virtualFile) {
        FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
        ConnectionHandler connectionHandler = connectionMappingManager.getConnectionHandler(virtualFile);
        if (connectionHandler != null) {
            return (D) connectionHandler.getLanguageDialect(this);
        }
        return getMainLanguageDialect();
    }

    @NotNull
    public D[] getAvailableLanguageDialects() {
        return languageDialects.get();
    }

    public D getLanguageDialect(DBLanguageDialectIdentifier id) {
        for (D languageDialect: getAvailableLanguageDialects()) {
            if (Objects.equals(languageDialect.getID(), id.getValue())) {
                return languageDialect;
            }
        }
        return null;
    }

    public abstract CodeStyleCustomSettings getCodeStyleSettings(Project project);

    public DBLanguageParserDefinition getParserDefinition(ConnectionHandler connectionHandler) {
        return connectionHandler.getLanguageDialect(this).getParserDefinition();
    }

    public static DBLanguage getLanguage(String identifier) {
        if (identifier.equalsIgnoreCase("SQL")) return SQLLanguage.INSTANCE;
        if (identifier.equalsIgnoreCase("PSQL")) return PSQLLanguage.INSTANCE;
        return null;
    }
}
