package com.dci.intellij.dbn.debugger.common.frame;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.code.common.style.DBLCodeStyleManager;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseOption;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseSettings;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.LazyValue;
import com.dci.intellij.dbn.common.util.SimpleLazyValue;
import com.dci.intellij.dbn.debugger.DBDebugUtil;
import com.dci.intellij.dbn.debugger.common.evaluation.DBDebuggerEvaluator;
import com.dci.intellij.dbn.debugger.common.process.DBDebugProcess;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.language.psql.PSQLLanguage;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DBVirtualFile;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.ui.ColoredTextContainer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.xdebugger.XDebuggerBundle;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XValueChildrenList;
import gnu.trove.THashMap;

public abstract class DBDebugStackFrame<P extends DBDebugProcess, V extends DBDebugValue> extends XStackFrame {
    private P debugProcess;
    private int frameIndex;
    private Map<String, V> valuesMap;

    private LazyValue<XSourcePosition> sourcePosition = new SimpleLazyValue<XSourcePosition>() {
        @Override
        protected XSourcePosition load() {
            return computeSourcePosition();
        }
    };


    private LazyValue<IdentifierPsiElement> subject = new SimpleLazyValue<IdentifierPsiElement>() {
        @Override
        protected IdentifierPsiElement load() {
            Project project = getDebugProcess().getProject();
            XSourcePosition sourcePosition = getSourcePosition();
            VirtualFile virtualFile = getVirtualFile();
            Document document = DocumentUtil.getDocument(virtualFile);
            DBLanguagePsiFile psiFile = (DBLanguagePsiFile) PsiUtil.getPsiFile(project, document);

            if (sourcePosition != null && psiFile != null && document != null) {
                int offset = document.getLineEndOffset(sourcePosition.getLine());
                PsiElement elementAtOffset = psiFile.findElementAt(offset);
                while (elementAtOffset instanceof PsiWhiteSpace || elementAtOffset instanceof PsiComment) {
                    elementAtOffset = elementAtOffset.getNextSibling();
                }

                if (elementAtOffset instanceof BasePsiElement) {
                    BasePsiElement basePsiElement = (BasePsiElement) elementAtOffset;
                    BasePsiElement objectDeclarationPsiElement = basePsiElement.findEnclosingPsiElement(ElementTypeAttribute.OBJECT_DECLARATION);
                    if (objectDeclarationPsiElement != null) {
                        return (IdentifierPsiElement) objectDeclarationPsiElement.findFirstPsiElement(ElementTypeAttribute.SUBJECT);
                    }
                }
            }
            return null;
        }
    };

    public DBDebugStackFrame(P debugProcess, int frameIndex) {
        this.debugProcess = debugProcess;
        this.frameIndex = frameIndex;
    }

    protected abstract XSourcePosition computeSourcePosition();

    @NotNull
    @Override
    public abstract DBDebuggerEvaluator<? extends DBDebugStackFrame, V> getEvaluator();

    @Nullable
    @Override
    public XSourcePosition getSourcePosition() {
        return sourcePosition.get();
    }

    public IdentifierPsiElement getSubject() {
        return subject.get();
    }

    public P getDebugProcess() {
        return debugProcess;
    }

    public int getFrameIndex() {
        return frameIndex;
    }


    public V getValue(String variableName) {
        return valuesMap == null ? null : valuesMap.get(variableName.toLowerCase());
    }

    public void setValue(String variableName, V value) {
        if (valuesMap == null) {
            valuesMap = new THashMap<String, V>();
        }
        valuesMap.put(variableName.toLowerCase(), value);
    }

    protected abstract VirtualFile getVirtualFile();

    @Nullable
    protected abstract V createSuspendReasonDebugValue();

    @NotNull
    public abstract V createDebugValue(String variableName, V parentValue, Set<String> childVariableNames, Icon icon);

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        valuesMap = new THashMap<String, V>();
        List<DBDebugValue> values = new ArrayList<DBDebugValue>();

        V frameInfoValue = createSuspendReasonDebugValue();
        if (frameInfoValue != null) {
            values.add(frameInfoValue);
            valuesMap.put(frameInfoValue.getName(), frameInfoValue);
        }

        XSourcePosition sourcePosition = getSourcePosition();
        VirtualFile virtualFile = DBDebugUtil.getSourceCodeFile(sourcePosition);

        if (virtualFile != null) {
            if (virtualFile instanceof DBEditableObjectVirtualFile) {
                virtualFile = ((DBEditableObjectVirtualFile) virtualFile).getMainContentFile();
            }
            Project project = getDebugProcess().getProject();
            Document document = DocumentUtil.getDocument(virtualFile);
            DBLanguagePsiFile psiFile = (DBLanguagePsiFile) PsiUtil.getPsiFile(project, virtualFile);

            if (document != null && psiFile != null) {
                int offset = document.getLineStartOffset(sourcePosition.getLine());
                Set<BasePsiElement> variables = psiFile.lookupVariableDefinition(offset);
                CodeStyleCaseSettings codeStyleCaseSettings = DBLCodeStyleManager.getInstance(psiFile.getProject()).getCodeStyleCaseSettings(PSQLLanguage.INSTANCE);
                CodeStyleCaseOption objectCaseOption = codeStyleCaseSettings.getObjectCaseOption();

                for (final BasePsiElement basePsiElement : variables) {
                    String variableName = objectCaseOption.format(basePsiElement.getText());
                    //DBObject object = basePsiElement.resolveUnderlyingObject();

                    Set<String> childVariableNames = null;
                    if (basePsiElement instanceof IdentifierPsiElement) {
                        IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) basePsiElement;
                        List<BasePsiElement> qualifiedUsages = identifierPsiElement.findQualifiedUsages();
                        for (BasePsiElement qualifiedUsage : qualifiedUsages) {
                            if (childVariableNames == null) childVariableNames = new HashSet<String>();

                            String childVariableName = objectCaseOption.format(qualifiedUsage.getText());
                            childVariableNames.add(childVariableName);
                        }
                    }

                    if (!valuesMap.containsKey(variableName.toLowerCase())) {
                        Icon icon = basePsiElement.getIcon(true);
                        V value = createDebugValue(variableName, null, childVariableNames, icon);
                        values.add(value);
                        valuesMap.put(variableName.toLowerCase(), value);
                    }
                }
            }
        }

        Collections.sort(values);

        XValueChildrenList children = new XValueChildrenList();
        for (DBDebugValue value : values) {
            children.add(value.getVariableName(), value);
        }
        node.addChildren(children, true);
    }

    public void customizePresentation(@NotNull ColoredTextContainer component) {
        XSourcePosition sourcePosition = getSourcePosition();
        VirtualFile virtualFile = DBDebugUtil.getSourceCodeFile(sourcePosition);

        DBSchemaObject object = DBDebugUtil.getObject(sourcePosition);
        if (object != null) {
            String frameName = object.getName();
            Icon frameIcon = object.getIcon();

            IdentifierPsiElement subject = getSubject();
            if (subject != null) {
                frameName = frameName + "." + subject.getChars();
                frameIcon = subject.getObjectType().getIcon();
            }

            component.append(frameName, SimpleTextAttributes.REGULAR_ATTRIBUTES);
            component.append(" (line " + (sourcePosition.getLine() + 1) + ") ", SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES);
            component.setIcon(frameIcon);

        } else if (virtualFile != null){
            Icon frameIcon;
            if (virtualFile instanceof DBVirtualFile) {
                frameIcon = ((DBVirtualFile) virtualFile).getIcon();
            } else {
                frameIcon = virtualFile.getFileType().getIcon();
            }
            component.setIcon(frameIcon);
            component.append(virtualFile.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            component.append(" (line " + (sourcePosition.getLine() + 1) + ") ", SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES);
        } else {
            component.append(XDebuggerBundle.message("invalid.frame"), SimpleTextAttributes.ERROR_ATTRIBUTES);
        }
    }
}
