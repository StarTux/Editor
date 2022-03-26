package com.cavetale.editor.reflect;

import java.util.List;

/**
 * A node which can be turned into a navigable GUI menu.
 */
public sealed interface MenuNode permits ObjectNode, MapNode, ListNode, SetNode {
    Object getObject();

    List<? extends MenuItemNode> getChildren();

    default MenuItemNode getChildNode(String key) {
        for (MenuItemNode it : getChildren()) {
            if (key.equals(it.getKey())) return it;
        }
        return null;
    }

    default List<Object> cut(List<Integer> selection) {
        throw new IllegalStateException("Cut not implemented");
    }

    default List<Object> copy(List<Integer> selection) {
        throw new IllegalStateException("Copy not implemented");
    }

    default int paste(List<Object> clipboard, List<Integer> selection) {
        throw new IllegalStateException("Paste not implemented");
    }
}
