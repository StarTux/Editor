package com.cavetale.editor.reflect;

import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class MapItemNode implements MenuItemNode {
    private final MapNode parentNode;
    private final Object key;

    @Override
    public NodeType getNodeType() {
        return parentNode.valueNodeType;
    }

    @Override
    public String getKey() {
        return key.toString();
    }

    @Override
    public Object getValue() {
        return parentNode.map.get(key);
    }

    @Override
    public boolean canSetValue() {
        return true;
    }

    @Override
    public void setValue(Object newValue) {
        parentNode.map.put(key, newValue);
    }

    @Override
    public boolean isDeletable() {
        return true;
    }

    @Override
    public void delete() {
        parentNode.map.remove(key);
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
