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

package com.viaversion.viarewind.legacysupport.injector;

import com.viaversion.viaversion.api.Via;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class NMSReflection {
    private static final int PROTOCOL_1_17 = 755;
    private static int protocolVersion = -1;

    private static String version;
    private static Field playerConnectionField;

    public static String getVersion() {
        return version == null ? version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] : version;
    }

    public static int getProtocolVersion() {
        return protocolVersion == -1 ? protocolVersion = Via.getAPI().getServerVersion().lowestSupportedVersion() : protocolVersion;
    }

    public static Class<?> getBlockPositionClass() {
        try {
            if (getProtocolVersion() >= PROTOCOL_1_17) {
                return Class.forName("net.minecraft.core.BlockPosition");
            }
            return getLegacyNMSClass("BlockPosition");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Class<?> getNMSBlock(String name) {
        try {
            if (getProtocolVersion() >= PROTOCOL_1_17) {
                return Class.forName("net.minecraft.world.level.block." + name);
            }
            return getLegacyNMSClass(name);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public static Class getSoundCategoryClass() {
        try {
            if (getProtocolVersion() >= PROTOCOL_1_17) {
                return Class.forName("net.minecraft.sounds.SoundCategory");
            }
            return getLegacyNMSClass("SoundCategory");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Class<?> getPacketClass() {
        try {
            if (getProtocolVersion() >= PROTOCOL_1_17) {
                return Class.forName("net.minecraft.network.protocol.Packet");
            }
            return getLegacyNMSClass("Packet");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Class<?> getGamePacketClass(String packetType) {
        try {
            if (getProtocolVersion() >= PROTOCOL_1_17) {
                return Class.forName("net.minecraft.network.protocol.game." + packetType);
            }
            return getLegacyNMSClass(packetType);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Class<?> getPlayerConnectionClass() {
        try {
            if (getProtocolVersion() >= PROTOCOL_1_17) {
                return Class.forName("net.minecraft.server.network.PlayerConnection");
            }
            return getLegacyNMSClass("PlayerConnection");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Class<?> getLegacyNMSClass(String name) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + getVersion() + "." + name);
    }

    public static void sendPacket(Player player, Object packet) throws ReflectiveOperationException {
        Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
        if (playerConnectionField == null) {
            playerConnectionField = Arrays.stream(nmsPlayer.getClass().getFields())
                    .filter(field -> field.getType() == getPlayerConnectionClass()).findFirst()
                    .orElseThrow(() -> new ReflectiveOperationException("Failed to find PlayerConnection field in EntityPlayer"));
        }
        Object playerConnection = playerConnectionField.get(nmsPlayer);
        Method sendPacket;
        try { // TODO find better way
            sendPacket = playerConnection.getClass().getDeclaredMethod("sendPacket", getPacketClass());
        } catch (Exception e) {
            try {
                sendPacket = playerConnection.getClass().getDeclaredMethod("a", getPacketClass());
            } catch (Exception e2) {
                throw new ReflectiveOperationException("Failed to find sendPacket method in PlayerConnection");
            }
        }
        sendPacket.invoke(playerConnection, packet);
    }
}
