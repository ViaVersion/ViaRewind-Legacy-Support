/*
 * This file is part of ViaRewind-Legacy-Support - https://github.com/ViaVersion/ViaRewind-Legacy-Support
 * Copyright (C) 2018-2026 ViaVersion and contributors
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
package com.viaversion.viarewind.legacysupport.util;

import com.viaversion.viarewind.legacysupport.BukkitPlugin;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static com.viaversion.viarewind.legacysupport.util.ReflectionUtil.failSafeGetClass;

public class NMSUtil {

    public static final boolean NEWER_THAN_V1_20_5 = BukkitPlugin.getInstance().getServerProtocol().newerThanOrEqualTo(ProtocolVersion.v1_20_5);
    public static String nmsVersionPackage;
    private static Field playerConnectionField;

    static {
        if (BukkitPlugin.getInstance().getServerProtocol().olderThan(ProtocolVersion.v1_17)) {
            nmsVersionPackage = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        }
    }

    public static Class<?> getBlockPositionClass() {
        if (BukkitPlugin.getInstance().getServerProtocol().newerThanOrEqualTo(ProtocolVersion.v1_17)) {
            return failSafeGetClass("net.minecraft.core.BlockPosition");
        } else {
            return getLegacyNMSClass("BlockPosition");
        }
    }

    public static Class<?> getNMSBlockClass(final String name) {
        if (BukkitPlugin.getInstance().getServerProtocol().newerThanOrEqualTo(ProtocolVersion.v1_17)) {
            return failSafeGetClass("net.minecraft.world.level.block." + name);
        } else {
            return getLegacyNMSClass(name);
        }
    }

    public static Class getSoundCategoryClass() { // Bypass generics
        if (BukkitPlugin.getInstance().getServerProtocol().newerThanOrEqualTo(ProtocolVersion.v1_17)) {
            return failSafeGetClass("net.minecraft.sounds.SoundCategory");
        } else {
            return getLegacyNMSClass("SoundCategory");
        }
    }

    public static Class<?> getPacketClass() {
        if (BukkitPlugin.getInstance().getServerProtocol().newerThanOrEqualTo(ProtocolVersion.v1_17)) {
            return failSafeGetClass("net.minecraft.network.protocol.Packet");
        } else {
            return getLegacyNMSClass("Packet");
        }
    }

    public static Class<?> getGamePacketClass(final String packet) {
        if (BukkitPlugin.getInstance().getServerProtocol().newerThanOrEqualTo(ProtocolVersion.v1_17)) {
            return failSafeGetClass("net.minecraft.network.protocol.game." + packet);
        } else {
            return getLegacyNMSClass(packet);
        }
    }

    public static Class<?> getPlayerConnectionClass() {
        if (BukkitPlugin.getInstance().getServerProtocol().newerThanOrEqualTo(ProtocolVersion.v1_17)) {
            return failSafeGetClass("net.minecraft.server.network.PlayerConnection");
        } else {
            return getLegacyNMSClass("PlayerConnection");
        }
    }

    public static Object getNMSPlayer(final Player player) {
        try {
            return player.getClass().getMethod("getHandle").invoke(player);
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            BukkitPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to get EntityPlayer from player", e);
            return null;
        }
    }

    public static void sendPacket(final Player player, final Object packet) {
        final Object nmsPlayer = getNMSPlayer(player);
        if (nmsPlayer == null) {
            return;
        }

        // Cache result as it never changes
        if (playerConnectionField == null) {
            final Class<?> playerConnection = getPlayerConnectionClass();
            for (Field field : nmsPlayer.getClass().getFields()) {
                if (field.getType() == playerConnection) {
                    playerConnectionField = field;
                    break;
                }
            }

            // If reflection failed, log and return
            if (playerConnectionField == null) {
                BukkitPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to find PlayerConnection field in EntityPlayer");
                return;
            }
        }

        Object playerConnection;
        try {
            playerConnection = playerConnectionField.get(nmsPlayer);
        } catch (IllegalAccessException e) {
            BukkitPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to get PlayerConnection from EntityPlayer", e);
            return;
        }
        try {
            final Method sendPacket = ReflectionUtil.findMethod(playerConnection.getClass(), new String[]{"sendPacket", "a"}, getPacketClass());
            sendPacket.invoke(playerConnection, packet);
        } catch (IllegalAccessException | InvocationTargetException | NullPointerException e) {
            BukkitPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to send packet to player", e);
        }
    }

    public static Class<?> getLegacyNMSClass(final String name) {
        try {
            return Class.forName("net.minecraft.server." + nmsVersionPackage + "." + name);
        } catch (ClassNotFoundException e) {
            BukkitPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not find NMS class " + name + "! NMS version package: " + nmsVersionPackage, e);
            return null;
        }
    }

}
