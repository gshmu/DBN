package com.dci.intellij.dbn.language.common.parameter;

import com.dci.intellij.dbn.language.common.element.WrapperElementType;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.NamedPsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.language.common.psi.lookup.ObjectReferenceLookupAdapter;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.parameterInfo.CreateParameterInfoContext;
import com.intellij.lang.parameterInfo.ParameterInfoContext;
import com.intellij.lang.parameterInfo.ParameterInfoHandler;
import com.intellij.lang.parameterInfo.ParameterInfoUIContext;
import com.intellij.lang.parameterInfo.UpdateParameterInfoContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MethodParameterInfoHandler implements ParameterInfoHandler<BasePsiElement, DBArgument> {
    private String id = "METHOD_PARAMETER_HANDLER";
    public static final ObjectReferenceLookupAdapter METHOD_LOOKUP_ADAPTER = new ObjectReferenceLookupAdapter(null, DBObjectType.METHOD, null);

    @Override
    public boolean couldShowInLookup() {
        return true;
    }

    @Nullable
    @Override
    public Object[] getParametersForLookup(LookupElement item, ParameterInfoContext context) {
        return new Object[0];
    }

    @Nullable
    @Override
    public Object[] getParametersForDocumentation(DBArgument argument, ParameterInfoContext context) {
        return new Object[0];
    }

    @Nullable
    @Override
    public BasePsiElement findElementForParameterInfo(@NotNull CreateParameterInfoContext context) {
        BasePsiElement basePsiElement = PsiUtil.lookupWrapperElementAtOffset(context.getFile(), context.getOffset());
        if (basePsiElement != null) {
            WrapperElementType wrapperElementType = (WrapperElementType) basePsiElement.getElementType();
            if (id.equals(wrapperElementType.getParameterHandler())) {
                NamedPsiElement enclosingNamedPsiElement = basePsiElement.findEnclosingNamedPsiElement();
                BasePsiElement methodPsiElement = METHOD_LOOKUP_ADAPTER.findInElement(enclosingNamedPsiElement);
                if (methodPsiElement instanceof IdentifierPsiElement) {
                    IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) methodPsiElement;
                    DBObject object = identifierPsiElement.resolveUnderlyingObject();
                    if (object instanceof DBMethod) {
                        DBMethod method = (DBMethod) object;
                        context.setItemsToShow(method.getArguments().toArray());
                        return identifierPsiElement;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void showParameterInfo(@NotNull BasePsiElement element, @NotNull CreateParameterInfoContext context) {
        context.showHint(element, context.getOffset(), this);
    }

    @Nullable
    @Override
    public BasePsiElement findElementForUpdatingParameterInfo(@NotNull UpdateParameterInfoContext context) {
        return PsiUtil.lookupWrapperElementAtOffset(context.getFile(), context.getOffset());
    }

    @Override
    public void updateParameterInfo(@NotNull BasePsiElement o, @NotNull UpdateParameterInfoContext context) {

    }

    @Nullable
    @Override
    public String getParameterCloseChars() {
        return null;
    }

    @Override
    public boolean tracksParameterIndex() {
        return false;
    }

    @Override
    public void updateUI(DBArgument argument, @NotNull ParameterInfoUIContext context) {
        context.setUIComponentEnabled(true);
        String text = argument.getName() + " " + argument.getDataType().getQualifiedName();
        context.setupUIComponentPresentation(text, 0, text.length(), false, false, false, context.getDefaultParameterColor());
    }
}
