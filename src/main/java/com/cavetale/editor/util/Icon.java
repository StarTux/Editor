package com.cavetale.editor.util;

import com.cavetale.core.editor.EditMenuAdapter;
import com.cavetale.editor.menu.MenuItemNode;
import com.cavetale.editor.menu.MenuNode;
import com.cavetale.editor.menu.NodeType;
import com.cavetale.editor.menu.VariableType;
import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.item.font.Glyph;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.noSeparators;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;

public final class Icon {
    private Icon() { }

    public static ItemStack of(MenuNode menu, Object o) {
        if (o == null) return Mytems.CHECKBOX.createIcon();
        if (o instanceof EditMenuAdapter adapter) {
            ItemStack custom = adapter.getMenuIcon(menu);
            if (custom != null) return custom;
        }
        if (o instanceof Boolean bool) {
            return bool
                ? Mytems.ON.createItemStack()
                : Mytems.OFF.createItemStack();
        }
        if (o instanceof Material material && material.isItem() && !material.isEmpty()) {
            return new ItemStack(material);
        }
        if (o instanceof Mytems mytems) {
            return mytems.createIcon();
        }
        if (o instanceof EntityType entityType && entityType != EntityType.UNKNOWN) {
            ItemStack result = Bukkit.getItemFactory().getSpawnEgg(entityType);
            if (result != null) return result;
        }
        if (o instanceof Enum || o instanceof String) {
            return firstLetter(o);
        }
        return NodeType.of(o.getClass()).mytems.createIcon();
    }

    public static ItemStack firstLetter(Object o) {
        String name = o.toString().toLowerCase();
        for (int i = 0; i < name.length(); i += 1) {
            Glyph glyph = Glyph.toGlyph(name.charAt(i));
            if (glyph != null) return glyph.mytems.createIcon();
        }
        return Mytems.QUESTION_MARK.createIcon();
    }


    public static List<Component> tooltip(MenuNode menu, MenuItemNode node) {
        List<Component> result = new ArrayList<>();
        Object o = node.getValue();
        Component value;
        VariableType variableType = node.getVariableType();
        if (variableType.nodeType.isPrimitive()) {
            value = o != null
                ? text(o.toString(), WHITE)
                : text("null", DARK_PURPLE, ITALIC);
        } else if (variableType.nodeType.isContainer()) {
            int size = 0;
            String sizeString = "?";
            if (o instanceof Collection collection) {
                size = collection.size();
                sizeString = size > 0 ? "[" + size + "]" : "[]";
            } else if (o instanceof Map map) {
                size = map.size();
                sizeString = size > 0 ? "{" + size + "}" : "{}";
            }
            value = o != null
                ? text(sizeString, WHITE)
                : text("null", DARK_PURPLE, ITALIC);
        } else {
            value = o != null
                ? text(VariableType.classNameOf(o.getClass()), WHITE)
                : text("null", DARK_PURPLE, ITALIC);
        }
        result.add(join(noSeparators(), text(node.getKey(), GRAY), text(": ", DARK_GRAY), value));
        if (o instanceof EditMenuAdapter adapter) {
            List<Component> custom = adapter.getTooltip(menu);
            if (custom != null) {
                result.addAll(custom);
            }
            return result;
        }
        if (variableType.nodeType == NodeType.OBJECT || variableType.nodeType == NodeType.ENUM) {
            result.add(text(variableType.nodeType.humanName + " " + variableType.getClassName(), DARK_GRAY, ITALIC));
        } else {
            result.add(text(variableType.nodeType.humanName, DARK_GRAY, ITALIC));
        }
        return result;
    }
}
