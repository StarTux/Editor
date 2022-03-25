package com.cavetale.editor;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class Editor {
    private Editor() { }

    public static void open(Plugin plugin, Player player, Object rootObject, EditContext context) {
        EditorPlugin.instance.sessions.of(player).setup(plugin, rootObject, context).open(player);
    }
}
