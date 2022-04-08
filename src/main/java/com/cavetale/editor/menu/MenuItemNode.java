package com.cavetale.editor.menu;

public interface MenuItemNode {
    VariableType getVariableType();

    default NodeType getNodeType() {
        return getVariableType().getNodeType();
    }

    String getKey();

    Object getValue();

    boolean canSetValue();

    void setValue(Object newValue);

    boolean isDeletable();

    void delete();

    MenuNode getMenuNode();
}
