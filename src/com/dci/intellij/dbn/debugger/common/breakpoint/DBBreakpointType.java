package com.dci.intellij.dbn.debugger.common.breakpoint;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.debugger.jdbc.evaluation.DBJdbcDebuggerEditorsProvider;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.DBLanguageFileType;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.xdebugger.XDebuggerBundle;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;

public class DBBreakpointType extends XLineBreakpointType<DBBreakpointProperties> {
    public DBBreakpointType() {
        super("db-program", "DB-Program Breakpoint");
    }

    @Override
    public boolean canPutAt(@NotNull VirtualFile file, int line, @NotNull Project project) {
        if (file.getFileType() instanceof DBLanguageFileType) {
            PsiFile psiFile = PsiUtil.getPsiFile(project, file);
            if (psiFile instanceof DBLanguagePsiFile) {
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
                        BasePsiElement debuggablePsiElement = basePsiElement.findEnclosingPsiElement(ElementTypeAttribute.DEBUGGABLE);
                        return debuggablePsiElement != null;
                    }
                }
            }
        }
        return false;
    }

    @Nullable
    private BasePsiElement findPsiElement(PsiFile psiFile, int line) {
        Document document = DocumentUtil.getDocument(psiFile);
        if (document != null) {
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
                int elementLine = document.getLineNumber(basePsiElement.getTextOffset());
                if (elementLine == line) {
                    return basePsiElement;
                }
            }
        }
        return null;
    }

    @Override
    public DBBreakpointProperties createBreakpointProperties(@NotNull VirtualFile file, int line) {
        if (file instanceof ConnectionProvider) {
            ConnectionProvider connectionProvider = (ConnectionProvider) file;
            return new DBBreakpointProperties(connectionProvider.getConnectionHandler());
        }
        return new DBBreakpointProperties(null);
    }

    @Nullable
    @Override
    public DBBreakpointProperties createProperties() {
        return new DBBreakpointProperties();
    }

    @Override
    public XDebuggerEditorsProvider getEditorsProvider() {
        return DBJdbcDebuggerEditorsProvider.INSTANCE;
    }

    @Nullable
    @Override
    public XDebuggerEditorsProvider getEditorsProvider(@NotNull XLineBreakpoint<DBBreakpointProperties> breakpoint, @NotNull Project project) {
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
}
