package com.dci.intellij.dbn.object.common;

import com.dci.intellij.dbn.code.common.lookup.LookupItemBuilder;
import com.dci.intellij.dbn.code.common.lookup.ObjectLookupItemBuilder;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.path.Node;
import com.dci.intellij.dbn.common.path.NodeBase;
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
import com.dci.intellij.dbn.object.common.sorting.DBObjectComparator;
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

import static com.dci.intellij.dbn.common.content.DynamicContentProperty.*;

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

    private volatile boolean loadingChildren;
    private PsiElementRef<BasePsiElement> relevantPsiElement;
    private final DBObjectPsiCache psiCache;
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
        super(psiElement.getConnection(), objectType, psiElement.getText());

        psiCache = new DBObjectPsiCache(psiElement);
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
    public DBObjectPsiCache getPsiCache() {
        return psiCache;
    }

    @NotNull
    public LookupItemBuilder getLookupItemBuilder(DBLanguage language) {
        return lookupItemBuilder.computeIfAbsent(language.getID(), id -> new ObjectLookupItemBuilder(ref(), language));
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
        return getChildObject(name, overload, lookupHidden, new NodeBase<>(this, null));
    }

    @Nullable
    public DBObject getChildObject(String name, short overload, boolean lookupHidden, Node<DBObject> lookupPath) {
        DBObject childObject = super.getChildObject(name, overload, lookupHidden);
        if (childObject == null) {
            BasePsiElement relevantPsiElement = getRelevantPsiElement();
            DBObject underlyingObject = relevantPsiElement.getUnderlyingObject();

            if (underlyingObject != null && !lookupPath.isAncestor(underlyingObject)) {
                lookupPath = new NodeBase<>(underlyingObject, lookupPath);
                return underlyingObject.getChildObject(name, overload, lookupHidden, lookupPath);
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
    public DBObjectList<DBObject> getChildObjectList(DBObjectType objectType) {
        if (!loadingChildren) {
            synchronized (this) {
                if (!loadingChildren) {
                    try {
                        loadingChildren = true;
                        return loadChildObjectList(objectType);
                    } finally {
                        loadingChildren = false;
                    }
                }
            }
        }
        return null;
    }

    private DBObjectList<DBObject> loadChildObjectList(DBObjectType objectType) {
        DBObjectListContainer childObjects = ensureChildObjects();
        DBObjectList<DBObject> objectList = childObjects.getObjectList(objectType);

        if (objectList == null) {
            objectList = childObjects.createObjectList(objectType, this, MUTABLE, VIRTUAL);
            if (objectList != null) {
                loadChildObjects(objectType, objectList);
                objectList.set(LOADED, true);
            }
        } else {
            boolean invalid = objectList.getObjects().stream().anyMatch(o -> !o.isValid());
            if (invalid) {
                objectList.setElements(Collections.emptyList());
                loadChildObjects(objectType, objectList);
                objectList.set(LOADED, true);
            }
        }
        return objectList;
    }

    private void loadChildObjects(DBObjectType objectType, DBObjectList<DBObject> objectList) {
        VirtualObjectLookupAdapter lookupAdapter = new VirtualObjectLookupAdapter(getObjectType(), objectType);
        BasePsiElement underlyingPsiElement = getUnderlyingPsiElement();
        if (underlyingPsiElement != null) {
            List<DBObject> objects = new ArrayList<>();
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
                                        objects.add(column);
                                    }
                                }
                            }
                        } else {
                            DATASET_LOOKUP_ADAPTER.collectInElement(underlyingPsiElement, basePsiElement -> {
                                DBObject object = basePsiElement.getUnderlyingObject();
                                if (object != null && object != this && object.getObjectType().matches(DBObjectType.DATASET)) {
                                    List<DBObject> columns = object.getChildObjects(DBObjectType.COLUMN);
                                    for (DBObject column : columns) {
                                        objects.add(column);
                                    }
                                }
                            });
                        }

                    }
                }

                DBObject object = child.getUnderlyingObject();
                if (object != null && Strings.isNotEmpty(object.getName()) && object.getObjectType().isChildOf(getObjectType()) && !objectList.getAllElements().contains(object)) {
                    if (object instanceof DBVirtualObject) {
                        DBVirtualObject virtualObject = (DBVirtualObject) object;
                        virtualObject.setParentObject(this);
                    }
                    objects.add(object);
                }

            });

            objectList.setElements(objects);
            objectList.sort(DBObjectComparator.basic(objectType));
        }
    }

    @Override
    public String getQualifiedNameWithType() {
        return getName();
    }

    @Override
    @NotNull
    public ConnectionHandler getConnection() {
        BasePsiElement underlyingPsiElement = Failsafe.nd(getUnderlyingPsiElement());
        DBLanguagePsiFile file = underlyingPsiElement.getFile();
        ConnectionHandler connection = file.getConnection();
        if (connection == null) {
            ConnectionManager connectionManager = ConnectionManager.getInstance(getProject());
            return connectionManager.getConnectionBundle().getVirtualConnection(ConnectionId.VIRTUAL_ORACLE);
        }
        return connection;
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
        return (BasePsiElement) getPsiCache().getPsiElement();
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

    @Override
    public int compareTo(@NotNull Object o) {
        DBVirtualObject that = (DBVirtualObject) o;
        int result = this.getObjectType().compareTo(that.getObjectType());
        if (result == 0) {
            return this.getName().compareToIgnoreCase(that.getName());
        }
        return result;
    }
}
