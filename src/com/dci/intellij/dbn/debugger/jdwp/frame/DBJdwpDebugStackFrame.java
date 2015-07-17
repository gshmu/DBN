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
import com.dci.intellij.dbn.common.util.LazyValue;
import com.dci.intellij.dbn.common.util.SimpleLazyValue;
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
import com.intellij.debugger.engine.JavaStackFrame;
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
import com.sun.jdi.Location;
import gnu.trove.THashMap;

public class DBJdwpDebugStackFrame extends XStackFrame {
    private int index;
    private XStackFrame underlyingFrame;
    private DBJdwpDebugProcess debugProcess;
    private DBProgramDebuggerEvaluator evaluator;
    private Map<String, DBJdwpDebugValue> valuesMap;

    private LazyValue<Location> location = new SimpleLazyValue<Location>() {
        @Override
        protected Location load() {
            return ((JavaStackFrame) underlyingFrame).getDescriptor().getLocation();
        }
    };

    private LazyValue<XSourcePosition> sourcePosition = new SimpleLazyValue<XSourcePosition>() {
        @Override
        protected XSourcePosition load() {
            Location location = getLocation();
            int lineNumber = location == null ? 0 : location.lineNumber() - 1;

            String ownerName = debugProcess.getOwnerName(underlyingFrame);
            if (ownerName == null) {
                ExecutionInput executionInput = debugProcess.getExecutionInput();
                if (executionInput instanceof StatementExecutionInput) {
                    StatementExecutionInput statementExecutionInput = (StatementExecutionInput) executionInput;
                    lineNumber += statementExecutionInput.getExecutableLineNumber();
                }
            }
            return XSourcePositionImpl.create(getVirtualFile(), lineNumber);
        }
    };

    private LazyValue<VirtualFile> virtualFile = new SimpleLazyValue<VirtualFile>() {
        @Override
        protected VirtualFile load() {
            return debugProcess.getVirtualFile(underlyingFrame);
        }
    };

    private LazyValue<IdentifierPsiElement> subject = new SimpleLazyValue<IdentifierPsiElement>() {
        @Override
        protected IdentifierPsiElement load() {
            Project project = getDebugProcess().getProject();
            XSourcePosition sourcePosition = getSourcePosition();
            VirtualFile virtualFile = getVirtualFile();
            Document document = DocumentUtil.getDocument(virtualFile);
            DBLanguagePsiFile psiFile = (DBLanguagePsiFile) PsiUtil.getPsiFile(project, virtualFile);

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

    public DBJdwpDebugStackFrame(DBJdwpDebugProcess debugProcess, XStackFrame underlyingFrame, int index) {
        this.debugProcess = debugProcess;
        this.underlyingFrame = underlyingFrame;
        this.index = index;
    }

    public XStackFrame getUnderlyingFrame() {
        return underlyingFrame;
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


    public Location getLocation() {
        return location.get();
    }

    @Override
    public XSourcePosition getSourcePosition() {
        return sourcePosition.get();
    }

    public VirtualFile getVirtualFile() {
        return virtualFile.get();
    }

    public IdentifierPsiElement getSubject() {
        return subject.get();
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

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        XSourcePosition sourcePosition = getSourcePosition();
        valuesMap = new THashMap<String, DBJdwpDebugValue>();
        List<DBJdwpDebugValue> values = new ArrayList<DBJdwpDebugValue>();

        VirtualFile sourceCodeFile = DBDebugUtil.getSourceCodeFile(sourcePosition);

        if (sourceCodeFile != null) {
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
                        DBJdwpDebugValue value = new DBJdwpDebugValue(this, null, variableName, childVariableNames, icon, index);
                        values.add(value);
                        valuesMap.put(variableName.toLowerCase(), value);
                    }
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
        DBSchemaObject object = DBDebugUtil.getObject(getSourcePosition());
        return object == null ? null : object.getQualifiedName();
    }
}


