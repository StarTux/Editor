package com.cavetale.editor.menu;

import com.cavetale.mytems.Mytems;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.noSeparators;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;

public interface MenuItemNode {
    VariableType getVariableType();

    default NodeType getNodeType() {
        return getVariableType().getNodeType();
    }

    String getKey();

    Object getValue();

    boolean canSetValue();

    void setValue(Object newValue);

    boolean isDeletable();

    void delete();

    MenuNode getMenuNode();

    boolean canHold(Object object);

    default ItemStack getIcon() {
        NodeType nodeType = getNodeType();
        switch (nodeType) {
        case BOOLEAN: {
            return getValue() == Boolean.TRUE
                ? Mytems.ON.createItemStack()
                : Mytems.OFF.createItemStack();
        }
        default:
            return nodeType.mytems.createItemStack();
        }
    }

    default List<Component> getTooltip() {
        Component value;
        NodeType nodeType = getNodeType();
        if (nodeType.isPrimitive()) {
            Object obj = getValue();
            value = obj != null
                ? text(obj.toString(), WHITE)
                : text("null", DARK_PURPLE, ITALIC);
        } else {
            Object obj = getValue();
            value = obj != null
                ? text(obj.getClass().getSimpleName(), WHITE)
                : text("null", DARK_PURPLE, ITALIC);
        }
        return List.of(join(noSeparators(), text(getKey(), GRAY), text(": ", DARK_GRAY), value),
                       text(nodeType.name().toLowerCase(), DARK_GRAY, ITALIC));
    }
}
