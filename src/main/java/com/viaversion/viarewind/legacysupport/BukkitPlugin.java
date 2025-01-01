/*
 * This file is part of ViaRewind-Legacy-Support - https://github.com/ViaVersion/ViaRewind-Legacy-Support
 * Copyright (C) 2018-2025 ViaVersion and contributors
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

import com.viaversion.viarewind.legacysupport.feature.BlockCollisionChanges;
import com.viaversion.viarewind.legacysupport.versioninfo.VersionInformer;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viarewind.legacysupport.feature.AreaEffectCloudEmulator;
import com.viaversion.viarewind.legacysupport.feature.SlimeBounceEmulator;
import com.viaversion.viarewind.legacysupport.feature.BrewingInteractionEmulator;
import com.viaversion.viarewind.legacysupport.feature.ElytraVelocityEmulator;
import com.viaversion.viarewind.legacysupport.feature.EnchantingGuiEmulator;
import com.viaversion.viarewind.legacysupport.feature.BlockPlaceSoundEmulator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitPlugin extends JavaPlugin {

    private ProtocolVersion serverProtocol;

    @Override
    public void onEnable() {
        // Make the config file
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        if (Via.getManager().getInjector().lateProtocolVersionSetting()) {
            // Enable in the next tick
            Via.getPlatform().runSync(this::enable, 1);
        } else {
            enable();
        }
    }

    private void enable() {
        serverProtocol = Via.getAPI().getServerVersion().lowestSupportedProtocolVersion();
        if (!serverProtocol.isKnown()) {
            return;
        }

        final FileConfiguration config = getConfig();
        if (serverProtocol.newerThanOrEqualTo(ProtocolVersion.v1_8)) {
            if (config.getBoolean("enchanting-gui-fix")) {
                Bukkit.getPluginManager().registerEvents(new EnchantingGuiEmulator(), BukkitPlugin.this);
            }
            if (config.getBoolean("slime-fix")) {
                Bukkit.getPluginManager().registerEvents(new SlimeBounceEmulator(), BukkitPlugin.this);
            }
        }
        if (serverProtocol.newerThanOrEqualTo(ProtocolVersion.v1_9)) {
            if (config.getBoolean("sound-fix")) {
                Bukkit.getPluginManager().registerEvents(new BlockPlaceSoundEmulator(BukkitPlugin.this), BukkitPlugin.this);
            }
            if (config.getBoolean("ladder-fix")) { // 15w31a
                BlockCollisionChanges.fixLadder(getLogger(), serverProtocol);
            }
            if (config.getBoolean("area-effect-cloud-particles")) { // 15w32c
                Bukkit.getPluginManager().registerEvents(new AreaEffectCloudEmulator(BukkitPlugin.this), BukkitPlugin.this);
            }
            if (config.getBoolean("elytra-fix")) { // 15w40b
                Bukkit.getPluginManager().registerEvents(new ElytraVelocityEmulator(), BukkitPlugin.this);
            }
            if (config.getBoolean("brewing-stand-gui-fix")) { // 15w41b
                Bukkit.getPluginManager().registerEvents(new BrewingInteractionEmulator(), BukkitPlugin.this);
            }
            if (config.getBoolean("lily-pad-fix")) { // 15w44b
                BlockCollisionChanges.fixLilyPad(getLogger(), serverProtocol);
            }
        }
        if (serverProtocol.newerThanOrEqualTo(ProtocolVersion.v1_14_4) && config.getBoolean("carpet-fix")) {
            BlockCollisionChanges.fixCarpet(getLogger(), serverProtocol);
        }
        if (config.getBoolean("versioninfo.active")) {
            new VersionInformer(BukkitPlugin.this, config);
        }
    }

    public ProtocolVersion getServerProtocol() {
        return serverProtocol;
    }

    public static BukkitPlugin getInstance() {
        return getPlugin(BukkitPlugin.class);
    }

}
