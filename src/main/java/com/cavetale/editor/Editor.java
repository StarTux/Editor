package com.cavetale.editor;

import com.cavetale.core.editor.EditMenuDelegate;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.separator;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;

@RequiredArgsConstructor
public final class Editor implements com.cavetale.core.editor.Editor {
    private final EditorPlugin plugin;

    @Override
    public void open(Plugin owningPlugin, Player player, Object rootObject, EditMenuDelegate delegate) {
        EditorPlugin.instance.sessions.of(player).setup(owningPlugin, rootObject, delegate).open(player);
        player.sendMessage(join(separator(space()),
                                text("Menu opened. Click here to"),
                                text("[Reopen]", GREEN))
                           .clickEvent(ClickEvent.runCommand("/editor reopen"))
                           .hoverEvent(HoverEvent.showText(text("/editor reopen", GREEN))));
    }
}
