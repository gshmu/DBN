package com.dci.intellij.dbn.debugger.jdwp.frame;

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
import com.dci.intellij.dbn.debugger.DBDebugUtil;
import com.dci.intellij.dbn.debugger.jdbc.evaluation.DBProgramDebuggerEvaluator;
import com.dci.intellij.dbn.debugger.jdwp.DBJdwpDebugProcess;
import com.dci.intellij.dbn.execution.ExecutionInput;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.language.psql.PSQLLanguage;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
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
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.intellij.xdebugger.impl.XSourcePositionImpl;
import gnu.trove.THashMap;

public class DBJdwpDebugStackFrame extends XStackFrame {
    private int index;
    private XStackFrame underlyingFrame;
    private DBJdwpDebugProcess debugProcess;
    private XSourcePosition sourcePosition;
    private DBProgramDebuggerEvaluator evaluator;
    private Map<String, DBJdwpDebugValue> valuesMap;

    public DBJdwpDebugStackFrame(DBJdwpDebugProcess debugProcess, XStackFrame underlyingFrame, int index) {
        this.underlyingFrame = underlyingFrame;
        this.index = index;
        VirtualFile virtualFile = debugProcess.getVirtualFile(underlyingFrame);

        this.debugProcess = debugProcess;
        XSourcePosition sourcePosition = underlyingFrame.getSourcePosition();
        int lineNumber = sourcePosition == null ? 0 : sourcePosition.getLine();
        String ownerName = debugProcess.getOwnerName(underlyingFrame);
        if (ownerName == null) {
            ExecutionInput executionInput = debugProcess.getExecutionInput();
            if (executionInput instanceof StatementExecutionInput) {
                StatementExecutionInput statementExecutionInput = (StatementExecutionInput) executionInput;
                lineNumber += statementExecutionInput.getExecutableLineNumber();
            }
        }
        this.sourcePosition = XSourcePositionImpl.create(virtualFile, lineNumber);
    }

    public DBJdwpDebugProcess getDebugProcess() {
        return debugProcess;
    }

    public DBJdwpDebugValue getValue(String variableName) {
        return valuesMap == null ? null : valuesMap.get(variableName.toLowerCase());
    }

    public void setValue(String variableName, DBJdwpDebugValue value) {
        if (valuesMap == null) {
            valuesMap =new THashMap<String, DBJdwpDebugValue>();
        }
        valuesMap.put(variableName.toLowerCase(), value);
    }

    @Override
    public XDebuggerEvaluator getEvaluator() {
/*
        if (evaluator == null) {
            evaluator = new DBProgramDebuggerEvaluator(this);
        }
*/
        return evaluator;
    }

    @Override
    public XSourcePosition getSourcePosition() {
        return sourcePosition;
    }

    public void customizePresentation(@NotNull ColoredTextContainer component) {
        VirtualFile virtualFile = DBDebugUtil.getSourceCodeFile(sourcePosition);

        DBSchemaObject object = DBDebugUtil.getObject(sourcePosition);
        if (object != null) {
            String frameName = object.getName();
            Icon frameIcon = object.getIcon();

            Project project = getDebugProcess().getProject();
            Document document = DocumentUtil.getDocument(virtualFile);
            DBLanguagePsiFile psiFile = (DBLanguagePsiFile) PsiUtil.getPsiFile(project, virtualFile);

            if (psiFile != null && document != null) {
                int offset = document.getLineEndOffset(sourcePosition.getLine());
                PsiElement elementAtOffset = psiFile.findElementAt(offset);
                while (elementAtOffset instanceof PsiWhiteSpace || elementAtOffset instanceof PsiComment) {
                    elementAtOffset = elementAtOffset.getNextSibling();
                }

                if (elementAtOffset instanceof BasePsiElement) {
                    BasePsiElement basePsiElement = (BasePsiElement) elementAtOffset;
                    BasePsiElement objectDeclarationPsiElement = basePsiElement.findEnclosingPsiElement(ElementTypeAttribute.OBJECT_DECLARATION);
                    if (objectDeclarationPsiElement != null) {
                        IdentifierPsiElement subjectPsiElement = (IdentifierPsiElement) objectDeclarationPsiElement.findFirstPsiElement(ElementTypeAttribute.SUBJECT);
                        if (subjectPsiElement != null) {
                            frameName = frameName + "." + subjectPsiElement.getChars();
                            frameIcon = subjectPsiElement.getObjectType().getIcon();
                        }
                    }
                }

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

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        valuesMap = new THashMap<String, DBJdwpDebugValue>();
        List<DBJdwpDebugValue> values = new ArrayList<DBJdwpDebugValue>();

        VirtualFile sourceCodeFile = DBDebugUtil.getSourceCodeFile(sourcePosition);

        Project project = getDebugProcess().getProject();
        Document document = DocumentUtil.getDocument(sourceCodeFile);
        DBLanguagePsiFile psiFile = (DBLanguagePsiFile) PsiUtil.getPsiFile(project, sourceCodeFile);

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
                    DBJdwpDebugValue value = new DBJdwpDebugValue(debugProcess, null, variableName, childVariableNames, icon, index);
                    values.add(value);
                    valuesMap.put(variableName.toLowerCase(), value);
                }
            }
        }
        Collections.sort(values);

        XValueChildrenList children = new XValueChildrenList();
        for (DBJdwpDebugValue value : values) {
            children.add(value.getVariableName(), value);
        }
        node.addChildren(children, true);
    }

    @Nullable
    @Override
    public Object getEqualityObject() {
/*
        DebuggerRuntimeInfo runtimeInfo = debugProcess.getRuntimeInfo();
        return runtimeInfo == null ? null : runtimeInfo.getOwnerName() + "." + runtimeInfo.getProgramName();
*/
        return null;
    }
}


