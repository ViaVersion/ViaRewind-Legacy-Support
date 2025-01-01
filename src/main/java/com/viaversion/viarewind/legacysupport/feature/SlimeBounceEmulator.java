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

package com.viaversion.viarewind.legacysupport.feature;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

@SuppressWarnings({"unchecked", "DataFlowIssue"})
public class SlimeBounceEmulator implements Listener {

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if (e.getTo().getY() >= e.getFrom().getY()) return; // Only check upwards motion

		final Player player = e.getPlayer();
		if (Via.getAPI().getPlayerProtocolVersion(player).newerThanOrEqualTo(ProtocolVersion.v1_8)) return; // Only apply for 1.7 and below players

		if (Math.floor(e.getTo().getY()) + 0.01 < e.getTo().getY()) return;
		if (player.isSneaking()) return;

		final Block block = e.getTo().clone().add(0, -0.1, 0).getBlock();
		if (block.getType() != Material.SLIME_BLOCK) return;

		final Vector newVelocity = player.getVelocity();

		final double deltaY = (e.getTo().getY() - e.getFrom().getY());
		if (deltaY >- 0.11) return; // Only bounce if the player is falling down

		newVelocity.setY(-deltaY * 1.05);

		player.setVelocity(newVelocity);
	}
}
