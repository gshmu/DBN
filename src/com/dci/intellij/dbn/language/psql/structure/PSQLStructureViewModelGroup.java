package com.dci.intellij.dbn.language.psql.structure;

import com.intellij.ide.util.treeView.smartTree.Group;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.common.util.NamingUtil;

import javax.swing.Icon;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.awt.Color;
import java.awt.Font;

public class PSQLStructureViewModelGroup implements Group {
    private static TextAttributesKey TEXT_ATTRIBUTES_KEY =
            TextAttributesKey.createTextAttributesKey(
                    "PSQLStructureViewModelGroup",
                    new TextAttributes(Color.BLACK, null, null, null, Font.BOLD));

    private DBObjectType objectType;
    private List<TreeElement> children = new ArrayList<TreeElement>();


    public PSQLStructureViewModelGroup(DBObjectType objectType) {
        this.objectType = objectType;
    }

    public void addChild(TreeElement treeElement) {
        children.add(treeElement);
    }

    public ItemPresentation getPresentation() {
        return itemPresentation;
    }

    public Collection<TreeElement> getChildren() {
        return children;
    }


    private ItemPresentation itemPresentation = new ItemPresentation(){
        public String getPresentableText() {
            return NamingUtil.capitalize(objectType.getListName());
        }

        public String getLocationString() {
            return null;
        }

        public Icon getIcon(boolean open) {
            return null;//objectType.getListIcon();
        }

        public TextAttributesKey getTextAttributesKey() {
            return TEXT_ATTRIBUTES_KEY;
        }
    };
}