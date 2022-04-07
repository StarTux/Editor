package com.cavetale.editor.reflect;

import com.cavetale.editor.menu.MenuItemNode;
import com.cavetale.editor.menu.MenuNode;
import com.cavetale.editor.menu.NodeType;
import com.cavetale.editor.menu.VariableType;

public final class ListItemNode implements MenuItemNode {
    private final ListNode parentNode;
    private final int index;

    protected ListItemNode(final ListNode parentNode, final int index) {
        this.parentNode = parentNode;
        this.index = index;
    }

    @Override
    public VariableType getVariableType() {
        return parentNode.valueType;
    }

    @Override
    public String getKey() {
        return "" + index;
    }

    @Override
    public Object getValue() {
        return parentNode.list.get(index);
    }

    @Override
    public boolean canSetValue() {
        return true;
    }

    @Override
    public void setValue(Object newValue) {
        parentNode.list.set(index, newValue);
    }

    @Override
    public boolean isDeletable() {
        return true;
    }

    @Override
    public void delete() {
        parentNode.list.remove(index);
    }

    @Override
    public MenuNode getMenuNode() {
        if (parentNode.valueType.nodeType == NodeType.OBJECT) {
            Object value = getValue();
            if (value != null) return new ObjectNode(value);
        }
        return null;
    }
}
