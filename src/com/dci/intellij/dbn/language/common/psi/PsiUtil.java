package com.dci.intellij.dbn.language.common.psi;

import com.dci.intellij.dbn.common.thread.Read;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.lookup.IdentifierLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.ObjectLookupAdapter;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiWhiteSpace;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Set;

public class PsiUtil {

    public static DBSchema getDatabaseSchema(PsiElement psiElement) {
        DBSchema currentSchema = null;
        if (psiElement instanceof BasePsiElement) {
            BasePsiElement basePsiElement = (BasePsiElement) psiElement;
            currentSchema = basePsiElement.getDatabaseSchema();
        }
        if (currentSchema == null) {
            VirtualFile virtualFile = getVirtualFileForElement(psiElement);
            if (virtualFile != null) {
                FileConnectionMappingManager mappingManager = FileConnectionMappingManager.getInstance(psiElement.getProject());
                SchemaId schemaId = mappingManager.getDatabaseSchema(virtualFile);
                ConnectionHandler connectionHandler = mappingManager.getConnectionHandler(virtualFile);
                if (schemaId != null && connectionHandler != null) {
                    currentSchema = connectionHandler.getSchema(schemaId);
                }

            }
        }
        return currentSchema;
    }

    @Nullable
    public static VirtualFile getVirtualFileForElement(@NotNull PsiElement psiElement) {
        PsiFile psiFile = null;
        try {
            psiFile = psiElement.getContainingFile().getOriginalFile();
        } catch (PsiInvalidElementAccessException e) {
            System.out.println("");
        }
        return psiFile == null ? null : psiFile.getVirtualFile();
    }

    @Nullable
    public static BasePsiElement resolveAliasedEntityElement(IdentifierPsiElement aliasElement) {
        PsiElement psiElement = aliasElement.isReference() ? aliasElement.resolve() : aliasElement; 
        if (psiElement instanceof BasePsiElement) {
            BasePsiElement basePsiElement = (BasePsiElement) psiElement;
            BasePsiElement scope = basePsiElement.findEnclosingNamedPsiElement();

            DBObjectType objectType = aliasElement.getObjectType();
            BasePsiElement objectPsiElement = null;
            if (scope != null) {
                IdentifierLookupAdapter lookupInput = new IdentifierLookupAdapter(aliasElement, null, null, objectType, null);

                objectPsiElement = lookupInput.findInScope(scope);
                if (objectPsiElement == null) {
                    scope = scope.findEnclosingSequencePsiElement();
                    if (scope != null) {
                        objectPsiElement = lookupInput.findInScope(scope);
                    }
                }
            }

            if (objectPsiElement != null) {
                Set<BasePsiElement> virtualObjectPsiElements = new THashSet<BasePsiElement>();
                scope.collectVirtualObjectPsiElements(virtualObjectPsiElements, objectType);
                for (BasePsiElement virtualObjectPsiElement : virtualObjectPsiElements) {
                    if (virtualObjectPsiElement.containsPsiElement(objectPsiElement))
                        return virtualObjectPsiElement;

                }
            }

            return objectPsiElement;

        }
        return null;
    }

    @Nullable
    public static IdentifierPsiElement lookupObjectPriorTo(@NotNull BasePsiElement element, DBObjectType objectType) {
        SequencePsiElement scope = element.findEnclosingSequencePsiElement();

        if (scope != null) {
            Iterator<PsiElement> children = PsiUtil.getChildrenIterator(scope);
            while (children.hasNext()) {
                PsiElement child = children.next();
                if (child instanceof BasePsiElement) {
                    BasePsiElement basePsiElement = (BasePsiElement) child;
                    ObjectLookupAdapter lookupInput = new ObjectLookupAdapter(null, objectType);
                    BasePsiElement objectPsiElement = lookupInput.findInScope(basePsiElement);
                    if (objectPsiElement instanceof IdentifierPsiElement) {
                        return (IdentifierPsiElement) objectPsiElement;
                    }
                }
                if (child == element) break;
            }
        }
        return null;
    }

    @Nullable
    public static ExecutablePsiElement lookupExecutableAtCaret(@NotNull Editor editor, boolean lenient) {
        // GTK: PsiElement psiElement = PsiFile.findElementA(offset)

        int offset = editor.getCaretModel().getOffset();

        PsiFile file = DocumentUtil.getFile(editor);
        if (file != null) {
            PsiElement current;

            if (lenient) {
                int lineStart = editor.getCaretModel().getVisualLineStart();
                int lineEnd = editor.getCaretModel().getVisualLineEnd();
                current = file.findElementAt(lineStart);
                while (ignore(current)) {
                    offset = current.getTextOffset() + current.getTextLength();
                    if (offset >= lineEnd) break;
                    current = file.findElementAt(offset);
                }
            } else {
                current = file.findElementAt(offset);
            }

            if (current != null) {
                PsiElement parent = current.getParent();
                while (parent != null && !(parent instanceof PsiFile)) {
                    if (parent instanceof ExecutablePsiElement){
                        ExecutablePsiElement executable = (ExecutablePsiElement) parent;
                        if (!executable.isNestedExecutable()) {
                            return executable;
                        }

                    }
                    parent = parent.getParent();
                }
            }
        }

        return null;
    }

    @Nullable
    public static BasePsiElement lookupElementAtOffset(@NotNull PsiFile file, ElementTypeAttribute typeAttribute, int offset) {
        PsiElement element = file.findElementAt(offset);
        while (element != null && !(element instanceof PsiFile)) {
            if (element instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) element;
                if (basePsiElement.getElementType().is(typeAttribute)) {
                    return basePsiElement;
                }
            }
            element = element.getParent();
        }
        return null;
    }

    @Nullable
    public static LeafPsiElement lookupLeafBeforeOffset(PsiFile file, int originalOffset) {
        int offset = originalOffset;
        if (offset > 0 && offset == file.getTextLength()) {
            offset--;
        }
        PsiElement element = file.findElementAt(offset);
        while (element != null && offset >= 0) {
            int elementEndOffset = element.getTextOffset() + element.getTextLength();
            PsiElement parent = element.getParent();
            if (elementEndOffset <= originalOffset && parent instanceof LeafPsiElement) {
                LeafPsiElement leafPsiElement = (LeafPsiElement) parent;
                if (leafPsiElement instanceof IdentifierPsiElement) {
                    if (elementEndOffset < originalOffset) {
                        return leafPsiElement;
                    }
                } else {
                    return (LeafPsiElement) parent;
                }
            }
            offset = element.getTextOffset() - 1;
            element = file.findElementAt(offset);
        }
        return null;
    }

    private static boolean ignore(PsiElement element) {
        return element instanceof PsiWhiteSpace || element instanceof PsiComment;
    }


    @Nullable
    public static LeafPsiElement lookupLeafAtOffset(@NotNull PsiFile file, int originalOffset) {
        int offset = originalOffset;
        PsiElement element = file.findElementAt(offset);
        while (element != null && offset >= 0) {
            int elementEndOffset = element.getTextOffset() + element.getTextLength();
            if (element.getParent() instanceof LeafPsiElement) {
                LeafPsiElement leafPsiElement = (LeafPsiElement) element.getParent();
                if (leafPsiElement instanceof IdentifierPsiElement) {
                    if (elementEndOffset < originalOffset) {
                        return leafPsiElement;
                    }
                } else {
                    return (LeafPsiElement) element.getParent();
                }
            }
            offset = element.getTextOffset() - 1;
            element = file.findElementAt(offset);
        }
        return null;
    }

    public static void moveCaretOutsideExecutable(Editor editor) {
        ExecutablePsiElement executablePsiElement = lookupExecutableAtCaret(editor, false);
        if (executablePsiElement != null) {
            int offset = executablePsiElement.getTextOffset();
            editor.getCaretModel().moveToOffset(offset);
        }
    }

    private static Iterator<PsiElement> getChildrenIterator(@NotNull PsiElement element) {
        return new Iterator<PsiElement>() {
            private PsiElement current = element.getFirstChild();
            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public PsiElement next() {
                PsiElement oldCurrent = current;
                current = current.getNextSibling();
                return oldCurrent;
            }

            @Override
            public void remove() {

            }
        };
    }

    public static int getChildCount(@NotNull PsiElement element) {
        int count = 0;
        PsiElement current = element.getFirstChild();
        while (current != null) {
            count ++ ;
            current = current.getNextSibling();
        }
        return count;
    }

    public static PsiElement getNextSibling(@NotNull PsiElement psiElement) {
        PsiElement nextPsiElement = psiElement.getNextSibling();
        while (ignore(nextPsiElement)) {
            nextPsiElement = nextPsiElement.getNextSibling();
        }
        return nextPsiElement;
    }

    @Nullable
    public static PsiElement getFirstLeaf(@NotNull PsiElement psiElement) {
        PsiElement childPsiElement = psiElement.getFirstChild();
        if (childPsiElement == null) {
            return psiElement;
        } else if (ignore(childPsiElement)) {
            return getNextLeaf(childPsiElement);
        }
        return getFirstLeaf(childPsiElement);
    }

    @Nullable
    public static PsiElement getNextLeaf(@Nullable PsiElement psiElement) {
        if (psiElement == null) {
            return null;
        } else {
            PsiElement nextElement = psiElement.getNextSibling();
            if (nextElement == null) {
                return getNextLeaf(psiElement.getParent());
            }
            else if (ignore(nextElement)) {
                return getNextLeaf(nextElement);
            }
            return getFirstLeaf(nextElement);
        }
    }

    @Nullable
    public static PsiFile getPsiFile(Project project, Document document) {
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        return psiDocumentManager == null ? null : psiDocumentManager.getPsiFile(document);
    }

    @Nullable
    public static PsiFile getPsiFile(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        return Read.call(() -> {
            if (virtualFile.isValid()) {
                PsiManager psiManager = PsiManager.getInstance(project);
                return psiManager.findFile(virtualFile);
            } else {
                return null;
            }
        });
    }


    @Nullable
    public static BasePsiElement getBasePsiElement(@Nullable PsiElement element) {
        while (element != null && !(element instanceof PsiFile)) {
            if (element instanceof BasePsiElement) {
                return (BasePsiElement) element;
            }
            element = element.getParent();
        }

        return null;
    }

    @Nullable
    public static ElementType getElementType(PsiElement psiElement) {
        if (psiElement instanceof BasePsiElement) {
            BasePsiElement basePsiElement = (BasePsiElement) psiElement;
            return basePsiElement.getElementType();
        }
        return null;
    }

    public static Language getLanguage(@NotNull PsiElement element) {
        Language language = element.getLanguage();
        if (language instanceof DBLanguageDialect) {
            DBLanguageDialect languageDialect = (DBLanguageDialect) language;
            language = languageDialect.getBaseLanguage();
        }
        return language;
    }
}
