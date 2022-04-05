package com.cavetale.editor.menu;

import com.cavetale.core.editor.EditMenuAdapter;
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
        if (getValue() instanceof EditMenuAdapter adapter) {
            ItemStack custom = adapter.getMenuIcon();
            if (custom != null) return custom;
        }
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
        if (getValue() instanceof EditMenuAdapter adapter) {
            List<Component> custom = adapter.getTooltip();
            if (custom != null) return custom;
        }
        Component value;
        VariableType variableType = getVariableType();
        NodeType nodeType = variableType.nodeType;
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
        Component line2;
        if (nodeType == NodeType.OBJECT || nodeType == NodeType.ENUM) {
            line2 = text(nodeType.humanName + " " + variableType.objectType.getSimpleName(), DARK_GRAY, ITALIC);
        } else {
            line2 = text(nodeType.humanName, DARK_GRAY, ITALIC);
        }
        return List.of(join(noSeparators(), text(getKey(), GRAY), text(": ", DARK_GRAY), value),
                       line2);
    }
}
