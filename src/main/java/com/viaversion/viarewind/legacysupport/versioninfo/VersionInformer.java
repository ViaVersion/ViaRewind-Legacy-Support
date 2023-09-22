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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class VersionInformer implements Listener {
    private final String[] versionMessage;
    private final int maxVersion;

    public VersionInformer() {
        String message = BukkitPlugin.getInstance().getConfig().getString("versioninfo.message");
        message = ChatColor.translateAlternateColorCodes('&', message);
        message = message.replace("%version%", Bukkit.getVersion().split(" ")[2].replace(")", ""));
        this.versionMessage = message.split(System.lineSeparator());

        maxVersion = BukkitPlugin.getInstance().getConfig().getInt("versioninfo.max-version");
        String interval = BukkitPlugin.getInstance().getConfig().getString("versioninfo.interval");
        if (interval.equalsIgnoreCase("JOIN")) {
            Bukkit.getPluginManager().registerEvents(this, BukkitPlugin.getInstance());
        } else {
            long ticks = Long.parseLong(interval);
            Bukkit.getScheduler().runTaskTimer(BukkitPlugin.getInstance(), () -> {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    int version = Via.getAPI().getPlayerVersion(player);
                    if (version > maxVersion) return;
                    player.sendMessage(this.versionMessage);
                });
            }, ticks, ticks);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        int version = Via.getAPI().getPlayerVersion(e.getPlayer());
        if (version > maxVersion) return;
        e.getPlayer().sendMessage(this.versionMessage);
    }
}
