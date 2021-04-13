package com.dci.intellij.dbn.language.common;

import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.thread.Read;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.PresentableConnectionProvider;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingProvider;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.lookup.ElementLookupContext;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.language.common.psi.lookup.IdentifierDefinitionLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.LookupAdapterCache;
import com.dci.intellij.dbn.language.common.psi.lookup.PsiLookupAdapter;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectPsiFacade;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.dci.intellij.dbn.vfs.DBParseableVirtualFile;
import com.dci.intellij.dbn.vfs.DBVirtualFile;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.dci.intellij.dbn.vfs.file.DBContentVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBObjectVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.ide.util.EditSourceUtil;
import com.intellij.injected.editor.VirtualFileWindow;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.ParserDefinition;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.Consumer;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Set;

public abstract class DBLanguagePsiFile extends PsiFileImpl implements FileConnectionMappingProvider, PresentableConnectionProvider, StatefulDisposable {
    private final Language language;
    private final DBLanguageFileType fileType;
    private final ParserDefinition parserDefinition;
    private ConnectionHandlerRef connectionHandler;
    private DatabaseSession databaseSession;
    private DBObjectRef<DBSchema> databaseSchema;
    private DBObjectRef<DBSchemaObject> underlyingObject;

    @Override
    public PsiElement getPrevSibling() {
        return null;
        //return super.getPrevSibling();
    }

    public DBLanguagePsiFile(FileViewProvider viewProvider, DBLanguageFileType fileType, DBLanguage language) {
        super(viewProvider);
        this.language = findLanguage(language);
        this.fileType = fileType;
        parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(language);
        if (parserDefinition == null) {
            throw new RuntimeException("PsiFileBase: language.getParserDefinition() returned null.");
        }
        VirtualFile virtualFile = viewProvider.getVirtualFile();
        if (virtualFile instanceof DBSourceCodeVirtualFile) {
            DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) virtualFile;
            this.underlyingObject = DBObjectRef.of(sourceCodeFile.getObject());
        }

        IFileElementType nodeType = parserDefinition.getFileNodeType();
        //assert nodeType.getLanguage() == this.language;
        init(nodeType, nodeType);
/*        if (viewProvider instanceof SingleRootFileViewProvider) {
            SingleRootFileViewProvider singleRootFileViewProvider = (SingleRootFileViewProvider) viewProvider;
            singleRootFileViewProvider.forceCachedPsi(this);
        }*/
    }

    @Nullable
    @Override
    public Icon getIcon() {
        VirtualFile virtualFile = getVirtualFile();
        if (virtualFile instanceof DBVirtualFile) {
            DBVirtualFile databaseVirtualFile = (DBVirtualFile) virtualFile;
            return databaseVirtualFile.getIcon();
        }
        return virtualFile.getFileType().getIcon();
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    public void setUnderlyingObject(DBSchemaObject underlyingObject) {
        this.underlyingObject = DBObjectRef.of(underlyingObject);
    }

    public DBObject getUnderlyingObject() {
        VirtualFile virtualFile = getVirtualFile();
        if (virtualFile != null) {
            if (virtualFile instanceof DBObjectVirtualFile) {
                DBObjectVirtualFile<?> databaseObjectFile = (DBObjectVirtualFile<?>) virtualFile;
                return databaseObjectFile.getObject();
            }

            if (virtualFile instanceof DBSourceCodeVirtualFile) {
                DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) virtualFile;
                return sourceCodeFile.getObject();
            }

            DDLFileAttachmentManager instance = DDLFileAttachmentManager.getInstance(getProject());
            DBSchemaObject editableObject = instance.getEditableObject(virtualFile);
            if (editableObject != null) {
                return editableObject;
            }
        }

        return DBObjectRef.get(underlyingObject);
    }

    public DBLanguagePsiFile(Project project, DBLanguageFileType fileType, @NotNull DBLanguage<?> language) {
        this(createFileViewProvider(project), fileType, language);
    }

    private static SingleRootFileViewProvider createFileViewProvider(Project project) {
        return new SingleRootFileViewProvider(PsiManager.getInstance(project), new LightVirtualFile());
    }

    private Language findLanguage(Language baseLanguage) {
        final FileViewProvider viewProvider = getViewProvider();
        final Set<Language> languages = viewProvider.getLanguages();
        for (final Language actualLanguage : languages) {
            if (actualLanguage.isKindOf(baseLanguage)) {
                return actualLanguage;
            }
        }
        throw new AssertionError(
                "Language " + baseLanguage + " doesn't participate in view provider " + viewProvider + ": " + new ArrayList<>(languages));
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        // TODO: check if any other visitor relevant
        String name = visitor.getClass().getName();
        if (name.contains("SpellCheckingInspection") || name.contains("InjectedLanguageManager")) {
            visitor.visitFile(this);
        }
    }

    @NotNull
    public ParserDefinition getParserDefinition() {
        return parserDefinition;
    }

    @Nullable
    public DBLanguageDialect getLanguageDialect() {
        VirtualFile virtualFile = getVirtualFile();
        if (virtualFile instanceof DBContentVirtualFile) {
            DBContentVirtualFile contentFile = (DBContentVirtualFile) virtualFile;
            return contentFile.getLanguageDialect();
        }
        
        if (language instanceof DBLanguage) {
            DBLanguage<?> dbLanguage = (DBLanguage<?>) language;
            ConnectionHandler connectionHandler = getConnectionHandler();
            if (connectionHandler != null) {

                DBLanguageDialect languageDialect = connectionHandler.getLanguageDialect(dbLanguage);
                if (languageDialect != null){
                    return languageDialect;
                }
            } else {
                return dbLanguage.getAvailableLanguageDialects()[0];
            }
        } else if (language instanceof DBLanguageDialect) {
            return (DBLanguageDialect) language;
        }
        
        return null;
    }

    @Override
    public VirtualFile getVirtualFile() {
/*
        PsiFile originalFile = getOriginalFile();
        return originalFile == this ?
                super.getVirtualFile() :
                originalFile.getVirtualFile();
*/
        return CommonUtil.nvl(super.getVirtualFile(), getViewProvider().getVirtualFile());
    }

    public boolean isInjectedContext() {
        return getVirtualFile() instanceof VirtualFileWindow;
    }

    private FileConnectionMappingManager getConnectionMappingManager() {
        return FileConnectionMappingManager.getInstance(getProject());
    }

    @Override
    @Nullable
    public ConnectionHandler getConnectionHandler() {
        VirtualFile file = getVirtualFile();
        if (file != null && !getProject().isDisposed()) {
            FileConnectionMappingManager connectionMappingManager = getConnectionMappingManager();
            return connectionMappingManager.getConnectionHandler(file);
        }
        return null;
    }

    public void setConnectionHandler(ConnectionHandler connectionHandler) {
        VirtualFile file = getVirtualFile();
        if (file != null) {
            FileConnectionMappingManager connectionMappingManager = getConnectionMappingManager();
            connectionMappingManager.setConnectionHandler(file, connectionHandler);
        }
    }

    @Override
    @Nullable
    public SchemaId getSchemaId() {
        VirtualFile file = getVirtualFile();
        if (file != null) {
            FileConnectionMappingManager connectionMappingManager = getConnectionMappingManager();
            return connectionMappingManager.getDatabaseSchema(file);
        }
        return null;
    }

    public void setDatabaseSchema(SchemaId schema) {
        VirtualFile file = getVirtualFile();
        if (file != null) {
            FileConnectionMappingManager connectionMappingManager = getConnectionMappingManager();
            connectionMappingManager.setDatabaseSchema(file, schema);
        }
    }

    @Override
    public DatabaseSession getDatabaseSession() {
        VirtualFile file = getVirtualFile();
        if (file != null && !getProject().isDisposed()) {
            FileConnectionMappingManager connectionMappingManager = getConnectionMappingManager();
            return connectionMappingManager.getDatabaseSession(file);
        }
        return null;
    }

    public void setDatabaseSession(DatabaseSession session) {
        VirtualFile file = getVirtualFile();
        if (file != null) {
            FileConnectionMappingManager connectionMappingManager = getConnectionMappingManager();
            connectionMappingManager.setDatabaseSession(file, session);
        }
    }

    @NotNull
    @Override
    public Language getLanguage() {
        return language;
    }

    public DBLanguage<?> getDBLanguage() {
        return language instanceof DBLanguage ? (DBLanguage<?>) language : null;
    }

    @Override
    public void navigate(boolean requestFocus) {
        Editor selectedEditor = EditorUtil.getSelectedEditor(getProject());
        if (selectedEditor != null) {
            Document document = DocumentUtil.getDocument(getContainingFile());
            if (document != null) {
                Editor[] editors = EditorFactory.getInstance().getEditors(document);
                for (Editor editor : editors) {
                    if (editor == selectedEditor) {
                        OpenFileDescriptor descriptor = (OpenFileDescriptor) EditSourceUtil.getDescriptor(this);
                        if (descriptor != null) {
                            descriptor.navigateIn(selectedEditor);
                            return;
                        }
                    }
                }
            }
        }
        if (!(getVirtualFile() instanceof DBParseableVirtualFile) && canNavigate()) {
            super.navigate(requestFocus);
        }
    }

    @Override
    @NotNull
    public DBLanguageFileType getFileType() {
        return fileType;
    }

    public ElementTypeBundle getElementTypeBundle() {
        DBLanguageDialect languageDialect = getLanguageDialect();
        languageDialect = CommonUtil.nvl(languageDialect, SQLLanguage.INSTANCE.getMainLanguageDialect());
        return languageDialect.getParserDefinition().getParser().getElementTypes();
    }

    @Override
    public PsiDirectory getParent() {
        DBObject underlyingObject = getUnderlyingObject();
        if (underlyingObject != null) {
            DBObject parentObject = underlyingObject.getParentObject();
            return DBObjectPsiFacade.asPsiDirectory(parentObject);

        }
        return Read.call(() -> DBLanguagePsiFile.super.getParent());
    }

    @Override
    public boolean isValid() {
        VirtualFile virtualFile = getViewProvider().getVirtualFile();
        return virtualFile.getFileSystem() instanceof DatabaseFileSystem ?
                virtualFile.isValid() :
                super.isValid();
    }

    public String getParseRootId() {
        VirtualFile virtualFile = getVirtualFile();
        if (virtualFile != null) {
            String parseRootId = virtualFile.getUserData(DBParseableVirtualFile.PARSE_ROOT_ID_KEY);
            if (parseRootId == null && virtualFile instanceof DBSourceCodeVirtualFile) {
                DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) virtualFile;
                parseRootId = sourceCodeFile.getParseRootId();
                if (parseRootId != null) {
                    virtualFile.putUserData(DBParseableVirtualFile.PARSE_ROOT_ID_KEY, parseRootId);
                }
            }

            return parseRootId;
        }
        return null;
    }

    public double getDatabaseVersion() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        return connectionHandler == null ? ElementLookupContext.MAX_DB_VERSION : connectionHandler.getDatabaseVersion();
    }

    @Nullable
    public static DBLanguagePsiFile createFromText(Project project, String fileName, DBLanguageDialect languageDialect, String text, ConnectionHandler activeConnection, SchemaId currentSchema) {
        PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);
        PsiFile rawPsiFile = psiFileFactory.createFileFromText(fileName, languageDialect, text);
        if (rawPsiFile instanceof DBLanguagePsiFile) {
            DBLanguagePsiFile psiFile = (DBLanguagePsiFile) rawPsiFile;
            psiFile.setConnectionHandler(activeConnection);
            psiFile.setDatabaseSchema(currentSchema);
            return psiFile;
        }
        return null;
    }

    public void lookupVariableDefinition(int offset, Consumer<BasePsiElement> consumer) {
        BasePsiElement scope = PsiUtil.lookupElementAtOffset(this, ElementTypeAttribute.SCOPE_DEMARCATION, offset);
        while (scope != null) {
            PsiLookupAdapter lookupAdapter = new IdentifierDefinitionLookupAdapter(null, DBObjectType.ARGUMENT, null);
            scope.collectPsiElements(lookupAdapter, 0, consumer);

            lookupAdapter = LookupAdapterCache.VARIABLE_DEFINITION.get(DBObjectType.ANY);
            scope.collectPsiElements(lookupAdapter, 0, consumer);

            PsiElement parent = scope.getParent();
            if (parent instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) parent;
                scope = basePsiElement.findEnclosingPsiElement(ElementTypeAttribute.SCOPE_DEMARCATION);
                if (scope == null) scope = basePsiElement.findEnclosingPsiElement(ElementTypeAttribute.SCOPE_ISOLATION);
            } else {
                scope = null;
            }
        }
    }

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    @Getter
    private boolean disposed;

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            markInvalidated();
            nullify();

            // TODO memory cleanup
            //markInvalidated();
/*
            FileElement treeElement = derefTreeElement();
            if (treeElement != null) {
                treeElement.detachFromFile();
            }
*/

        }
    }

    @NotNull
    public EnvironmentType getEnvironmentType() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        return connectionHandler == null ? EnvironmentType.DEFAULT :  connectionHandler.getEnvironmentType();
    }
}
