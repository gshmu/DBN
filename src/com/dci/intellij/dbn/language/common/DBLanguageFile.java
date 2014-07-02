package com.dci.intellij.dbn.language.common;

import com.dci.intellij.dbn.common.compatibility.CompatibilityUtil;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.common.util.VirtualFileUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingProvider;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.psi.NamedPsiElement;
import com.dci.intellij.dbn.navigation.psi.NavigationPsiCache;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.DatabaseContentFile;
import com.dci.intellij.dbn.vfs.DatabaseFile;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.dci.intellij.dbn.vfs.DatabaseObjectFile;
import com.dci.intellij.dbn.vfs.SourceCodeFile;
import com.intellij.ide.util.EditSourceUtil;
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
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Set;

public abstract class DBLanguageFile extends PsiFileImpl implements FileConnectionMappingProvider {
    private Language language;
    private DBLanguageFileType fileType;
    private ParserDefinition parserDefinition;
    private String parseRootId;
    private ConnectionHandler activeConnection;
    private DBSchema currentSchema;
    private DBObjectRef underlyingObject;

    public DBLanguageFile(FileViewProvider viewProvider, DBLanguageFileType fileType, DBLanguage language) {
        super(viewProvider);
        this.language = findLanguage(language);
        this.fileType = fileType;
        parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(language);
        if (parserDefinition == null) {
            throw new RuntimeException("PsiFileBase: language.getParserDefinition() returned null.");
        }
        VirtualFile virtualFile = viewProvider.getVirtualFile();
        if (virtualFile instanceof SourceCodeFile) {
            SourceCodeFile sourceCodeFile = (SourceCodeFile) virtualFile;
            this.underlyingObject = sourceCodeFile.getObject().getRef();
        }

        parseRootId = CompatibilityUtil.getParseRootId(virtualFile);

        IFileElementType nodeType = parserDefinition.getFileNodeType();
        //assert nodeType.getLanguage() == this.language;
        init(nodeType, nodeType);
    }

    public void setUnderlyingObject(DBObject underlyingObject) {
        this.underlyingObject = underlyingObject.getRef();
    }

    public DBObject getUnderlyingObject() {
        VirtualFile virtualFile = getVirtualFile();
        if (virtualFile instanceof DatabaseObjectFile) {
            DatabaseObjectFile databaseObjectFile = (DatabaseObjectFile) virtualFile;
            return databaseObjectFile.getObject();
        }

        if (virtualFile instanceof SourceCodeFile) {
            SourceCodeFile sourceCodeFile = (SourceCodeFile) virtualFile;
            return sourceCodeFile.getObject();
        }

        DDLFileAttachmentManager instance = DDLFileAttachmentManager.getInstance(getProject());
        DBSchemaObject editableObject = instance.getEditableObject(virtualFile);
        if (editableObject != null) {
            return editableObject;
        }


        return underlyingObject == null ? null : underlyingObject.get();
    }

    public DBLanguageFile(Project project,  DBLanguageFileType fileType, @NotNull DBLanguage language) {
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
                "Language " + baseLanguage + " doesn't participate in view provider " + viewProvider + ": " + new ArrayList<Language>(languages));
    }

    public void setParseRootId(String parseRootId) {
        this.parseRootId = parseRootId;
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        visitor.visitFile(this);
    }

    @NotNull
    public ParserDefinition getParserDefinition() {
        return parserDefinition;
    }

    @NonNls
    @Nullable
    public DBLanguageDialect getLanguageDialect() {
        VirtualFile virtualFile = getVirtualFile();
        if (virtualFile instanceof DatabaseContentFile) {
            DatabaseContentFile contentFile = (DatabaseContentFile) virtualFile;
            return contentFile.getLanguageDialect();
        }
        
        Language language = getLanguage();
        if (language instanceof DBLanguage) {
            DBLanguage dbLanguage = (DBLanguage) language;
            ConnectionHandler connectionHandler = getActiveConnection();
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

    public VirtualFile getVirtualFile() {
        DBLanguageFile originalFile = (DBLanguageFile) getOriginalFile();
        return originalFile == null || originalFile == this ?
                super.getVirtualFile() :
                originalFile.getVirtualFile();

    }

    private FileConnectionMappingManager getConnectionMappingManager() {
        return FileConnectionMappingManager.getInstance(getProject());
    }

    @Nullable
    public ConnectionHandler getActiveConnection() {
        VirtualFile file = getVirtualFile();
        if (file != null) {
            if (VirtualFileUtil.isVirtualFileSystem(file)) {
                DBLanguageFile originalFile = (DBLanguageFile) getOriginalFile();
                return originalFile == null || originalFile == this ? activeConnection : originalFile.getActiveConnection();
            } else {
                return getConnectionMappingManager().getActiveConnection(file);
            }
        }
        return null;
    }

    public void setActiveConnection(ConnectionHandler activeConnection) {
        VirtualFile file = getVirtualFile();
        if (file != null) {
            if (VirtualFileUtil.isVirtualFileSystem(file)) {
                this.activeConnection = activeConnection;
            } else {
                getConnectionMappingManager().setActiveConnection(file, activeConnection);
            }
        }
    }

    public DBSchema getCurrentSchema() {
        VirtualFile file = getVirtualFile();
        if (file != null) {
            if (VirtualFileUtil.isVirtualFileSystem(file)) {
                DBLanguageFile originalFile = (DBLanguageFile) getOriginalFile();
                return originalFile == null || originalFile == this ? currentSchema : originalFile.getCurrentSchema();
            } else {
                return getConnectionMappingManager().getCurrentSchema(file);
            }
        }
        return null;
    }

    public void setCurrentSchema(DBSchema schema) {
        VirtualFile file = getVirtualFile();
        if (file != null) {
            if (VirtualFileUtil.isVirtualFileSystem(file)) {
                this.currentSchema = schema;
            } else {
                getConnectionMappingManager().setCurrentSchema(file, schema);
            }
        }
    }

    public boolean contains(NamedPsiElement element, boolean leniant) {
        PsiElement child = getFirstChild();
        while (child != null) {
            if (child instanceof NamedPsiElement) {
                NamedPsiElement namedPsiElement = (NamedPsiElement) child;
                if (namedPsiElement == element) {
                    return true;
                }
            }
            child = child.getNextSibling();
        }
        if (leniant) {
            child = getFirstChild();
            while (child != null) {
                if (child instanceof NamedPsiElement) {
                    NamedPsiElement namedPsiElement = (NamedPsiElement) child;
                    if (namedPsiElement.matches(element)) {
                        return true;
                    }
                }
                child = child.getNextSibling();
            }
        }

        return false;
    }

    @NotNull
    @Override
    public Language getLanguage() {
        return language;
    }

    public DBLanguage getDBLanguage() {
        return language instanceof DBLanguage ? (DBLanguage) language : null;
    }

    @Override
    public void navigate(boolean requestFocus) {
        Editor selectedEditor = EditorUtil.getSelectedEditor(getProject());
        if (selectedEditor != null) {
            Document document = DocumentUtil.getDocument(getContainingFile());
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
        if (!(getVirtualFile() instanceof DatabaseFile)) {
            super.navigate(requestFocus);
        }
    }

    @NotNull
    public DBLanguageFileType getFileType() {
        return fileType;
    }

    public String getParseRootId() {
        return parseRootId;
    }

    public ElementTypeBundle getElementTypeBundle() {
        return getLanguageDialect().getParserDefinition().getParser().getElementTypes();
    }

    @Override
    public PsiDirectory getParent() {
        DBObject underlyingObject = getUnderlyingObject();
        if (underlyingObject != null) {
            DBObject parentObject = underlyingObject.getParentObject();
            return NavigationPsiCache.getPsiDirectory(parentObject);

        }
        return super.getParent();
    }

    @Override
    public boolean isValid() {
        VirtualFile virtualFile = getViewProvider().getVirtualFile();
        return virtualFile.getFileSystem() instanceof DatabaseFileSystem ?
                virtualFile.isValid() :
                super.isValid();
    }
}
