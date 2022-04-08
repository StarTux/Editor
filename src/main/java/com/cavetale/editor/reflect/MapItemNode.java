package com.cavetale.editor.reflect;

import com.cavetale.editor.menu.MenuItemNode;
import com.cavetale.editor.menu.MenuNode;
import com.cavetale.editor.menu.NodeType;
import com.cavetale.editor.menu.VariableType;
import lombok.RequiredArgsConstructor;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;

@RequiredArgsConstructor
public final class MapItemNode implements MenuItemNode {
    private final MapNode parentNode;
    private final Object key;

    @Override
    public VariableType getVariableType() {
        return parentNode.valueType;
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
        if (parentNode.valueType.nodeType == NodeType.OBJECT) {
            Object value = getValue();
            if (value != null) return new ObjectNode(parentNode.session, parentNode, value);
        }
        return null;
    }
}
