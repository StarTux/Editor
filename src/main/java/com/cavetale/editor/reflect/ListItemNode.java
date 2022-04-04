package com.cavetale.editor.reflect;

public final class ListItemNode implements MenuItemNode {
    private final ListNode parentNode;
    private final int index;

    protected ListItemNode(final ListNode parentNode, final int index) {
        this.parentNode = parentNode;
        this.index = index;
    }

    @Override
    public NodeType getNodeType() {
        return parentNode.valueNodeType;
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
        if (parentNode.valueNodeType == NodeType.OBJECT) {
            Object value = getValue();
            if (value != null) return new ObjectNode(value);
        }
        return null;
    }

    @Override
    public boolean canHold(Object object) {
        return parentNode.canHoldValue(object);
    }
}
