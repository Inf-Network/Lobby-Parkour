package net.crumb.lobbyParkour.utils;

import net.crumb.lobbyParkour.LobbyParkour;
import net.crumb.lobbyParkour.database.ParkoursDatabase;
import net.crumb.lobbyParkour.database.Query;
import net.crumb.lobbyParkour.listeners.EntityRemove;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ReloadParkour {
    private static final LobbyParkour plugin = LobbyParkour.getInstance();
    private static final TextFormatter textFormatter = new TextFormatter();

    /** Refreshes all stored holograms without sending a player-facing message. */
    public static void reload() {
        reload(null);
    }

    public static void reload(Player player) {
        if (player != null) {
            MMUtils.sendMessage(player, "Reloading all parkours...", MessageType.INFO);
        }

        try {
            ParkoursDatabase database = new ParkoursDatabase(plugin.getDataFolder().getAbsolutePath() + "/lobby_parkour.db");
            Query query = new Query(database.getConnection());

            List<Object[]> starts = query.getAllParkourStarts();

            for (Object[] data : starts) {
                String name = (String) data[0];
                Location loc = (Location) data[1];
                Material mat = (Material) data[2];

                loc.getBlock().setType(mat);

                UUID oldUuid = query.getStartEntityUuid(name);
                if (oldUuid != null) {
                    Entity oldEntity = loc.getWorld().getEntity(oldUuid);
                    if (oldEntity instanceof TextDisplay) {
                        EntityRemove.suppress(oldUuid);
                        oldEntity.remove();
                    }
                }


                Map<String, String> placeholders = Map.of(
                        "parkour_name", name
                );
                Component startText = textFormatter.formatString(ConfigManager.getFormat().getStartPlate(), placeholders);

                Location textDisplayLocation = new Location(loc.getWorld(), loc.getX() + 0.5, loc.getY() + 1.0, loc.getZ() + 0.5);
                TextDisplay display = loc.getWorld().spawn(textDisplayLocation, TextDisplay.class, textDisplay -> {
                    textDisplay.text(startText);
                    textDisplay.setBillboard(Display.Billboard.CENTER);
                });

                query.updateStartEntityUuid(name, display.getUniqueId());
            }

            List<Object[]> ends = query.getAllParkourEnds();

            for (Object[] data : ends) {
                String name = (String) data[0];
                Location loc = (Location) data[1];
                Material mat = (Material) data[2];

                loc.getBlock().setType(mat);

                UUID oldUuid = query.getEndEntityUuid(name);
                if (oldUuid != null) {
                    Entity oldEntity = loc.getWorld().getEntity(oldUuid);
                    if (oldEntity instanceof TextDisplay) {
                        EntityRemove.suppress(oldUuid);
                        oldEntity.remove();
                    }
                }

                Map<String, String> placeholders = Map.of(
                        "parkour_name", name
                );
                Component endText = textFormatter.formatString(ConfigManager.getFormat().getEndPlate(), placeholders);

                Location textDisplayLocation = new Location(loc.getWorld(), loc.getX() + 0.5, loc.getY() + 1.0, loc.getZ() + 0.5);
                TextDisplay display = loc.getWorld().spawn(textDisplayLocation, TextDisplay.class, textDisplay -> {
                    textDisplay.text(endText);
                    textDisplay.setBillboard(Display.Billboard.CENTER);
                });

                query.updateEndEntityUuid(name, display.getUniqueId());
            }

            // Existing checkpoint entities survive a plugin reload too. Update
            // their text in place so legacy color codes are applied immediately.
            for (Object[] checkpoint : query.getCheckpoints()) {
                int parkourId = (int) checkpoint[5];
                String parkourName = query.getParkourNameById(parkourId);
                Location location = LocationHelper.stringToLocation((String) checkpoint[2]);
                Entity entity = location == null || location.getWorld() == null
                        ? null : location.getWorld().getEntity((UUID) checkpoint[4]);
                if (!(entity instanceof TextDisplay textDisplay) || parkourName == null) continue;

                int total = query.getCheckpoints(parkourId).size();
                Component checkpointText = textFormatter.formatString(
                        ConfigManager.getFormat().getCheckpointPlate(),
                        Map.of(
                                "parkour_name", parkourName,
                                "checkpoint", String.valueOf(checkpoint[1]),
                                "checkpoint_total", String.valueOf(total)
                        ));
                textDisplay.text(checkpointText);
            }

            if (player != null) {
                MMUtils.sendMessage(player, "Parkours reloaded successfully!", MessageType.INFO);
                SoundUtils.playSoundSequence(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f, 0);
                SoundUtils.playSoundSequence(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f, 4);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            if (player != null) {
                MMUtils.sendMessage(player, "There was an error while reloading the parkours!", MessageType.ERROR);
            }
        }
    }
}
