package com.dci.intellij.dbn.debugger.common.breakpoint;

import com.dci.intellij.dbn.common.util.Documents;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.context.DatabaseContext;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.debugger.jdbc.DBJdbcBreakpointProperties;
import com.dci.intellij.dbn.debugger.jdbc.evaluation.DBJdbcDebuggerEditorsProvider;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.xdebugger.XDebuggerBundle;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;

import static com.dci.intellij.dbn.common.dispose.Checks.isNotValid;
import static com.dci.intellij.dbn.common.dispose.Failsafe.conditionallyLog;
import static com.dci.intellij.dbn.common.util.Files.isDbLanguageFile;
import static com.dci.intellij.dbn.common.util.Files.isDbLanguagePsiFile;

@Slf4j
public class DBBreakpointType extends XLineBreakpointType<XBreakpointProperties> {

    public DBBreakpointType() {
        super("db-program", "DB-Program Breakpoint");
    }

    @Override
    public boolean canPutAt(@NotNull VirtualFile file, int line, @NotNull Project project) {
        if (isNotValid(file)) return false;
        if (!isDbLanguageFile(file)) return false;

        PsiFile psiFile = PsiUtil.getPsiFile(project, file);
        if (isNotValid(psiFile)) return false;
        if (!isDbLanguagePsiFile(psiFile)) return false;

        if (file instanceof DBSourceCodeVirtualFile) {
            DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) file;
            DBContentType contentType = sourceCodeFile.getContentType();
            if (contentType == DBContentType.CODE || contentType == DBContentType.CODE_BODY) {
                BasePsiElement basePsiElement = findPsiElement(psiFile, line);
                return basePsiElement != null;
            }
        } else {
            BasePsiElement basePsiElement = findPsiElement(psiFile, line);
            if (basePsiElement != null) {
                BasePsiElement debuggablePsiElement = basePsiElement.findEnclosingElement(ElementTypeAttribute.DEBUGGABLE);
                return debuggablePsiElement != null;
            }
        }
        return false;
    }

    @Nullable
    private BasePsiElement findPsiElement(PsiFile psiFile, int line) {
        Document document = Documents.getDocument(psiFile);
        if (document != null && line > -1 && document.getLineCount() > line) {
            int lineOffset = document.getLineStartOffset(line);
            PsiElement element = psiFile.findElementAt(lineOffset);
            while (element != null && !(element instanceof BasePsiElement)) {
                PsiElement nextSibling = element.getNextSibling();
                if (nextSibling == null) {
                    element = element.getParent();
                    break;
                } else {
                    element = nextSibling;
                }
            }

            if (element instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) element;
                int textOffset = basePsiElement.getTextOffset();
                if (textOffset< document.getTextLength()) {
                    int elementLine = document.getLineNumber(textOffset);
                    if (elementLine == line) {
                        return basePsiElement;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public XBreakpointProperties createBreakpointProperties(@NotNull VirtualFile file, int line) {
        ConnectionHandler connection = null;
        if (file instanceof DatabaseContext) {
            DatabaseContext connectionProvider = (DatabaseContext) file;
            connection = connectionProvider.getConnection();
        }

        return createBreakpointProperties(connection);
    }

    @Nullable
    @Override
    public XBreakpointProperties createProperties() {
        return createBreakpointProperties(null);
    }

    @Override
    public XDebuggerEditorsProvider getEditorsProvider() {
        return DBJdbcDebuggerEditorsProvider.INSTANCE;
    }

    @Nullable
    @Override
    public XDebuggerEditorsProvider getEditorsProvider(@NotNull XLineBreakpoint<XBreakpointProperties> breakpoint, @NotNull Project project) {
        return DBJdbcDebuggerEditorsProvider.INSTANCE;
    }

    @Override
    public String getDisplayText(XLineBreakpoint breakpoint) {
        XSourcePosition sourcePosition = breakpoint.getSourcePosition();
        if (sourcePosition != null ){
        VirtualFile file = sourcePosition.getFile();
        return XDebuggerBundle.message("xbreakpoint.default.display.text",
                breakpoint.getLine() + 1,
                file.getPresentableUrl());
        }
        return "unknown";
    }

    private static XBreakpointProperties createBreakpointProperties(ConnectionHandler connection) {
        if (DBDebuggerType.JDWP.isSupported()) {
            try {
                Class propertiesClass = Class.forName("com.dci.intellij.dbn.debugger.jdwp.DBJdwpBreakpointProperties");
                Constructor constructor = propertiesClass.getConstructor(ConnectionHandler.class);
                return (XBreakpointProperties) constructor.newInstance(connection);
            } catch (Exception e) {
                conditionallyLog(e);
                log.error("Error creating JDWP breakpoints properties", e);
            }
        }

        return new DBJdbcBreakpointProperties(connection);
    }

}
