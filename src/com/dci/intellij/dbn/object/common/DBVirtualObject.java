package com.dci.intellij.dbn.object.common;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoadException;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.lookup.AliasDefinitionLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.PsiLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.SimpleObjectLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.VirtualObjectLookupAdapter;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.DBContentVirtualFile;
import com.intellij.ide.util.EditSourceUtil;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;

public class DBVirtualObject extends DBObjectImpl implements PsiReference {
    private DBObjectType objectType;
    private BasePsiElement underlyingPsiElement;
    private BasePsiElement relevantPsiElement;

    public DBVirtualObject(DBObjectType objectType, BasePsiElement psiElement) {
        super(psiElement.getActiveConnection() == null ? null :
                psiElement.getActiveConnection().getObjectBundle(), psiElement.getText());

        underlyingPsiElement = psiElement;
        relevantPsiElement = psiElement;
        this.objectType = objectType;

        if (objectType == DBObjectType.COLUMN) {
            PsiLookupAdapter lookupAdapter = new AliasDefinitionLookupAdapter(null, objectType);
            BasePsiElement relevantPsiElement = lookupAdapter.findInElement(psiElement);

            if (relevantPsiElement == null) {
                lookupAdapter = new SimpleObjectLookupAdapter(null, objectType);
                relevantPsiElement = lookupAdapter.findInElement(psiElement);
            }

            if (relevantPsiElement != null) {
                this.relevantPsiElement = relevantPsiElement;
                this.name = relevantPsiElement.getText();
            }
        } else if (objectType == DBObjectType.TYPE || objectType == DBObjectType.TYPE_ATTRIBUTE) {
            BasePsiElement relevantPsiElement = psiElement.lookupFirstPsiElement(ElementTypeAttribute.SUBJECT);
            if (relevantPsiElement != null) {
                this.relevantPsiElement = relevantPsiElement;
                this.name = relevantPsiElement.getText();
            }
        }
        objectRef = new DBObjectRef(this);
    }

    @Override
    protected void initObject(ResultSet resultSet) throws SQLException {
    }

    public boolean isValid() {
        if (name.equalsIgnoreCase(relevantPsiElement.getText())) {
            if (relevantPsiElement instanceof IdentifierPsiElement) {
                IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) relevantPsiElement;
                if (identifierPsiElement.getObjectType() != getObjectType()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @NotNull
    public List<DBObject> getChildObjects(DBObjectType objectType) {
        return getChildObjectList(objectType).getObjects();
    }

    public DBObject getChildObject(DBObjectType objectType, String name, boolean lookupHidden) {
        return getChildObjectList(objectType).getObject(name);
    }

    public DBObjectList<DBObject> getChildObjectList(DBObjectType objectType) {
        DBObjectListContainer childObjects = initChildObjects();
        DBObjectList<DBObject> objectList = childObjects.getObjectList(objectType);
        if (objectList != null) {
            for (DBObject object : objectList.getObjects()) {
                if (!object.isValid()) {
                    objectList = null;
                    break;
                }
            }
        }

        if (objectList == null) {
            objectList = childObjects.createObjectList(objectType, this, VOID_CONTENT_LOADER, false, false);
        }
        if (objectList.size() == 0) {
            VirtualObjectLookupAdapter lookupAdapter = new VirtualObjectLookupAdapter(null, this.objectType, objectType);
            Set<BasePsiElement> children = underlyingPsiElement.collectPsiElements(lookupAdapter, null, 100);
            if (children != null) {
                for (BasePsiElement child : children) {
                    DBObject object = child.resolveUnderlyingObject();
                    if (object != null && !objectList.getElements().contains(object)) {
                        objectList.addObject(object);
                    }
                }
            }
        }
        return objectList;
    }

    public String getQualifiedNameWithType() {
        return getName();
    }

    public ConnectionHandler getConnectionHandler() {
        DBLanguagePsiFile file = underlyingPsiElement.getFile();
        return file == null ? null : file.getActiveConnection();
    }

    @NotNull
    public Project getProject() {
        return underlyingPsiElement.getProject();
    }

    public DBObjectType getObjectType() {
        return objectType;
    }

    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return EMPTY_TREE_NODE_LIST;
    }

    public void navigate(boolean requestFocus) {
        PsiFile containingFile = getContainingFile();
        if (containingFile != null) {
            VirtualFile virtualFile = containingFile.getVirtualFile();
            if(virtualFile instanceof DBContentVirtualFile) {
                Document document = DocumentUtil.getDocument(containingFile);
                Editor[] editors =  EditorFactory.getInstance().getEditors(document);
                OpenFileDescriptor descriptor = (OpenFileDescriptor) EditSourceUtil.getDescriptor(relevantPsiElement);
                if (descriptor != null) descriptor.navigateIn(editors[0]);

            } else{
                relevantPsiElement.navigate(requestFocus);
            }
        }
    }
    
    public PsiFile getContainingFile() throws PsiInvalidElementAccessException {
        return relevantPsiElement.isValid() ? relevantPsiElement.getContainingFile() : null;
    }

    private static final DynamicContentLoader VOID_CONTENT_LOADER = new DynamicContentLoader() {
        public void loadContent(DynamicContent dynamicContent, boolean forceReload) throws DynamicContentLoadException {}
        public void reloadContent(DynamicContent dynamicContent) throws DynamicContentLoadException {}
    };

    /*********************************************************
     *                       PsiReference                    *
     *********************************************************/
    public PsiElement getElement() {
        return null;
    }

    public TextRange getRangeInElement() {
        return new TextRange(0, getTextLength());
    }

    public PsiElement resolve() {
        return underlyingPsiElement;
    }

    public BasePsiElement getUnderlyingPsiElement() {
        return underlyingPsiElement;
    }

    public BasePsiElement getRelevantPsiElement() {
        return relevantPsiElement;
    }

    @NotNull
    public String getCanonicalText() {
        return null;
    }

    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        return null;
    }

    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
        return null;
    }

    public boolean isReferenceTo(PsiElement element) {
        return underlyingPsiElement == element;
    }

    @NotNull
    public Object[] getVariants() {
        return new Object[0];
    }

    public boolean isSoft() {
        return false;
    }

}
