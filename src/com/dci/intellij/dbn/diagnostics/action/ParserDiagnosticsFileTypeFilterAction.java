package com.dci.intellij.dbn.diagnostics.action;

import com.dci.intellij.dbn.common.ui.DBNComboBoxAction;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.diagnostics.data.ParserDiagnosticsFilter;
import com.dci.intellij.dbn.diagnostics.ui.ParserDiagnosticsDetailsForm;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import java.util.Arrays;

public class ParserDiagnosticsFileTypeFilterAction extends DBNComboBoxAction implements DumbAware {
    private final ParserDiagnosticsDetailsForm form;

    public ParserDiagnosticsFileTypeFilterAction(ParserDiagnosticsDetailsForm form) {
        this.form = form;
    }

    @Override
    @NotNull
    protected DefaultActionGroup createPopupActionGroup(JComponent component) {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new SelectFilterValueAction(null));
        actionGroup.addSeparator();
        for (String fileType : Arrays.asList("sql", "pkg")) {
            actionGroup.add(new SelectFilterValueAction(fileType));
        }
        return actionGroup;
    }

    @Override
    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();

        ParserDiagnosticsFilter resultFilter = getResultFilter();
        String fileType = resultFilter.getFileType();
        presentation.setText(StringUtil.isEmpty(fileType) ? "file type" : "*." + fileType, false);
    }

    private ParserDiagnosticsFilter getResultFilter() {
        return form.getManager().getResultFilter();
    }

    private class SelectFilterValueAction extends AnAction {
        private final String fileType;

        public SelectFilterValueAction(String fileType) {
            super(StringUtil.isEmpty(fileType) ? "No Filter" : "*." + fileType);
            this.fileType = fileType;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            getResultFilter().setFileType(fileType);
            form.refreshResult();
        }
    }
 }