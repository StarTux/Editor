package com.cavetale.editor.reflect;

import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

public sealed interface MenuItemNode permits FieldNode {
    NodeType getNodeType();

    String getKey();

    Object getValue();

    void setValue(Object newValue);

    ItemStack getIcon();

    List<Component> getTooltip();

    MenuNode getMenuNode();

    boolean isDeletable();
}
