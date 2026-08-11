package net.crumb.lobbyParkour.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class TextFormatter {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    /**
     * Parses MiniMessage while also accepting Minecraft's legacy color codes.
     * Supported legacy formats include {@code &d}, {@code §d}, and
     * {@code &#ff55ff}.
     */
    public static Component deserialize(String text) {
        if (text == null) {
            return Component.empty();
        }

        return MINI_MESSAGE.deserialize(convertLegacyCodes(text));
    }

    static String convertLegacyCodes(String text) {
        StringBuilder converted = new StringBuilder(text.length());

        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if ((current != '&' && current != '§') || i + 1 >= text.length()) {
                converted.append(current);
                continue;
            }

            char code = Character.toLowerCase(text.charAt(i + 1));

            // Bukkit's expanded legacy hex format: &x&R&R&G&G&B&B (and §x...).
            if (code == 'x' && i + 13 < text.length()) {
                StringBuilder hex = new StringBuilder(6);
                boolean valid = true;
                for (int part = 0; part < 6; part++) {
                    int separator = i + 2 + part * 2;
                    int digit = separator + 1;
                    if (text.charAt(separator) != current || !isHexDigit(text.charAt(digit))) {
                        valid = false;
                        break;
                    }
                    hex.append(text.charAt(digit));
                }
                if (valid) {
                    converted.append("<#").append(hex).append('>');
                    i += 13;
                    continue;
                }
            }

            // Common modern legacy hex syntax: &#RRGGBB
            if (code == '#' && i + 7 < text.length()) {
                String hex = text.substring(i + 2, i + 8);
                if (hex.chars().allMatch(TextFormatter::isHexDigit)) {
                    converted.append("<#").append(hex).append('>');
                    i += 7;
                    continue;
                }
            }

            String tag = switch (code) {
                case '0' -> "black";
                case '1' -> "dark_blue";
                case '2' -> "dark_green";
                case '3' -> "dark_aqua";
                case '4' -> "dark_red";
                case '5' -> "dark_purple";
                case '6' -> "gold";
                case '7' -> "gray";
                case '8' -> "dark_gray";
                case '9' -> "blue";
                case 'a' -> "green";
                case 'b' -> "aqua";
                case 'c' -> "red";
                case 'd' -> "light_purple";
                case 'e' -> "yellow";
                case 'f' -> "white";
                case 'k' -> "obfuscated";
                case 'l' -> "bold";
                case 'm' -> "strikethrough";
                case 'n' -> "underlined";
                case 'o' -> "italic";
                case 'r' -> "reset";
                default -> null;
            };

            if (tag == null) {
                converted.append(current);
                continue;
            }

            converted.append('<').append(tag).append('>');
            i++;
        }

        return converted.toString();
    }

    private static boolean isHexDigit(int character) {
        return character >= '0' && character <= '9'
                || character >= 'a' && character <= 'f'
                || character >= 'A' && character <= 'F';
    }

    /**
     * Formats a string with custom placeholders, PlaceholderAPI (if enabled), and MiniMessage.
     *
     * @param text   The input string with placeholders.
     * @param player The player (can be OfflinePlayer or Player).
     * @param data   A map of custom placeholders to replace (e.g., %checkpoint%).
     * @return The formatted Adventure Component.
     */
    public Component formatString(String text, @Nullable OfflinePlayer player, @Nullable Map<String, String> data) {
        if (text == null) {
            return Component.empty();
        }

        String formatted = text;

        if (data != null) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                formatted = formatted.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }

        if (player != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            formatted = PlaceholderAPI.setPlaceholders(player, formatted);
        }

        return deserialize(formatted);
    }


    public Component formatString(String text, @Nullable Player player, @Nullable Map<String, String> data) {
        return formatString(text, (OfflinePlayer) player, data);
    }

    public Component formatString(String text, @Nullable OfflinePlayer player) {
        return formatString(text, player, null);
    }

    public Component formatString(String text, @Nullable Player player) {
        return formatString(text, (OfflinePlayer) player, null);
    }

    public Component formatString(String text, @Nullable Map<String, String> data) {
        return formatString(text, null, data);
    }

    public Component formatString(String text) {
        return formatString(text, null, null);
    }
}
