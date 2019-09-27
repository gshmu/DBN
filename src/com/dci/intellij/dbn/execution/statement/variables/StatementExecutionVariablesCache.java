package com.dci.intellij.dbn.execution.statement.variables;

import com.dci.intellij.dbn.common.ProjectRef;
import com.dci.intellij.dbn.common.state.PersistentStateElement;
import com.dci.intellij.dbn.common.util.FileUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StatementExecutionVariablesCache implements PersistentStateElement {
    private ProjectRef projectRef;
    private Map<String, Set<StatementExecutionVariable>> fileVariablesMap = new THashMap<String, Set<StatementExecutionVariable>>();

    public StatementExecutionVariablesCache(Project project) {
        this.projectRef = ProjectRef.from(project);
    }

    public Project getProject() {
        return projectRef.ensure();
    }

    public Set<StatementExecutionVariable> getVariables(@Nullable VirtualFile virtualFile) {
        if (virtualFile != null) {
            String fileUrl = virtualFile.getUrl();
            return fileVariablesMap.computeIfAbsent(fileUrl, s -> new THashSet<>());
        }
        return Collections.emptySet();
    }

    public void cacheVariable(@Nullable VirtualFile virtualFile, StatementExecutionVariable executionVariable) {
        if (virtualFile != null) {
            Set<StatementExecutionVariable> variables = getVariables(virtualFile);
            for (StatementExecutionVariable variable : variables) {
                if (variable.getName().equals(executionVariable.getName())) {
                    variable.setValue(executionVariable.getValue());
                    return;
                }
            }
            variables.add(new StatementExecutionVariable(executionVariable));
        }
    }

    @Nullable
    public StatementExecutionVariable getVariable(@Nullable VirtualFile virtualFile, String name) {
        if (virtualFile != null) {
            Set<StatementExecutionVariable> variables = getVariables(virtualFile);
            for (StatementExecutionVariable variable : variables) {
                if (StringUtil.equalsIgnoreCase(variable.getName(), name)) {
                    return variable;
                }
            }
        }
        return null;
    }

    /*********************************************
     *            PersistentStateElement         *
     ********************************************
     * @param element*/
    @Override
    public void readState(Element element) {
        Element variablesElement = element.getChild("execution-variables");
        if (variablesElement != null) {
            this.fileVariablesMap.clear();
            List<Element> fileElements = variablesElement.getChildren();

            for (Element fileElement : fileElements) {
                String fileUrl = fileElement.getAttributeValue("file-url");
                if ( StringUtil.isEmpty(fileUrl)) {
                    // TODO backward compatibility. Do cleanup
                    fileUrl = fileElement.getAttributeValue("path");
                }

                Set<StatementExecutionVariable> fileVariables = new THashSet<StatementExecutionVariable>();
                this.fileVariablesMap.put(fileUrl, fileVariables);

                List<Element> variableElements = fileElement.getChildren();
                for (Element variableElement : variableElements) {
                    StatementExecutionVariable executionVariable = new StatementExecutionVariable(variableElement);
                    fileVariables.add(executionVariable);
                }
            }
        }
    }

    @Override
    public void writeState(Element element) {
        Element variablesElement = new Element("execution-variables");
        element.addContent(variablesElement);

        for (String fileUrl : fileVariablesMap.keySet()) {

            if (FileUtil.isValidFileUrl(fileUrl, getProject())) {
                Element fileElement = new Element("file");
                fileElement.setAttribute("file-url", fileUrl);
                Set<StatementExecutionVariable> executionVariables = fileVariablesMap.get(fileUrl);
                for (StatementExecutionVariable executionVariable : executionVariables) {
                    Element variableElement = executionVariable.getState();
                    fileElement.addContent(variableElement);
                }
                variablesElement.addContent(fileElement);
            }
        }
    }
}
