package com.cavetale.editor.util;

import com.cavetale.core.editor.EditMenuAdapter;
import com.cavetale.editor.menu.NodeType;
import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.item.font.Glyph;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public final class Icon {
    private Icon() { }

    public static ItemStack of(Object o) {
        if (o == null) return Mytems.CHECKBOX.createIcon();
        if (o instanceof EditMenuAdapter adapter) {
            ItemStack custom = adapter.getMenuIcon();
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
        if (o instanceof Enum) {
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
}
