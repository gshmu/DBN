package com.dci.intellij.dbn.code.common.completion.options;

import com.dci.intellij.dbn.code.common.completion.options.filter.CodeCompletionFiltersSettings;
import com.dci.intellij.dbn.code.common.completion.options.sorting.CodeCompletionSortingSettings;
import com.dci.intellij.dbn.code.common.completion.options.ui.CodeCompletionSettingsForm;
import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.intellij.openapi.project.Project;
import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class CodeCompletionSettings extends CompositeProjectConfiguration<CodeCompletionSettingsForm> {
    private CodeCompletionFiltersSettings filtersSettings;
    private CodeCompletionSortingSettings sortingSettings;

    public CodeCompletionSettings(Project project) {
        super(project);
        filtersSettings = new CodeCompletionFiltersSettings();
        sortingSettings = new CodeCompletionSortingSettings();
        loadDefaults();
    }

    public static CodeCompletionSettings getInstance(Project project) {
        return getGlobalProjectSettings(project).getCodeCompletionSettings();
    }

    @NotNull
    @Override
    public String getId() {
        return "DBNavigator.Project.CodeCompletionSettings";
    }


    public String getDisplayName() {
        return "Code Completion";
    }

    public String getHelpTopic() {
        return "codeEditor";
    }

   private void loadDefaults() {
       try {
           Document document = CommonUtil.loadXmlFile(getClass(), "default-settings.xml");
           Element root = document.getRootElement();
           readConfiguration(root);
       } catch (Exception e) {
           e.printStackTrace();
       }
   }

   /*********************************************************
    *                         Custom                        *
    *********************************************************/
    public CodeCompletionFiltersSettings getFilterSettings() {
        return filtersSettings;
    }

    public CodeCompletionSortingSettings getSortingSettings() {
        return sortingSettings;
    }

    /*********************************************************
    *                     Configuration                      *
    *********************************************************/

    protected CodeCompletionSettingsForm createConfigurationEditor() {
        return new CodeCompletionSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "code-completion-settings";
    }

    protected Configuration[] createConfigurations() {
        return new Configuration[] {
                filtersSettings,
                sortingSettings};
    }
}
