package com.dci.intellij.dbn.language.common.psi;

import com.dci.intellij.dbn.code.common.style.formatting.FormattingAttributes;
import com.dci.intellij.dbn.code.common.style.formatting.FormattingDefinition;
import com.dci.intellij.dbn.code.common.style.formatting.FormattingProviderPsiElement;
import com.dci.intellij.dbn.common.editor.BasicTextEditor;
import com.dci.intellij.dbn.common.thread.ReadActionRunner;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.editor.ddl.DDLFileEditor;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.DBLanguageFile;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttributesBundle;
import com.dci.intellij.dbn.language.common.element.util.IdentifierCategory;
import com.dci.intellij.dbn.language.common.psi.lookup.ObjectLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.PsiLookupAdapter;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBVirtualObject;
import com.dci.intellij.dbn.vfs.DatabaseEditableObjectFile;
import com.dci.intellij.dbn.vfs.SourceCodeFile;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.ide.util.EditSourceUtil;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
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

import javax.swing.Icon;
import java.util.Set;

public abstract class BasePsiElement extends ASTWrapperPsiElement implements ItemPresentation, FormattingProviderPsiElement {
    private ElementType elementType;
    private DBVirtualObject underlyingObject;
    private boolean isScopeIsolation;
    private boolean isScopeDemarcation;
    private FormattingAttributes formattingAttributes;

    public BasePsiElement(ASTNode astNode, ElementType elementType) {
        super(astNode);
        this.elementType = elementType;
        isScopeIsolation = elementType.is(ElementTypeAttribute.SCOPE_ISOLATION);
        isScopeDemarcation = elementType.is(ElementTypeAttribute.SCOPE_DEMARCATION);
    }

    public FormattingAttributes getFormattingAttributes() {
        FormattingDefinition formattingDefinition = elementType.getFormatting();
        if (formattingAttributes == null && formattingDefinition != null) {
            formattingAttributes = FormattingAttributes.copy(formattingDefinition.getAttributes());
        }

        return formattingAttributes;
    }

    public ElementTypeAttributesBundle getElementTypeAttributes() {
        return elementType.getAttributes();
    }

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
        CharSequence charSequence = getNode().getChars();
        for (int i=0; i<charSequence.length(); i++) {
            if (charSequence.charAt(i) == '\n') return true;
        }
        return false;
    }

    public void setElementType(ElementType elementType) {
        this.elementType = elementType;
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
        if (originalFile == null || originalFile == containingFile) {
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
                boolean isSameElement = basePsiElement.getElementType() == getElementType();
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
        return originalFile == null || originalFile == containingFile;

    }

    public String getReferenceQualifiedName() {
        return isVirtualObject() ? "virtual " + getElementType().getVirtualObjectType().getName() : "[unknown element]";
    }

    public abstract int approximateLength();

    public ElementType getElementType() {
        return elementType;
    }

    public DBLanguageFile getFile() {
        PsiElement parent = getParent();
        while (parent != null && !(parent instanceof DBLanguageFile)) {
            parent = parent.getParent();
        }
        return (DBLanguageFile) parent;
    }

    @Nullable
    public ConnectionHandler getActiveConnection() {
        DBLanguageFile file = getFile();
        return file == null ? null : file.getActiveConnection();
    }

    public DBSchema getCurrentSchema() {
        DBLanguageFile file = getFile();
        return file == null ? null : file.getCurrentSchema();
    }

    public String toString() {
        return hasErrors() ?
                "[INVALID] " + elementType.getDebugName() :
                elementType.getDebugName() +
                        (isScopeDemarcation() ? " SCOPE_DEMARCATION" : "") +
                        (isScopeIsolation() ? " SCOPE_ISOLATION" : "");
    }

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

    public void accept(@NotNull PsiElementVisitor visitor) {
        super.accept(visitor);
    }

    public String getText() {
        if (ApplicationManager.getApplication().isReadAccessAllowed()) {
            return super.getText();
        }
        return new ReadActionRunner<String>() {
            protected String run() {
                return BasePsiElement.super.getText();
            }
        }.start();
    }


    public PsiElement findElementAt(int offset) {
        return super.findElementAt(offset);
    }

    public PsiElement getLastChildIgnoreWhiteSpace() {
        PsiElement psiElement = this.getLastChild();
        while (psiElement != null && psiElement instanceof PsiWhiteSpace) {
            psiElement = psiElement.getPrevSibling();
        }
        return psiElement;
    }

    public BasePsiElement getPrevElement() {
        PsiElement preElement = getPrevSibling();
        while (preElement != null && preElement instanceof PsiWhiteSpace) {
            preElement = preElement.getPrevSibling();
        }
        BasePsiElement previous = (BasePsiElement) preElement;
        while (previous != null && previous.getLastChild() instanceof BasePsiElement) {
            previous = (BasePsiElement) previous.getLastChild();
        }
        return previous;
    }

    protected BasePsiElement getNextElement() {
        PsiElement nextElement = getNextSibling();
        while (nextElement != null && (nextElement instanceof PsiWhiteSpace || nextElement instanceof PsiComment || nextElement instanceof PsiErrorElement)) {
            nextElement = nextElement.getNextSibling();
        }
        BasePsiElement next = (BasePsiElement) nextElement;
        while (next != null && next.getFirstChild() instanceof BasePsiElement) {
            next = (BasePsiElement) next.getFirstChild();
        }
        return next;
    }

    public boolean isVirtualObject() {
        return getElementType().isVirtualObject();
    }

    public void navigate(boolean requestFocus) {
        if (isValid()) {
            OpenFileDescriptor descriptor = (OpenFileDescriptor) EditSourceUtil.getDescriptor(this);
            if (descriptor != null) {
                VirtualFile virtualFile = getFile().getVirtualFile();
                FileEditorManager editorManager = FileEditorManager.getInstance(getProject());
                if (virtualFile != null) {
                    if (virtualFile instanceof SourceCodeFile) {
                        SourceCodeFile sourceCodeFile = (SourceCodeFile) virtualFile;
                        DatabaseEditableObjectFile databaseFile = sourceCodeFile.getDatabaseFile();
                        if (!editorManager.isFileOpen(databaseFile)) {
                            editorManager.openFile(databaseFile, requestFocus);
                        }
                        BasicTextEditor textEditor = EditorUtil.getFileEditor(databaseFile, virtualFile);
                        descriptor.navigateIn(textEditor.getEditor());
                        return;
                    }

                    Editor editor = editorManager.getSelectedTextEditor();
                    if (editor != null && virtualFile == DocumentUtil.getVirtualFile(editor)) {
                        super.navigate(requestFocus);
                        return;
                    }

                    FileEditor[] fileEditors = editorManager.getSelectedEditors();
                    for (FileEditor fileEditor : fileEditors) {
                        if (fileEditor instanceof DDLFileEditor) {
                            DDLFileEditor textEditor = (DDLFileEditor) fileEditor;
                            if (textEditor.getVirtualFile() == virtualFile) {
                                descriptor.navigateIn(textEditor.getEditor());
                                return;
                            }

                        }
                    }

                    super.navigate(requestFocus);
                }
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

    public abstract BasePsiElement lookupPsiElement(PsiLookupAdapter lookupAdapter, int scopeCrossCount);
    public abstract Set<BasePsiElement> collectPsiElements(PsiLookupAdapter lookupAdapter, Set<BasePsiElement> bucket, int scopeCrossCount);

    public abstract void collectExecVariablePsiElements(Set<ExecVariablePsiElement> bucket);
    public abstract void collectSubjectPsiElements(Set<BasePsiElement> bucket);


    public void collectVirtualObjectPsiElements(Set<BasePsiElement> bucket, DBObjectType objectType) {
        if (getElementType().isVirtualObject()) {
            DBObjectType virtualObjectType = getElementType().getVirtualObjectType();
            if (objectType == virtualObjectType) {
                bucket.add(this);
            }
        }
    }

    public abstract NamedPsiElement lookupNamedPsiElement(String id);
    public abstract BasePsiElement lookupFirstPsiElement(ElementTypeAttribute attribute);
    public abstract BasePsiElement lookupFirstLeafPsiElement();
    public abstract BasePsiElement lookupPsiElementBySubject(ElementTypeAttribute attribute, CharSequence subjectName, DBObjectType subjectType);
    public abstract BasePsiElement lookupPsiElementByAttribute(ElementTypeAttribute attribute);


    public boolean containsPsiElement(BasePsiElement basePsiElement) {
        return this == basePsiElement;
    }

    /*********************************************************
     *                       ItemPresentation                *
     *********************************************************/

    public BasePsiElement lookupEnclosingPsiElement(ElementTypeAttribute attribute) {
        PsiElement element = this;
        while (element != null) {
            if (element instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) element;
                if (basePsiElement.getElementType().is(attribute)) {
                    return (BasePsiElement) element;
                }
            }
            element = element.getParent();
        }
        return null;
    }

    public BasePsiElement lookupEnclosingPsiElement(ElementTypeAttribute[] typeAttributes) {
        PsiElement element = this;
        while (element != null) {
            if (element  instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) element;
                for (ElementTypeAttribute typeAttribute : typeAttributes) {
                    if (basePsiElement.getElementType().is(typeAttribute)) {
                        return basePsiElement;
                    }
                }
            }
            element = element.getParent();
        }
        return null;
    }


    public NamedPsiElement lookupEnclosingNamedPsiElement() {
        PsiElement parent = getParent();
        while (parent != null) {
            if (parent instanceof NamedPsiElement) {
                return (NamedPsiElement) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    public SequencePsiElement lookupEnclosingSequencePsiElement() {
        PsiElement parent = getParent();
        while (parent != null) {
            if (parent instanceof SequencePsiElement) {
                return (SequencePsiElement) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    BasePsiElement enclosingScopePsiElement;

    public BasePsiElement getEnclosingScopePsiElement() {
        if (enclosingScopePsiElement == null) {
            enclosingScopePsiElement = lookupEnclosingScopePsiElement();
        }

        return enclosingScopePsiElement;
    }

    public BasePsiElement lookupEnclosingScopeIsolationPsiElement() {
        PsiElement element = this;
        BasePsiElement basePsiElement = null;
        while (element != null) {
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

    public BasePsiElement lookupEnclosingScopeDemarcationPsiElement() {
        PsiElement element = this;
        BasePsiElement basePsiElement = null;
        while (element != null) {
            if (element instanceof BasePsiElement) {
                basePsiElement = (BasePsiElement) element;
                if (basePsiElement.isScopeDemarcation()) {
                    return basePsiElement;
                }
            }
            element = element.getParent();
        }

        return basePsiElement;
    }

    /**
     * @deprecated use scope demarcation and isolation
     */
    public BasePsiElement lookupEnclosingScopePsiElement() {
        PsiElement element = this;
        BasePsiElement basePsiElement = null;
        while (element != null) {
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



    public BasePsiElement lookupEnclosingPsiElement(Class type) {
        PsiElement parent = getParent();
        while (parent != null) {
            if (type.isAssignableFrom(parent.getClass())) {
                return (BasePsiElement) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    public NamedPsiElement lookupEnclosingRootPsiElement() {
        PsiElement parent = getParent();
        while (parent != null) {
            if (parent instanceof NamedPsiElement) {
                NamedPsiElement namedPsiElement = (NamedPsiElement) parent;
                if (namedPsiElement.getElementType().is(ElementTypeAttribute.ROOT)) {
                    return namedPsiElement;
                }
            }
            parent = parent.getParent();
        }
        return null;
    }
 
    public boolean isParentOf(BasePsiElement basePsiElement) {
        PsiElement parent = basePsiElement.getParent();
        while (parent != null) {
            if (parent == this) {
                return true;
            }
            parent = parent.getParent();
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
        return isScopeDemarcation;
        //return elementType.is(ElementTypeAttribute.SCOPE_DEMARCATION);
    }

    public boolean isScopeIsolation() {
        return isScopeIsolation;
    }
    
    public boolean isScopeBoundary() {
        return isScopeDemarcation() || isScopeIsolation();
    }


    /*********************************************************
     *                       ItemPresentation                *
     *********************************************************/
    public String getPresentableText() {
        return elementType.getDescription();
    }

    @Nullable
    public String getLocationString() {
        return null;
    }

    @Nullable
    public Icon getIcon(boolean open) {
        return getElementType().getIcon();
    }

    @Nullable
    public TextAttributesKey getTextAttributesKey() {
        return null;
    }

    public abstract boolean hasErrors();

    @NotNull
    public DBLanguage getLanguage() {
        return getLanguageDialect().getBaseLanguage();
    }

    @NotNull
    public DBLanguageDialect getLanguageDialect() {
        return getElementType().getLanguageDialect();
    }

    public abstract boolean equals(BasePsiElement basePsiElement);

    public abstract boolean matches(BasePsiElement basePsiElement);

    public synchronized DBObject resolveUnderlyingObject() {
        if (isVirtualObject() && (underlyingObject == null || !underlyingObject.isValid()) ) {
            DBObjectType virtualObjectType = getElementType().getVirtualObjectType();
            underlyingObject = new DBVirtualObject(virtualObjectType, this);
        }
        return underlyingObject;
    }

    public char getIdentifierQuotesChar() {
        ConnectionHandler activeConnection = getActiveConnection();
        if (activeConnection != null) {
            return DatabaseCompatibilityInterface.getInstance(activeConnection).getIdentifierQuotes();
        }
        return '"';
    }

}
