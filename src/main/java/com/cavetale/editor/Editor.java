package com.cavetale.editor;

import com.cavetale.core.editor.EditMenuDelegate;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Editor implements com.cavetale.core.editor.Editor {
    private final EditorPlugin plugin;

    @Override
    public void open(Plugin owningPlugin, Player player, Object rootObject, EditMenuDelegate delegate) {
        EditorPlugin.instance.sessions.of(player).setup(owningPlugin, rootObject, delegate).open(player);
    }
}
