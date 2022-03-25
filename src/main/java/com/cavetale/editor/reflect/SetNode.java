package com.cavetale.editor.reflect;

import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class SetNode implements MenuNode {
    private final Set<?> set;
    private final Class<?> valueType;

    @Override
    public Object getObject() {
        return set;
    }

    @Override
    public List<MenuItemNode> getChildren() {
        return List.of();
    }
}
