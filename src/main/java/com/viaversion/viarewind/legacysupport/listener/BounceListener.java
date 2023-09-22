package com.viaversion.viarewind.legacysupport.listener;

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
public class BounceListener implements Listener {

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if (e.getTo().getY() >= e.getFrom().getY()) return; // Only check upwards motion

		final Player player = e.getPlayer();
		if (Via.getAPI().getPlayerVersion(player) >= ProtocolVersion.v1_8.getVersion()) return; // Only apply for 1.7 and below players

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
