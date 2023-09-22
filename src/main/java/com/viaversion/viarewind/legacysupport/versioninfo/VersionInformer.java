/*
 * This file is part of ViaRewind-Legacy-Support - https://github.com/ViaVersion/ViaRewind-Legacy-Support
 * Copyright (C) 2016-2023 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.viaversion.viarewind.legacysupport.versioninfo;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viarewind.legacysupport.BukkitPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@SuppressWarnings({"DataFlowIssue", "unchecked"})
public class VersionInformer implements Listener {
    private final String[] versionMessage;
    private final int maxVersion;

    public VersionInformer(final BukkitPlugin plugin, final FileConfiguration config) {
        final String message = ChatColor.translateAlternateColorCodes('&', config.getString("versioninfo.message")).
                replace("%version%", Bukkit.getVersion().split(" ")[2].replace(")", ""));

        this.versionMessage = message.split(System.lineSeparator());
        maxVersion = config.getInt("versioninfo.max-version");

        String interval = config.getString("versioninfo.interval");
        if (interval.equalsIgnoreCase("JOIN")) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
        } else {
            long ticks;
            try {
                ticks = Long.parseLong(interval);
            } catch (NumberFormatException e) {
                Bukkit.getLogger().warning("Invalid interval for versioninfo.interval, defaulting to 6000");
                ticks = 6000;
            }

            Bukkit.getScheduler().runTaskTimer(plugin, () -> Bukkit.getOnlinePlayers().forEach(this::inform), ticks, ticks);
        }
    }

    protected void inform(final Player player) {
        int version = Via.getAPI().getPlayerVersion(player);
        if (version > maxVersion) return;

        player.sendMessage(this.versionMessage);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        inform(e.getPlayer());
    }
}
