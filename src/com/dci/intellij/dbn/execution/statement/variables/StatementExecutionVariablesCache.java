package com.dci.intellij.dbn.execution.statement.variables;

import com.dci.intellij.dbn.common.project.ProjectRef;
import com.dci.intellij.dbn.common.state.PersistentStateElement;
import com.dci.intellij.dbn.common.util.FileUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.THashSet;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StatementExecutionVariablesCache implements PersistentStateElement {
    private final ProjectRef project;
    private final Map<String, Set<StatementExecutionVariable>> fileVariablesMap = new HashMap<>();

    public StatementExecutionVariablesCache(Project project) {
        this.project = ProjectRef.of(project);
    }

    public Project getProject() {
        return project.ensure();
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
     *********************************************/

    @Override
    public void readState(Element element) {
        Element variablesElement = element.getChild("execution-variables");
        if (variablesElement != null) {
            this.fileVariablesMap.clear();

            for (Element fileElement : variablesElement.getChildren()) {
                String fileUrl = fileElement.getAttributeValue("file-url");
                if ( StringUtil.isEmpty(fileUrl)) {
                    // TODO backward compatibility. Do cleanup
                    fileUrl = fileElement.getAttributeValue("path");
                }

                Set<StatementExecutionVariable> fileVariables = new HashSet<>();
                this.fileVariablesMap.put(fileUrl, fileVariables);

                for (Element variableElement : fileElement.getChildren()) {
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
