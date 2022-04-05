package com.cavetale.editor.session;

import com.cavetale.editor.EditorPlugin;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Sessions manager.
 */
@RequiredArgsConstructor
public final class Sessions implements Listener {
    private final EditorPlugin plugin;
    private final Map<UUID, Session> sessions = new HashMap<>();

    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void disable() {
        sessions.clear();
    }

    public Session of(Player player) {
        return sessions.computeIfAbsent(player.getUniqueId(), u -> new Session(this, player.getUniqueId()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncChat(final AsyncChatEvent event) {
        Session session = sessions.get(event.getPlayer().getUniqueId());
        if (session == null || session.chatCallback == null) return;
        if (!(event.originalMessage() instanceof TextComponent textComponent)) return;
        Bukkit.getScheduler().runTask(plugin, () -> {
                Consumer<String> chatCallback = session.chatCallback;
                session.chatCallback = null;
                chatCallback.accept(textComponent.content());
            });
        event.setCancelled(true);
    }
}
