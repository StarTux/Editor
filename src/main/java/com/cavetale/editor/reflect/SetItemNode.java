package com.cavetale.editor.reflect;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class SetItemNode implements MenuItemNode {
    private final SetNode parentNode;
    private final Object value;

    @Override
    public NodeType getNodeType() {
        return parentNode.valueNodeType;
    }

    @Override
    public String getKey() {
        return value.toString();
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public boolean canSetValue() {
        return false;
    }

    @Override
    public void setValue(Object newValue) {
        throw new IllegalStateException("Not implemented!");
    }

    @Override
    public boolean isDeletable() {
        return true;
    }

    @Override
    public void delete() {
        parentNode.set.remove(value);
    }

    @Override
    public MenuNode getMenuNode() {
        if (parentNode.valueNodeType == NodeType.OBJECT) {
            if (value != null) return new ObjectNode(value);
        }
        return null;
    }

    @Override
    public boolean canHold(Object object) {
        return false;
    }
}
