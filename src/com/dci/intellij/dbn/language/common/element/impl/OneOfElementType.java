package com.dci.intellij.dbn.language.common.element.impl;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.cache.OneOfElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.parser.BranchCheck;
import com.dci.intellij.dbn.language.common.element.parser.impl.OneOfElementTypeParser;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeDefinitionException;
import com.dci.intellij.dbn.language.common.psi.SequencePsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.stringAttribute;

public final class OneOfElementType extends ElementTypeBase {
    private ElementTypeRef[] children;
    private boolean sortable;
    private boolean sorted;
    private boolean basic;

    public OneOfElementType(ElementTypeBundle bundle, ElementTypeBase parent, String id, Element def) throws ElementTypeDefinitionException {
        super(bundle, parent, id, def);
    }

    @Override
    protected void loadDefinition(Element def) throws ElementTypeDefinitionException {
        super.loadDefinition(def);
        ElementTypeBundle bundle = getBundle();
        String tokenIds = stringAttribute(def, "tokens");
        if (StringUtil.isNotEmptyOrSpaces(tokenIds)) {
            basic = true;
            String id = getId();

            String[] tokens = tokenIds.split(",");
            children = new ElementTypeRef[tokens.length];
            for (int i=0; i<tokens.length; i++) {
                String tokenTypeId = tokens[i].trim();
                ElementTypeRef previous = i == 0 ? null : children[i-1];

                TokenElementType tokenElementType = new TokenElementType(bundle, this, tokenTypeId, id);
                children[i] = new ElementTypeRef(previous, this, tokenElementType, false, 0, null);
            }
            sortable = false;
        } else {
            List<Element> children = def.getChildren();
            this.children = new ElementTypeRef[children.size()];

            ElementTypeRef previous = null;
            for (int i=0; i<children.size(); i++) {
                Element child = children.get(i);
                String type = child.getName();
                ElementTypeBase elementType = bundle.resolveElementDefinition(child, type, this);
                double version = Double.parseDouble(CommonUtil.nvl(stringAttribute(child, "version"), "0"));
                Set<BranchCheck> branchChecks = parseBranchChecks(stringAttribute(child, "branch-check"));

                this.children[i] = new ElementTypeRef(previous, this, elementType, false, version, branchChecks);
                previous = this.children[i];

            }
            sortable = getBooleanAttribute(def, "sortable");
        }
    }

    @Override
    protected OneOfElementTypeLookupCache createLookupCache() {
        return new OneOfElementTypeLookupCache(this);
    }

    @NotNull
    @Override
    protected OneOfElementTypeParser createParser() {
        return new OneOfElementTypeParser(this);
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @NotNull
    @Override
    public String getName() {
        return "one-of (" + getId() + ")";
    }

    @Override
    public PsiElement createPsiElement(ASTNode astNode) {
        return new SequencePsiElement(astNode, this);
    }

    public void sort() {
        if (sortable && !sorted) {
            sorted = true;
            Arrays.sort(children, ONE_OF_COMPARATOR);
        }
    }

    private static final Comparator<ElementTypeRef> ONE_OF_COMPARATOR = (o1, o2) -> {
        int i1 = o1.getLookupCache().startsWithIdentifier() ? 1 : 2;
        int i2 = o2.getLookupCache().startsWithIdentifier() ? 1 : 2;
        return i2-i1;
    };

    public ElementTypeRef[] getChildren() {
        return children;
    }

    public ElementTypeRef getFirstChild() {
        return children[0];
    }

    @Override
    public void collectLeafElements(Set bucket) {
        if (basic) {
            for (ElementTypeRef child : children) {
                bucket.add(child.elementType);
            }
        }
    }
}
