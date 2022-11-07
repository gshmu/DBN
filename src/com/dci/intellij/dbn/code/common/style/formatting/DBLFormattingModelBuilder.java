package com.dci.intellij.dbn.code.common.style.formatting;

import com.dci.intellij.dbn.code.common.style.DBLCodeStyleManager;
import com.dci.intellij.dbn.code.common.style.options.DBLCodeStyleSettings;
import com.dci.intellij.dbn.common.compatibility.Compatibility;
import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.common.util.Documents;
import com.dci.intellij.dbn.common.util.Traces;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.intellij.formatting.Block;
import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelBuilder;
import com.intellij.formatting.FormattingModelProvider;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.impl.source.codeStyle.CodeFormatterFacade;
import org.jetbrains.annotations.NotNull;

public class DBLFormattingModelBuilder implements FormattingModelBuilder {

    @Override
    @NotNull
    @Compatibility
    public FormattingModel createModel(final PsiElement element, final CodeStyleSettings codeStyleSettings) {
        DBLanguage language = (DBLanguage) PsiUtil.getLanguage(element);

        PsiFile psiFile = element.getContainingFile();
        Document document = Documents.getDocument(psiFile);
        if (document != null && document.getTextLength() != psiFile.getTextLength()) {
            // TODO check why this happens (during startup)
            throw AlreadyDisposedException.INSTANCE;
        }


        Project project = element.getProject();
        DBLCodeStyleSettings settings = language.codeStyleSettings(project);

        boolean deliberate = Traces.isCalledThrough(CodeFormatterFacade.class);
        if (deliberate && settings.getCaseSettings().isEnabled()) {
            DBLCodeStyleManager.getInstance(project).formatCase(element.getContainingFile());
        }

        Block rootBlock = deliberate && settings.getFormattingSettings().isEnabled() ?
                new FormattingBlock(codeStyleSettings, settings, element, null, 0) :
                new PassiveFormattingBlock(element);
        return FormattingModelProvider.createFormattingModelForPsiFile(psiFile, rootBlock, codeStyleSettings);
    }

    @Override
    public TextRange getRangeAffectingIndent(PsiFile psiFile, int i, ASTNode astNode) {
        return astNode.getTextRange();
    }
}
