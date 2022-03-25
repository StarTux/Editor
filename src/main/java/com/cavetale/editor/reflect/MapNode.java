package com.cavetale.editor.reflect;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class MapNode implements MenuNode {
    private final Map<?, ?> map;
    private final Class<?> keyClass;
    private final Class<?> valueClass;

    @Override
    public Object getObject() {
        return map;
    }

    @Override
    public List<MenuItemNode> getChildren() {
        return List.of();
    }
}
