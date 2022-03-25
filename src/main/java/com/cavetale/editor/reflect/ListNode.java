package com.cavetale.editor.reflect;

import lombok.RequiredArgsConstructor;
import java.util.List;

@RequiredArgsConstructor
public final class ListNode implements MenuNode {
    private final List<?> list;
    private final Class<?> valueType;

    @Override
    public Object getObject() {
        return list;
    }

    @Override
    public List<MenuItemNode> getChildren() {
        return List.of();
    }
}
