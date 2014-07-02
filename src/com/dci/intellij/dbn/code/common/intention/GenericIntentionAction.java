package com.dci.intellij.dbn.code.common.intention;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.util.Iconable;
import org.jetbrains.annotations.NotNull;

public abstract class GenericIntentionAction implements IntentionAction, Iconable {

    @NotNull
    public String getFamilyName() {
        return "DBNavigator intentions";
    }
}
