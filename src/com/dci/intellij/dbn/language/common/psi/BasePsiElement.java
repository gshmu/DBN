package com.dci.intellij.dbn.language.common.psi;

import com.dci.intellij.dbn.code.common.style.formatting.FormattingAttributes;
import com.dci.intellij.dbn.code.common.style.formatting.FormattingDefinition;
import com.dci.intellij.dbn.code.common.style.formatting.FormattingProviderPsiElement;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.editor.BasicTextEditor;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.thread.Read;
import com.dci.intellij.dbn.common.thread.Synchronized;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.editor.ddl.DDLFileEditor;
import com.dci.intellij.dbn.editor.session.SessionBrowser;
import com.dci.intellij.dbn.editor.session.ui.SessionBrowserForm;
import com.dci.intellij.dbn.execution.NavigationInstruction;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.QuoteDefinition;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.element.util.IdentifierCategory;
import com.dci.intellij.dbn.language.common.psi.lookup.ObjectLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.ObjectReferenceLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.PsiLookupAdapter;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.DBObjectPsiElement;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBVirtualObject;
import com.dci.intellij.dbn.vfs.file.DBConsoleVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBSessionStatementVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.ide.util.EditSourceUtil;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

public abstract class BasePsiElement<T extends ElementType> extends ASTWrapperPsiElement implements ItemPresentation, FormattingProviderPsiElement {
    public T elementType;
    private DBVirtualObject underlyingObject;
    private FormattingAttributes formattingAttributes;

    private Latent<BasePsiElement> enclosingScopePsiElement = Latent.weak(() -> findEnclosingScopePsiElement());

    public enum MatchType {
        STRONG,
        CACHED,
        SOFT,
    }

    public BasePsiElement(ASTNode astNode, T elementType) {
        super(astNode);
        this.elementType = elementType;
    }

    @Override
    public FormattingAttributes getFormattingAttributes() {
        FormattingDefinition formattingDefinition = elementType.getFormatting();
        if (formattingAttributes == null && formattingDefinition != null) {
            formattingAttributes = FormattingAttributes.copy(formattingDefinition.getAttributes());
        }

        return formattingAttributes;
    }

    @Override
    public FormattingAttributes getFormattingAttributesRecursive(boolean left) {
        FormattingAttributes formattingAttributes = getFormattingAttributes();
        if (formattingAttributes == null) {
            PsiElement psiElement = left ? getFirstChild() : getLastChild();
            if (psiElement instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) psiElement;
                return basePsiElement.getFormattingAttributesRecursive(left);
            }
        }
        return formattingAttributes;
    }

    public boolean containsLineBreaks() {
        return StringUtil.containsLineBreak(getNode().getChars());
    }

    @Override
    public PsiElement getFirstChild() {
        ASTNode firstChildNode = getNode().getFirstChildNode();
        return firstChildNode == null ? null : firstChildNode.getPsi();
    }

    @Override
    public PsiElement getNextSibling() {
        ASTNode treeNext = getNode().getTreeNext();
        return treeNext == null ? null : treeNext.getPsi();
    }

    @Override
    public BasePsiElement getOriginalElement() {
        PsiFile containingFile = getContainingFile();
        PsiFile originalFile = containingFile.getOriginalFile();
        if (originalFile == containingFile) {
            return this;
        }
        int startOffset = getTextOffset();

        PsiElement psiElement = originalFile.findElementAt(startOffset);
        while (psiElement != null) {
            int elementStartOffset = psiElement.getTextOffset();
            if (elementStartOffset < startOffset) {
                break;
            }
            if (psiElement instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) psiElement;
                boolean isSameElement = basePsiElement.elementType == elementType;
                boolean isIdentifier = basePsiElement instanceof IdentifierPsiElement && this instanceof IdentifierPsiElement;
                if ((isSameElement || isIdentifier) && elementStartOffset == startOffset) {
                    return basePsiElement;
                }
            }
            psiElement = psiElement.getParent();
        }

        return this;
    }

    public boolean isOriginalElement() {
        PsiFile containingFile = getContainingFile();
        PsiFile originalFile = containingFile.getOriginalFile();
        return originalFile == containingFile;

    }

    public boolean isInjectedContext() {
        DBLanguagePsiFile file = getFile();
        return file.isInjectedContext();
    }

    public String getReferenceQualifiedName() {
        return isVirtualObject() ? "virtual " + elementType.getVirtualObjectType().getName() : "[unknown element]";
    }

    public abstract int approximateLength();

    @NotNull
    public DBLanguagePsiFile getFile() {
        PsiElement parent = getParent();
        while (parent != null && !(parent instanceof DBLanguagePsiFile)) {
            parent = parent.getParent();
        }
        return Failsafe.get((DBLanguagePsiFile) parent);
    }

    @Nullable
    public ConnectionHandler getConnectionHandler() {
        DBLanguagePsiFile file = getFile();
        return file.getConnectionHandler();
    }

    @Nullable
    public DBSchema getDatabaseSchema() {
        DBLanguagePsiFile file = getFile();
        ConnectionHandler connectionHandler = getConnectionHandler();
        SchemaId databaseSchema = file.getSchemaId();
        if (connectionHandler != null && databaseSchema != null) {
            DBObjectBundle objectBundle = connectionHandler.getObjectBundle();
            return objectBundle.getSchema(databaseSchema.id());
        }

        return null;
    }

    public String toString() {
        //return elementType.is(ElementTypeAttribute.SCOPE_DEMARCATION);
        return hasErrors() ?
                "[INVALID] " + elementType.getDebugName() :
                elementType.getDebugName() +
                        (isScopeDemarcation() ? " SCOPE_DEMARCATION" : "") +
                        (isScopeIsolation() ? " SCOPE_ISOLATION" : "");
    }

    @Override
    public void acceptChildren(@NotNull PsiElementVisitor visitor) {
            final PsiElement psiChild = getFirstChild();
        if (psiChild == null) return;

        ASTNode child = psiChild.getNode();
        while (child != null) {
            if (child.getPsi() != null) {
                child.getPsi().accept(visitor);
            }
            child = child.getTreeNext();
        }
    }

    @NotNull
    @Override
    public SearchScope getUseScope() {
        return new LocalSearchScope(getFile());
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        // TODO: check if any visitor relevant
        super.accept(visitor);
    }

    @Override
    public String getText() {
        if (ApplicationManager.getApplication().isReadAccessAllowed()) {
            return super.getText();
        }
        return Read.call(() -> BasePsiElement.super.getText());
    }


    @Override
    public PsiElement findElementAt(int offset) {
        return super.findElementAt(offset);
    }

    public PsiElement getLastChildIgnoreWhiteSpace() {
        PsiElement psiElement = this.getLastChild();
        while (psiElement instanceof PsiWhiteSpace) {
            psiElement = psiElement.getPrevSibling();
        }
        return psiElement;
    }

    @Nullable
    public BasePsiElement getPrevElement() {
        PsiElement preElement = getPrevSibling();
        while (preElement instanceof PsiWhiteSpace || preElement instanceof PsiComment) {
            preElement = preElement.getPrevSibling();
        }

        if (preElement instanceof BasePsiElement) {
            BasePsiElement previous = (BasePsiElement) preElement;
            while (previous.getLastChild() instanceof BasePsiElement) {
                previous = (BasePsiElement) previous.getLastChild();
            }
            return previous;
        }
        return null;
    }

    public LeafPsiElement getPrevLeaf() {
        PsiElement previousElement = getPrevSibling();
        while (previousElement instanceof PsiWhiteSpace || previousElement instanceof PsiComment) {
            previousElement = previousElement.getPrevSibling();
        }

        // is first in parent
        if (previousElement == null) {
            PsiElement parent = getParent();
            if (parent instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) parent;
                return basePsiElement.getPrevLeaf();
            }
        } else if (previousElement instanceof LeafPsiElement) {
            return (LeafPsiElement) previousElement;
        } else if (previousElement instanceof BasePsiElement) {
            BasePsiElement basePsiElement = (BasePsiElement) previousElement;
            PsiElement lastChild = basePsiElement.getLastChild();
            while (lastChild != null) {
                lastChild = lastChild.getLastChild();
                if (lastChild instanceof LeafPsiElement) {
                    return (LeafPsiElement) lastChild;
                }
            }
        }
        return null;
    }

    protected BasePsiElement getNextElement() {
        PsiElement nextElement = getNextSibling();
        while (nextElement instanceof PsiWhiteSpace || nextElement instanceof PsiComment || nextElement instanceof PsiErrorElement) {
            nextElement = nextElement.getNextSibling();
        }
        BasePsiElement next = (BasePsiElement) nextElement;
        while (next != null && next.getFirstChild() instanceof BasePsiElement) {
            next = (BasePsiElement) next.getFirstChild();
        }
        return next;
    }

    public boolean isVirtualObject() {
        return elementType.isVirtualObject();
    }

    @Override
    public void navigate(boolean requestFocus) {
        if (isValid()) {
            OpenFileDescriptor descriptor = (OpenFileDescriptor) EditSourceUtil.getDescriptor(this);
            if (descriptor != null) {
                VirtualFile virtualFile = getFile().getVirtualFile();
                Project project = getProject();
                FileEditorManager editorManager = FileEditorManager.getInstance(project);
                if (virtualFile != null) {
                    if (virtualFile instanceof DBSourceCodeVirtualFile) {
                        DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) virtualFile;
                        DBEditableObjectVirtualFile databaseFile = sourceCodeFile.getMainDatabaseFile();
                        if (!editorManager.isFileOpen(databaseFile)) {
                            editorManager.openFile(databaseFile, requestFocus);
                        }
                        BasicTextEditor textEditor = EditorUtil.getTextEditor((DBSourceCodeVirtualFile) virtualFile);
                        if (textEditor != null) {
                            Editor editor = textEditor.getEditor();
                            descriptor.navigateIn(editor);
                            if (requestFocus) EditorUtil.focusEditor(editor);
                        }
                        return;
                    }

                    if (virtualFile instanceof DBConsoleVirtualFile) {
                        DBConsoleVirtualFile consoleVirtualFile = (DBConsoleVirtualFile) virtualFile;
                        BasicTextEditor textEditor = EditorUtil.getTextEditor(consoleVirtualFile);
                        if (textEditor != null) {
                            Editor editor = textEditor.getEditor();
                            descriptor.navigateIn(editor);
                            if (requestFocus) EditorUtil.focusEditor(editor);
                        }
                        return;
                    }

                    if (virtualFile instanceof DBSessionStatementVirtualFile) {
                        DBSessionStatementVirtualFile sessionBrowserStatementFile = (DBSessionStatementVirtualFile) virtualFile;
                        SessionBrowser sessionBrowser = sessionBrowserStatementFile.getSessionBrowser();
                        if (sessionBrowser != null) {
                            SessionBrowserForm editorForm = sessionBrowser.getEditorForm();
                            EditorEx viewer = editorForm.getDetailsForm().getCurrentSqlPanel().getViewer();
                            if (viewer != null) {
                                descriptor.navigateIn(viewer);
                                if (requestFocus) EditorUtil.focusEditor(viewer);
                            }
                        }
                        return;
                    }

                    FileEditor[] fileEditors = editorManager.getSelectedEditors();
                    for (FileEditor fileEditor : fileEditors) {
                        if (fileEditor instanceof DDLFileEditor) {
                            DDLFileEditor textEditor = (DDLFileEditor) fileEditor;
                            if (textEditor.getVirtualFile().equals(virtualFile)) {
                                Editor editor = textEditor.getEditor();
                                descriptor.navigateIn(editor);
                                if (requestFocus) EditorUtil.focusEditor(editor);
                                return;
                            }

                        }
                    }

                    super.navigate(requestFocus);
                }
            }
        }
    }

    public void navigateInEditor(@NotNull FileEditor fileEditor, NavigationInstruction instruction) {
        OpenFileDescriptor descriptor = (OpenFileDescriptor) EditSourceUtil.getDescriptor(this);
        if (descriptor != null) {
            Editor editor = EditorUtil.getEditor(fileEditor);
            if (editor != null) {
                if (instruction.isScroll()) descriptor.navigateIn(editor);
                if (instruction.isFocus()) EditorUtil.focusEditor(editor);
                //TODO instruction.isOpen();
            }
        }
    }

    /*********************************************************
     *                   Lookup routines                     *
     *********************************************************/
    public Set<BasePsiElement> collectObjectPsiElements(Set<BasePsiElement> bucket, Set<DBObjectType> objectTypes, IdentifierCategory identifierCategory) {
        for (DBObjectType objectType : objectTypes) {
            PsiLookupAdapter lookupAdapter = new ObjectLookupAdapter(null, identifierCategory, objectType, null);
            bucket = lookupAdapter.collectInElement(this, bucket);
        }
        return bucket;
    }

    @Nullable
    public Set<DBObject> collectObjectReferences(DBObjectType objectType) {
        PsiLookupAdapter lookupAdapter = new ObjectReferenceLookupAdapter(null, objectType, null);
        Set<BasePsiElement> bucket = lookupAdapter.collectInElement(this, null);
        Set<DBObject> objects = null;
        if (bucket != null) {
            for (BasePsiElement basePsiElement : bucket) {
                if (basePsiElement instanceof IdentifierPsiElement) {
                    IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) basePsiElement;
                    PsiElement reference = identifierPsiElement.resolve();
                    if (reference instanceof DBObjectPsiElement) {
                        DBObjectPsiElement objectPsiElement = (DBObjectPsiElement) reference;
                        if (objects == null) {
                            objects = new HashSet<DBObject>();
                        }
                        objects.add(objectPsiElement.getObject());
                    }
                }
            }

        }

        return objects;
    }

    public abstract @Nullable BasePsiElement findPsiElement(PsiLookupAdapter lookupAdapter, int scopeCrossCount);
    public abstract @Nullable Set<BasePsiElement> collectPsiElements(PsiLookupAdapter lookupAdapter, @Nullable Set<BasePsiElement> bucket, int scopeCrossCount);

    public abstract void collectExecVariablePsiElements(@NotNull Set<ExecVariablePsiElement> bucket);
    public abstract void collectSubjectPsiElements(@NotNull Set<IdentifierPsiElement> bucket);


    public void collectVirtualObjectPsiElements(Set<BasePsiElement> bucket, DBObjectType objectType) {
        if (elementType.isVirtualObject()) {
            DBObjectType virtualObjectType = elementType.getVirtualObjectType();
            if (objectType == virtualObjectType) {
                bucket.add(this);
            }
        }
    }

    public abstract NamedPsiElement findNamedPsiElement(String id);
    public abstract BasePsiElement findFirstPsiElement(ElementTypeAttribute attribute);
    public abstract BasePsiElement findFirstPsiElement(Class<? extends ElementType> clazz);
    public abstract BasePsiElement findFirstLeafPsiElement();
    public abstract BasePsiElement findPsiElementBySubject(ElementTypeAttribute attribute, CharSequence subjectName, DBObjectType subjectType);
    public abstract BasePsiElement findPsiElementByAttribute(ElementTypeAttribute attribute);


    public boolean containsPsiElement(BasePsiElement basePsiElement) {
        return this == basePsiElement;
    }

    /*********************************************************
     *                       ItemPresentation                *
     *********************************************************/

    @Nullable
    public BasePsiElement findEnclosingPsiElement(ElementTypeAttribute attribute) {
        PsiElement element = this;
        while (element != null && !(element instanceof PsiFile)) {
            if (element instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) element;
                if (basePsiElement.elementType.is(attribute)) {
                    return (BasePsiElement) element;
                }
            }
            element = element.getParent();
        }
        return null;
    }

    @Nullable
    public BasePsiElement findEnclosingVirtualObjectPsiElement(DBObjectType objectType) {
        PsiElement element = this;
        while (element != null && !(element instanceof PsiFile)) {
            if (element instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) element;
                if (basePsiElement.elementType.getVirtualObjectType() == objectType) {
                    return (BasePsiElement) element;
                }
            }
            element = element.getParent();
        }
        return null;
    }

    @Nullable
    public BasePsiElement findEnclosingPsiElement(ElementTypeAttribute[] typeAttributes) {
        PsiElement element = this;
        while (element != null && !(element instanceof PsiFile)) {
            if (element  instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) element;
                for (ElementTypeAttribute typeAttribute : typeAttributes) {
                    if (basePsiElement.elementType.is(typeAttribute)) {
                        return basePsiElement;
                    }
                }
            }
            element = element.getParent();
        }
        return null;
    }


    @Nullable
    public NamedPsiElement findEnclosingNamedPsiElement() {
        PsiElement element = getParent();
        while (element != null && !(element instanceof PsiFile)) {
            if (element instanceof NamedPsiElement) {
                return (NamedPsiElement) element;
            }
            element = element.getParent();
        }
        return null;
    }

    @Nullable
    public SequencePsiElement findEnclosingSequencePsiElement() {
        PsiElement element = getParent();
        while (element != null && !(element instanceof PsiFile)) {
            if (element instanceof SequencePsiElement) {
                return (SequencePsiElement) element;
            }
            element = element.getParent();
        }
        return null;
    }

    public BasePsiElement findEnclosingScopeIsolationPsiElement() {
        PsiElement element = this;
        BasePsiElement basePsiElement = null;
        while (element != null && !(element instanceof PsiFile)) {
            if (element instanceof BasePsiElement) {
                basePsiElement = (BasePsiElement) element;
                if (basePsiElement.isScopeIsolation()) {
                    return basePsiElement;
                }
            }
            element = element.getParent();
        }

        return basePsiElement;
    }

    @Nullable
    public BasePsiElement findEnclosingScopeDemarcationPsiElement() {
        PsiElement element = this;
        BasePsiElement basePsiElement = null;
        while (element != null && !(element instanceof PsiFile)) {
            if (element instanceof BasePsiElement) {
                basePsiElement = (BasePsiElement) element;
                //return elementType.is(ElementTypeAttribute.SCOPE_DEMARCATION);
                if (basePsiElement.isScopeDemarcation()) {
                    return basePsiElement;
                }
            }
            element = element.getParent();
        }

        return basePsiElement;
    }

    @Nullable
    public BasePsiElement getEnclosingScopePsiElement() {
        return enclosingScopePsiElement.get();
    }

    @Nullable
    private BasePsiElement findEnclosingScopePsiElement() {
        PsiElement element = BasePsiElement.this;
        BasePsiElement basePsiElement = null;
        while (element != null && !(element instanceof PsiFile)) {
            if (element instanceof BasePsiElement) {
                basePsiElement = (BasePsiElement) element;
                if (basePsiElement.isScopeBoundary()) {
                    return basePsiElement;
                }
            }
            element = element.getParent();
        }

        return basePsiElement;
    }

    @Nullable
    public <E extends BasePsiElement<?>> E findEnclosingPsiElement(Class<E> psiClass) {
        PsiElement element = getParent();
        while (element != null && !(element instanceof PsiFile)) {
            if (element instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) element;
                if (psiClass.isAssignableFrom(basePsiElement.getClass())) {
                    return (E) element;
                }
            }
            element = element.getParent();
        }
        return null;
    }

    @Nullable
    public NamedPsiElement findEnclosingRootPsiElement() {
        PsiElement element = getParent();
        while (element != null && !(element instanceof PsiFile)) {
            if (element instanceof NamedPsiElement) {
                NamedPsiElement namedPsiElement = (NamedPsiElement) element;
                if (namedPsiElement.elementType.is(ElementTypeAttribute.ROOT)) {
                    return namedPsiElement;
                }
            }
            element = element.getParent();
        }
        return null;
    }
 
    public boolean isParentOf(BasePsiElement basePsiElement) {
        PsiElement element = basePsiElement.getParent();
        while (element != null && !(element instanceof PsiFile)) {
            if (element == this) {
                return true;
            }
            element = element.getParent();
        }
        return false;
    }

    public boolean isMatchingScope(SequencePsiElement sourceScope) {
        return true;
       /* if (sourceScope == null) return true; // no scope constraints
        SequencePsiElement scope = getEnclosingScopePsiElement();
        return scope == sourceScope || scope.isParentOf(sourceScope);*/
    }

    public boolean isScopeDemarcation() {
        return elementType.isScopeDemarcation();
    }

    public boolean isScopeIsolation() {
        return elementType.isScopeIsolation();
    }
    
    public boolean isScopeBoundary() {
        return isScopeDemarcation() || isScopeIsolation();
    }


    /*********************************************************
     *                       ItemPresentation                *
     *********************************************************/
    @Override
    public String getPresentableText() {
        ElementType elementType = getSpecificElementType();
        return elementType.getDescription();
    }

    public ElementType getSpecificElementType() {
        ElementType elementType = this.elementType;
        if (elementType.is(ElementTypeAttribute.GENERIC)) {
            BasePsiElement specificElement = findFirstPsiElement(ElementTypeAttribute.SPECIFIC);
            if (specificElement != null) {
                elementType = specificElement.elementType;
            }
        }
        return elementType;
    }

    public boolean is(ElementTypeAttribute attribute) {
        if (elementType.is(attribute)) {
            return true;
        } else if (attribute.isSpecific()) {
            ElementType specificElementType = getSpecificElementType();
            if (specificElementType != null) {
                return specificElementType.is(attribute);
            }
        }
        return false;
    }

    @Override
    @Nullable
    public String getLocationString() {
        return null;
    }

    @Override
    @Nullable
    public Icon getIcon(boolean open) {
        return getSpecificElementType().getIcon();
    }

    @Nullable
    public TextAttributesKey getTextAttributesKey() {
        return null;
    }

    public abstract boolean hasErrors();

    @Override
    @NotNull
    public DBLanguage getLanguage() {
        return getLanguageDialect().getBaseLanguage();
    }

    @NotNull
    public DBLanguageDialect getLanguageDialect() {
        return elementType.getLanguageDialect();
    }

    public abstract boolean matches(@Nullable BasePsiElement basePsiElement, MatchType matchType);

    public DBObject resolveUnderlyingObject() {
        if (isVirtualObject()) {
            Synchronized.run(this,
                    () -> getCachedUnderlyingObject() == null,
                    () -> {
                        DBObjectType virtualObjectType = elementType.getVirtualObjectType();
                        underlyingObject = new DBVirtualObject(virtualObjectType, this);
                    });
        }
        return underlyingObject;
    }

    public DBVirtualObject getCachedUnderlyingObject() {
        return underlyingObject != null && underlyingObject.isValid() ? underlyingObject : null;
    }

    public QuoteDefinition getIdentifierQuotes() {
        ConnectionHandler activeConnection = getConnectionHandler();
        if (activeConnection != null) {
            return DatabaseCompatibilityInterface.getInstance(activeConnection).getIdentifierQuotes();
        }
        return QuoteDefinition.DEFAULT_IDENTIFIER_QUOTE_DEFINITION;
    }

}
