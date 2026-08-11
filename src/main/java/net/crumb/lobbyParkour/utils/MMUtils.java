package net.crumb.lobbyParkour.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class MMUtils {
    // Sends a player a message with the minimessage format
    public static void sendMessage(Player player, String message) {
        Component parsed = TextFormatter.deserialize(message);
        player.sendMessage(parsed);
    }

    public static void sendMessage(Player player, String message, MessageType messageType) {
        Component prefix;
        Component parsed;

        switch (messageType) {
            case INFO:
                prefix = TextFormatter.deserialize("<color:#52a3ff>ⓘ</color> ");
                parsed = TextFormatter.deserialize("<color:#57ff65>" + message + "</color>");
                break;
            case WARNING:
                prefix = TextFormatter.deserialize("<color:#ffd321>⚠</color> ");
                parsed = TextFormatter.deserialize("<color:#ffeb7a>" + message + "</color>");
                break;
            case ERROR:
                prefix = TextFormatter.deserialize("<color:#ad1f39>☒</color> ");
                parsed = TextFormatter.deserialize("<color:#ff3358>" + message + "</color>");
                break;
            case DEBUG:
                prefix = TextFormatter.deserialize("<color:#ed3ef0>?</color> ");
                parsed = TextFormatter.deserialize("<color:#ffffff>" + message + "</color>");
                break;
            default:
                player.sendMessage(TextFormatter.deserialize(message));
                return;
        }

        player.sendMessage(prefix.append(parsed));
    }
}
