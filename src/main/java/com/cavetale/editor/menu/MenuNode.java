package com.cavetale.editor.menu;

import java.util.List;

/**
 * A node which can be turned into a navigable GUI menu.
 */
public interface MenuNode {
    Object getObject();

    List<? extends MenuItemNode> getChildren();

    default MenuItemNode getChildNode(String key) {
        for (MenuItemNode it : getChildren()) {
            if (key.equals(it.getKey())) return it;
        }
        return null;
    }

    List<Object> copy(List<Integer> selection);

    boolean canCut(List<Integer> selection);

    List<Object> cut(List<Integer> selection);

    boolean canPaste(List<Object> clipboard, List<Integer> selection);

    void paste(List<Object> clipboard, List<Integer> selection);
}
