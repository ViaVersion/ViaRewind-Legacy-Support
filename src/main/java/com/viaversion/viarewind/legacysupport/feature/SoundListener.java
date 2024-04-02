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

package com.viaversion.viarewind.legacysupport.feature;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viarewind.legacysupport.BukkitPlugin;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import java.lang.reflect.Method;
import java.util.logging.Level;

import static com.viaversion.viarewind.legacysupport.util.ReflectionUtil.*;
import static com.viaversion.viarewind.legacysupport.util.NMSUtil.*;

@SuppressWarnings("unchecked")
public class SoundListener implements Listener {

    private static boolean isSoundCategory = false;

    static {
        try {
            Class.forName("org.bukkit.SoundCategory");
            isSoundCategory = true;
        } catch (ClassNotFoundException ignored) {
        }
    }

    public SoundListener(final BukkitPlugin plugin) {
        try {
            Class.forName("org.bukkit.event.entity.EntityPickupItemEvent");

            Bukkit.getPluginManager().registerEvents(new Listener() {

                @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
                public void onItemPickUp(EntityPickupItemEvent e) {
                    if (!(e.getEntity() instanceof Player)) return;

                    SoundListener.this.onItemPickUp((Player) e.getEntity());
                }

            }, plugin);
        } catch (Exception ex) {
            Bukkit.getPluginManager().registerEvents(new Listener() {

                @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
                public void onItemPickUp(PlayerPickupItemEvent e) {
                    SoundListener.this.onItemPickUp(e.getPlayer());
                }

            }, plugin);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        final Player player = e.getPlayer();
        if (Via.getAPI().getPlayerProtocolVersion(player).newerThanOrEqualTo(ProtocolVersion.v1_9)) return;

        if (Via.getAPI().getServerVersion().lowestSupportedProtocolVersion().newerThanOrEqualTo(ProtocolVersion.v1_17)) {
            player.playSound(e.getBlockPlaced().getLocation(), e.getBlock().getBlockData().getSoundGroup().getPlaceSound(), 1.0f, 0.8f);
        } else {
            try {
                playBlockPlaceSoundNMS(player, e.getBlock());
            } catch (Exception exception) {
                Via.getPlatform().getLogger().log(Level.SEVERE, "Could not play block place sound.", exception);
            }
        }
    }

    private void onItemPickUp(Player player) {
        final float volume = 0.2f;
        final float pitch = (float) ((Math.random() - Math.random()) * 0.7f + 1.0f) * 2.0f;

        playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, volume, pitch);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onExperienceOrbPickup(PlayerExpChangeEvent e) {
        final float volume = 0.1f;
        final float pitch = (float) (0.5f * ((Math.random() - Math.random()) * 0.7f + 1.8f));

        playSound(e.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, volume, pitch);
    }

    private static void playSound(final Location loc, final Sound sound, final float volume, final float pitch) {
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getWorld() == loc.getWorld())
                .filter(p -> p.getLocation().distanceSquared(loc) < (double) 16 * (double) 16)
                .filter(p -> Via.getAPI().getPlayerVersion(p) <= 47)
                .forEach(p -> {
                    if (isSoundCategory) {
                        p.playSound(loc, sound, SoundCategory.valueOf("PLAYERS"), volume, pitch);
                    } else {
                        p.playSound(loc, sound, volume, pitch);
                    }
                });
    }


    // 1.8.8 -> 1.16.5
    private static void playBlockPlaceSoundNMS(Player player, Block block) throws Exception {
        World world = block.getWorld();
        Object nmsWorld = world.getClass().getMethod("getHandle").invoke(world);
        Class<?> blockPositionClass = getBlockPositionClass();
        Object blockPosition = null;

        if (blockPositionClass != null)
            blockPosition = blockPositionClass.getConstructor(int.class, int.class, int.class).newInstance(block.getX(), block.getY(), block.getZ());

        Method getTypeMethod = nmsWorld.getClass().getMethod("getType", blockPositionClass);
        getTypeMethod.setAccessible(true);

        Object blockData = getTypeMethod.invoke(nmsWorld, blockPosition);
        Method getBlock = blockData.getClass().getMethod("getBlock");
        getBlock.setAccessible(true);

        Object nmsBlock = getBlock.invoke(blockData);
        Method getStepSound;
        final int serverProtocol = Via.getAPI().getServerVersion().lowestSupportedVersion();
        if (serverProtocol > ProtocolVersion.v1_8.getVersion() && serverProtocol < ProtocolVersion.v1_12.getVersion()) {
            getStepSound = getMethod(nmsBlock.getClass(), "w");
        } else if (serverProtocol > ProtocolVersion.v1_10.getVersion() && serverProtocol < ProtocolVersion.v1_13.getVersion()) {
            getStepSound = getMethod(nmsBlock.getClass(), "getStepSound");
        } else { // 1.14 - 1.16.5
            getStepSound = getMethod(nmsBlock.getClass(), "getStepSound", blockData.getClass());
        }
        if (getStepSound == null) {
            Via.getPlatform().getLogger().severe("Could not find getStepSound method in " + nmsBlock.getClass().getName());
            return;
        }

        getStepSound.setAccessible(true);
        Object soundType;
        if (getStepSound.getParameterCount() == 0) {
            soundType = getStepSound.invoke(nmsBlock); // 1.9 - 1.13
        } else {
            soundType = getStepSound.invoke(nmsBlock, blockData); // 1.14 - 1.16.5
        }

        Method soundEffectMethod;
        Method volumeMethod;
        Method pitchMethod;

        try {
            // 1.16.5
            soundEffectMethod = soundType.getClass().getMethod("getPlaceSound");
            volumeMethod = soundType.getClass().getMethod("getVolume");
            pitchMethod = soundType.getClass().getMethod("getPitch");
        } catch (NoSuchMethodException ex) {
            // 1.9 -> 1.16.4
            soundEffectMethod = soundType.getClass().getMethod("e");
            volumeMethod = soundType.getClass().getMethod("a");
            pitchMethod = soundType.getClass().getMethod("b");
        }

        Object soundEffect = soundEffectMethod.invoke(soundType);
        float volume = (float) volumeMethod.invoke(soundType);
        float pitch = (float) pitchMethod.invoke(soundType);
        Object soundCategory = Enum.valueOf(getSoundCategoryClass(), "BLOCKS");

        volume = (volume + 1.0f) / 2.0f;
        pitch *= 0.8;

        playSound(player, soundEffect, soundCategory, block.getX() + 0.5, block.getY() + 0.5, block.getZ() + 0.5, volume, pitch);
    }

    // 1.8.8 -> 1.16.5
    private static void playSound(Player player, Object soundEffect, Object soundCategory, double x, double y, double z, float volume, float pitch) {
        try {
            Object packet = getGamePacketClass("PacketPlayOutNamedSoundEffect").getConstructor(
                    soundEffect.getClass(), soundCategory.getClass(),
                    double.class, double.class, double.class,
                    float.class, float.class
            ).newInstance(
                    soundEffect, soundCategory,
                    x, y, z,
                    volume, pitch
            );

            // Volume = 1
            // Pitch = .8
            sendPacket(player, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
