/*
 * This file is part of ViaRewind-Legacy-Support - https://github.com/ViaVersion/ViaRewind-Legacy-Support
 * Copyright (C) 2018-2024 ViaVersion and contributors
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

package com.viaversion.viarewind.legacysupport;

import com.viaversion.viarewind.legacysupport.injector.BoundingBoxFixer;
import com.viaversion.viarewind.legacysupport.versioninfo.VersionInformer;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viarewind.legacysupport.listener.AreaEffectCloudListener;
import com.viaversion.viarewind.legacysupport.listener.BounceListener;
import com.viaversion.viarewind.legacysupport.listener.BrewingListener;
import com.viaversion.viarewind.legacysupport.listener.ElytraListener;
import com.viaversion.viarewind.legacysupport.listener.EnchantingListener;
import com.viaversion.viarewind.legacysupport.listener.SoundListener;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class BukkitPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Make the config file
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        // Load VR LS
        final FileConfiguration config = getConfig();

        new BukkitRunnable() {

            @Override
            public void run() {
                final int serverProtocol = Via.getAPI().getServerVersion().lowestSupportedVersion();
                if (serverProtocol == -1) return;
                cancel();

                if (serverProtocol >= ProtocolVersion.v1_8.getVersion()) {
                    if (config.getBoolean("enchanting-gui-fix"))
                        Bukkit.getPluginManager().registerEvents(new EnchantingListener(), BukkitPlugin.this);

                    if (config.getBoolean("slime-fix"))
                        Bukkit.getPluginManager().registerEvents(new BounceListener(), BukkitPlugin.this);
                }
                if (serverProtocol >= ProtocolVersion.v1_9.getVersion()) {
                    if (config.getBoolean("sound-fix"))
                        Bukkit.getPluginManager().registerEvents(new SoundListener(BukkitPlugin.this), BukkitPlugin.this);

                    if (config.getBoolean("ladder-fix")) // 15w31a
                        BoundingBoxFixer.fixLadder(getLogger(), serverProtocol);

                    if (config.getBoolean("area-effect-cloud-particles")) // 15w32c
                        Bukkit.getPluginManager().registerEvents(new AreaEffectCloudListener(BukkitPlugin.this), BukkitPlugin.this);

                    if (config.getBoolean("elytra-fix")) // 15w40b
                        Bukkit.getPluginManager().registerEvents(new ElytraListener(), BukkitPlugin.this);

                    if (config.getBoolean("brewing-stand-gui-fix")) // 15w41b
                        Bukkit.getPluginManager().registerEvents(new BrewingListener(), BukkitPlugin.this);

                    if (config.getBoolean("lily-pad-fix")) // 15w44b
                        BoundingBoxFixer.fixLilyPad(getLogger(), serverProtocol);
                }
                if (serverProtocol >= ProtocolVersion.v1_14_4.getVersion() && config.getBoolean("carpet-fix")) {
                    BoundingBoxFixer.fixCarpet(getLogger(), serverProtocol);
                }

                if (config.getBoolean("versioninfo.active")) {
                    new VersionInformer(BukkitPlugin.this, config);
                }
            }
        }.runTaskTimer(this, 1L, 1L);
    }
}
