/*
 * Copyright 2000-2011 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dci.intellij.dbn.data.find.action;

import com.dci.intellij.dbn.data.find.DataSearchComponent;
import com.intellij.find.FindModel;
import com.intellij.find.FindSettings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ToggleRegex extends DataSearchHeaderToggleAction {

    public ToggleRegex(DataSearchComponent searchComponent) {
        super(searchComponent, "&Regex");
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return getEditorSearchComponent().getFindModel().isRegularExpressions();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        FindModel findModel = getEditorSearchComponent().getFindModel();
        findModel.setRegularExpressions(state);
        if (state) {
            findModel.setWholeWordsOnly(false);
        }
        FindSettings.getInstance().setLocalRegularExpressions(state);
    }
}
