package com.dci.intellij.dbn.object.common;

import com.dci.intellij.dbn.code.common.lookup.LookupItemBuilder;
import com.dci.intellij.dbn.code.common.lookup.ObjectLookupItemBuilder;
import com.dci.intellij.dbn.common.content.DynamicContentStatus;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.thread.Read;
import com.dci.intellij.dbn.common.util.Consumer;
import com.dci.intellij.dbn.common.util.Documents;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.PsiElementRef;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.element.util.IdentifierCategory;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.LeafPsiElement;
import com.dci.intellij.dbn.language.common.psi.QualifiedIdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.TokenPsiElement;
import com.dci.intellij.dbn.language.common.psi.lookup.LookupAdapterCache;
import com.dci.intellij.dbn.language.common.psi.lookup.ObjectLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.ObjectReferenceLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.PsiLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.SimpleObjectLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.VirtualObjectLookupAdapter;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.dci.intellij.dbn.vfs.file.DBContentVirtualFile;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DBVirtualObject extends DBObjectImpl implements PsiReference {
    private static final PsiLookupAdapter CHR_STAR_LOOKUP_ADAPTER = new PsiLookupAdapter() {
        @Override
        public boolean matches(BasePsiElement element) {
            if (element instanceof TokenPsiElement) {
                TokenPsiElement tokenPsiElement = (TokenPsiElement) element;
                return tokenPsiElement.getTokenType() == tokenPsiElement.getElementType().getLanguage().getSharedTokenTypes().getChrStar();
            }
            return false;
        }

        @Override
        public boolean accepts(BasePsiElement element) {
            return true;
        }
    };
    private static final ObjectReferenceLookupAdapter DATASET_LOOKUP_ADAPTER = new ObjectReferenceLookupAdapter(null, DBObjectType.DATASET, null);

    private boolean loadingChildren;
    private PsiElementRef<BasePsiElement> relevantPsiElement;
    private final DBObjectPsiFacade psiFacade;
    private final Map<String, ObjectLookupItemBuilder> lookupItemBuilder = new ConcurrentHashMap<>();

    private final Latent<Boolean> valid = Latent.basic(() -> Read.conditional(() -> {
        BasePsiElement underlyingPsiElement = getUnderlyingPsiElement();
        if (underlyingPsiElement != null && underlyingPsiElement.isValid()) {
            DBObjectType objectType = getObjectType();
            if (objectType == DBObjectType.DATASET) {
                return true;
            }
            BasePsiElement relevantPsiElement = getRelevantPsiElement();
            if (Strings.equalsIgnoreCase(getName(), relevantPsiElement.getText())) {
                if (relevantPsiElement instanceof IdentifierPsiElement) {
                    IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) relevantPsiElement;
                    return identifierPsiElement.getObjectType() == objectType;
                }
                return true;
            }
        }
        return false;
    }, false));

    public DBVirtualObject(@NotNull DBObjectType objectType, @NotNull BasePsiElement psiElement) {
        super(psiElement.getConnectionHandler(), objectType, psiElement.getText());

        psiFacade = new DBObjectPsiFacade(psiElement);
        relevantPsiElement = PsiElementRef.from(psiElement);
        String name = "";

        if (objectType == DBObjectType.COLUMN) {
            PsiLookupAdapter lookupAdapter = LookupAdapterCache.ALIAS_DEFINITION.get(objectType);
            BasePsiElement relevantPsiElement = lookupAdapter.findInElement(psiElement);

            if (relevantPsiElement == null) {
                lookupAdapter = new SimpleObjectLookupAdapter(null, objectType);
                relevantPsiElement = lookupAdapter.findInElement(psiElement);
            }

            if (relevantPsiElement != null) {
                this.relevantPsiElement = PsiElementRef.from(relevantPsiElement);
                name = relevantPsiElement.getText();
            }
        } else if (objectType == DBObjectType.TYPE || objectType == DBObjectType.TYPE_ATTRIBUTE || objectType == DBObjectType.CURSOR) {
            BasePsiElement relevantPsiElement = psiElement.findFirstPsiElement(ElementTypeAttribute.SUBJECT);
            if (relevantPsiElement != null) {
                this.relevantPsiElement = PsiElementRef.from(relevantPsiElement);
                name = relevantPsiElement.getText();
            }
        } else if (objectType == DBObjectType.DATASET) {
            if (psiElement instanceof LeafPsiElement) {
                LeafPsiElement leafPsiElement = (LeafPsiElement) psiElement;
                ObjectLookupAdapter lookupAdapter = new ObjectLookupAdapter(leafPsiElement, IdentifierCategory.REFERENCE, DBObjectType.DATASET);
                BasePsiElement dataset = lookupAdapter.findInParentScopeOf(psiElement);
                if (dataset != null) {
                    this.relevantPsiElement = PsiElementRef.from(dataset);
                    name = dataset.getText();
                } else {
                    name = "UNKNOWN";
                }
            } else {
                List<String> tableNames = new ArrayList<>();

                ObjectLookupAdapter lookupAdapter = new ObjectLookupAdapter(null, IdentifierCategory.REFERENCE, DBObjectType.DATASET);
                lookupAdapter.collectInElement(psiElement, basePsiElement -> {
                    if (basePsiElement instanceof IdentifierPsiElement) {
                        IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) basePsiElement;
                        String tableName = identifierPsiElement.getText().toUpperCase();
                        if (!tableNames.contains(tableName)) {
                            tableNames.add(tableName);
                        }
                    }
                });

                Collections.sort(tableNames);

                StringBuilder nameBuilder = new StringBuilder();
                for (CharSequence tableName : tableNames) {
                    if (nameBuilder.length() > 0) nameBuilder.append(", ");
                    nameBuilder.append(tableName);
                }

                name = "subquery " + nameBuilder;
            }
        }
        objectRef = new DBObjectRef<>(this, name);
    }

    @Override
    protected String initObject(DBObjectMetadata metadata) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public DBObjectPsiFacade getPsiFacade() {
        return psiFacade;
    }

    @NotNull
    public LookupItemBuilder getLookupItemBuilder(DBLanguage language) {
        return lookupItemBuilder.computeIfAbsent(language.getID(), key -> new ObjectLookupItemBuilder(getRef(), language));
    }

    @Override
    public boolean isValid() {
        return valid.get();
    }

    @Override
    public boolean isVirtual() {
        return true;
    }

    @Override
    @NotNull
    public List<DBObject> getChildObjects(DBObjectType objectType) {
        DBObjectList<DBObject> childObjectList = getChildObjectList(objectType);
        return childObjectList == null ? Collections.emptyList() : childObjectList.getObjects();
    }

    @Override
    public DBObject getChildObject(DBObjectType objectType, String name, short overload, boolean lookupHidden) {
        DBObjectList<DBObject> childObjectList = getChildObjectList(objectType);
        return childObjectList == null ? null : childObjectList.getObject(name, overload);
    }

    @Nullable
    @Override
    public DBObject getChildObject(String name, short overload, boolean lookupHidden) {
        DBObject childObject = super.getChildObject(name, overload, lookupHidden);
        if (childObject == null) {
            BasePsiElement relevantPsiElement = getRelevantPsiElement();
            DBObject underlyingObject = relevantPsiElement.getUnderlyingObject();
            if (underlyingObject != null) {
                return underlyingObject.getChildObject(name, overload, lookupHidden);
            }
        }
        return childObject;
    }

    @Override
    public void collectChildObjects(DBObjectType objectType, Consumer consumer) {
        super.collectChildObjects(objectType, consumer);
        BasePsiElement relevantPsiElement = getRelevantPsiElement();
        DBObject underlyingObject = relevantPsiElement.getUnderlyingObject();
        if (underlyingObject != null) {
            underlyingObject.collectChildObjects(objectType, consumer);
        }

    }

    @Override
    @Nullable
    public synchronized DBObjectList<DBObject> getChildObjectList(DBObjectType objectType) {
        if (!loadingChildren) {
            try {
                loadingChildren = true;
                return loadChildObjectList(objectType);
            } finally {
                loadingChildren = false;
            }
        }
        return null;
    }

    private DBObjectList<DBObject> loadChildObjectList(DBObjectType objectType) {
        DBObjectListContainer childObjects = initChildObjects();
        DBObjectList<DBObject> objectList = childObjects.getObjects(objectType);
        if (objectList != null) {
            for (DBObject object : objectList.getObjects()) {
                if (!object.isValid()) {
                    objectList = null;
                    break;
                }
            }
        }

        if (objectList == null) {
            objectList = childObjects.createObjectList(objectType, this, DynamicContentStatus.MUTABLE);
            if (objectList != null) {
                // mark as loaded so default loading mechanism does not kick in.
                objectList.set(DynamicContentStatus.LOADED, true);
            }
        }

        if (objectList != null && objectList.isEmpty()) {
            loadChildObjects(objectType, objectList);
            objectList.set(DynamicContentStatus.LOADED, true);
        }
        return objectList;
    }

    private void loadChildObjects(DBObjectType objectType, DBObjectList<DBObject> objectList) {
        VirtualObjectLookupAdapter lookupAdapter = new VirtualObjectLookupAdapter(getObjectType(), objectType);
        BasePsiElement underlyingPsiElement = getUnderlyingPsiElement();
        if (underlyingPsiElement != null) {
            underlyingPsiElement.collectPsiElements(lookupAdapter, 100, element -> {
                BasePsiElement child = (BasePsiElement) element;
                // handle STAR column
                if (objectType == DBObjectType.COLUMN) {
                    LeafPsiElement starPsiElement = (LeafPsiElement) CHR_STAR_LOOKUP_ADAPTER.findInElement(child);
                    if (starPsiElement != null) {
                        if (starPsiElement.getParent() instanceof QualifiedIdentifierPsiElement) {
                            QualifiedIdentifierPsiElement qualifiedIdentifierPsiElement = (QualifiedIdentifierPsiElement) starPsiElement.getParent();
                            int index = qualifiedIdentifierPsiElement.getIndexOf(starPsiElement);
                            if (index > 0) {
                                IdentifierPsiElement parentPsiElement = qualifiedIdentifierPsiElement.getLeafAtIndex(index - 1);
                                DBObject object = parentPsiElement.getUnderlyingObject();
                                if (object != null && object.getObjectType().matches(DBObjectType.DATASET)) {
                                    List<DBObject> columns = object.getChildObjects(DBObjectType.COLUMN);
                                    for (DBObject column : columns) {
                                        objectList.addObject(column);
                                    }
                                }
                            }
                        } else {
                            DATASET_LOOKUP_ADAPTER.collectInElement(underlyingPsiElement, basePsiElement -> {
                                DBObject object = basePsiElement.getUnderlyingObject();
                                if (object != null && object != this && object.getObjectType().matches(DBObjectType.DATASET)) {
                                    List<DBObject> columns = object.getChildObjects(DBObjectType.COLUMN);
                                    for (DBObject column : columns) {
                                        objectList.addObject(column);
                                    }
                                }
                            });
                        }

                    }
                }

                DBObject object = child.getUnderlyingObject();
                if (object != null && object.getObjectType().isChildOf(getObjectType()) && !objectList.getAllElements().contains(object)) {
                    if (object instanceof DBVirtualObject) {
                        DBVirtualObject virtualObject = (DBVirtualObject) object;
                        virtualObject.setParentObject(this);
                    }
                    objectList.addObject(object);
                }

            });
        }
    }

    @Override
    public String getQualifiedNameWithType() {
        return getName();
    }

    @Override
    @NotNull
    public ConnectionHandler getConnectionHandler() {
        BasePsiElement underlyingPsiElement = Failsafe.nd(getUnderlyingPsiElement());
        DBLanguagePsiFile file = underlyingPsiElement.getFile();
        ConnectionHandler connectionHandler = file.getConnectionHandler();
        if (connectionHandler == null) {
            ConnectionManager connectionManager = ConnectionManager.getInstance(getProject());
            return connectionManager.getConnectionBundle().getVirtualConnection(ConnectionId.VIRTUAL_ORACLE_CONNECTION);
        }
        return connectionHandler;
    }

    public void setParentObject(DBVirtualObject virtualObject) {
        parentObjectRef = DBObjectRef.of(virtualObject);
    }

    @Override
    @NotNull
    public Project getProject() {
        PsiElement underlyingPsiElement = Failsafe.nn(getUnderlyingPsiElement());
        return underlyingPsiElement.getProject();
    }

    @NotNull
    @Override
    public DBObjectType getObjectType() {
        return objectRef.getObjectType();
    }

    @Override
    public void navigate(boolean requestFocus) {
        PsiFile containingFile = getContainingFile();
        if (containingFile != null) {
            VirtualFile virtualFile = containingFile.getVirtualFile();
            BasePsiElement relevantPsiElement = getRelevantPsiElement();
            if(virtualFile instanceof DBContentVirtualFile) {
                Document document = Documents.getDocument(containingFile);
                if (document != null) {
                    Editor[] editors =  EditorFactory.getInstance().getEditors(document);
                    OpenFileDescriptor descriptor = (OpenFileDescriptor) EditSourceUtil.getDescriptor(relevantPsiElement);
                    if (descriptor != null) {
                        descriptor.navigateIn(editors[0]);
                    }
                }

            } else{
                relevantPsiElement.navigate(requestFocus);
            }
        }
    }
    
    @Override
    public PsiFile getContainingFile() throws PsiInvalidElementAccessException {
        return Read.call(() -> {
            BasePsiElement relevantPsiElement = getRelevantPsiElement();
            return relevantPsiElement.isValid() ? relevantPsiElement.getContainingFile() : null;
        });
    }

    /*********************************************************
     *                       PsiReference                    *
     *********************************************************/
    @NotNull
    @Override
    public PsiElement getElement() {
        return getRelevantPsiElement();
    }

    @NotNull
    @Override
    public TextRange getRangeInElement() {
        return new TextRange(0, getName().length());
    }

    @Override
    @Nullable
    public PsiElement resolve() {
        return getUnderlyingPsiElement();
    }

    @Nullable
    public BasePsiElement getUnderlyingPsiElement() {
        return (BasePsiElement) getPsiFacade().getPsiElement();
    }

    @NotNull
    private BasePsiElement getRelevantPsiElement() {
        BasePsiElement basePsiElement = PsiElementRef.get(relevantPsiElement);
        return Failsafe.nn(basePsiElement);
    }

    @Override
    @NotNull
    public String getCanonicalText() {
        return getName();
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
        return null;
    }

    @Override
    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
        return null;
    }

    @Override
    public boolean isReferenceTo(@NotNull PsiElement element) {
        return getUnderlyingPsiElement() == element;
    }

    @Override
    @NotNull
    public Object[] getVariants() {
        return new Object[0];
    }

    @Override
    public boolean isSoft() {
        return false;
    }

    @Override
    public boolean canNavigateToSource() {
        return false;
    }
}
