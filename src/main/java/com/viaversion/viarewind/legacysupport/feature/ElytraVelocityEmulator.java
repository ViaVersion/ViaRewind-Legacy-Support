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

package com.viaversion.viarewind.legacysupport.feature;

import com.viaversion.viarewind.legacysupport.BukkitPlugin;
import com.viaversion.viarewind.legacysupport.util.NMSUtil;
import com.viaversion.viarewind.legacysupport.util.ReflectionUtil;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.lang.reflect.Method;
import java.util.logging.Level;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

@SuppressWarnings("unchecked")
public class ElytraVelocityEmulator implements Listener {

    private boolean isGliding(final ProtocolVersion version, final Player player) {
        if (version.olderThanOrEqualTo(ProtocolVersion.v1_9_1)) {
            final Object nmsPlayer = NMSUtil.getNMSPlayer(player);
            if (nmsPlayer == null) return false;

            try {
                final Method getFlag = ReflectionUtil.getMethod(nmsPlayer.getClass(), "getFlag", int.class);
                return (boolean) getFlag.invoke(nmsPlayer, 7);
            } catch (Exception e) {
                BukkitPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to get player flag", e);
            }
        }
        return player.isGliding();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent e) {
        final Player player = e.getPlayer();

        if (Via.getAPI().getPlayerProtocolVersion(player).newerThanOrEqualTo(ProtocolVersion.v1_9)) {
            return; // Only apply for 1.8 and below players
        }
        final ProtocolVersion serverVersion = BukkitPlugin.getInstance().getServerProtocol();
        if (!isGliding(serverVersion, player)) {
            return; // Only apply if the player is gliding
        }

        final Vector direction = player.getLocation().getDirection();
        final Vector velocity = player.getVelocity();

        double motionX = velocity.getX();
        double motionY = velocity.getY();
        double motionZ = velocity.getZ();

        float pitch = player.getLocation().getPitch() * 0.017453292F;
        double directionH = Math.sqrt(direction.getX() * direction.getX() + direction.getZ() * direction.getZ());
        double speedH = Math.sqrt(motionX * motionX + motionZ * motionZ);
        float speedV = (float) Math.cos(pitch);

        speedV = (float) (speedV * speedV * Math.min(1.0D, direction.length() / 0.4D));
        motionY += -0.08D + speedV * 0.06D;

        if ((motionY < 0.0D) && (directionH > 0.0D)) {
            double d2 = motionY * -0.1D * speedV;
            motionY += d2;
            motionX += direction.getX() * d2 / directionH;
            motionZ += direction.getZ() * d2 / directionH;
        }

        if (pitch < 0.0F) {
            double speed = speedH * -Math.sin(pitch) * 0.04D;
            motionY += speed * 3.2D;
            motionX -= direction.getX() * speed / directionH;
            motionZ -= direction.getZ() * speed / directionH;
        }

        if (directionH > 0.0D) {
            motionX += (direction.getX() / directionH * speedH - motionX) * 0.1D;
            motionZ += (direction.getZ() / directionH * speedH - motionZ) * 0.1D;
        }

        motionX *= 0.9900000095367432D;
        motionY *= 0.9800000190734863D;
        motionZ *= 0.9900000095367432D;

        velocity.setX(motionX);
        velocity.setY(motionY);
        velocity.setZ(motionZ);

        player.setVelocity(velocity);
    }
}
