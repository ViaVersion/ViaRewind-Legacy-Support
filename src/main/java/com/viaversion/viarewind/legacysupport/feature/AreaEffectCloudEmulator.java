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
import org.bukkit.Particle;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.LingeringPotionSplashEvent;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class AreaEffectCloudEmulator implements Listener {
    private final ArrayList<AreaEffectCloud> effectClouds = new ArrayList<>();

    public AreaEffectCloudEmulator(final BukkitPlugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            final Set<Player> affectedPlayers = Bukkit.getOnlinePlayers().stream().filter(p -> Via.getAPI().getPlayerProtocolVersion(p).olderThanOrEqualTo(ProtocolVersion.v1_8)).collect(Collectors.toSet());
            effectClouds.removeIf(e -> !e.isValid());
            effectClouds.forEach(cloud -> {
                final Location location = cloud.getLocation();
                final float radius = cloud.getRadius();

                float area = (float) Math.PI * radius * radius;

                for (int i = 0; i < area; i++) {
                    float f1 = (float) Math.random() * 6.2831855F;
                    float f2 = (float) Math.sqrt(Math.random()) * radius;
                    float f3 = (float) Math.cos(f1) * f2;
                    float f6 = (float) Math.sin(f1) * f2;

                    if (cloud.getParticle() == Particle.SPELL_MOB) {
                        final int color = cloud.getColor().asRGB();

                        final int r = color >> 16 & 255;
                        final int g = color >> 8 & 255;
                        final int b = color & 255;

                        affectedPlayers.stream().filter(player -> player.getWorld() == location.getWorld()).forEach(player -> {

                            player.spawnParticle(
                                    cloud.getParticle(),
                                    location.getX() + f3, location.getY(), location.getZ() + f6,
                                    0,
                                    r / 255f, g / 255f, b / 255f
                            );
                        });
                    }
                }
            });
        }, 1L, 1L);
    }

    @EventHandler
    public void onEntitySpawn(LingeringPotionSplashEvent e) {
        effectClouds.add(e.getAreaEffectCloud());
    }
}
