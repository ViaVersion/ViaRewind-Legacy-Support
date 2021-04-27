package de.gerrygames.viarewind.legacysupport.listener;

import com.viaversion.viaversion.api.Via;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class BounceListener implements Listener {

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if (e.getTo().getY()>=e.getFrom().getY()) return;
		Player player = e.getPlayer();
		if (Via.getAPI().getPlayerVersion(player)>5) return;
		if (Math.floor(e.getTo().getY()) + 0.01 < e.getTo().getY()) return;
		if (player.isSneaking()) return;
		Block block = e.getTo().clone().add(0, -0.1, 0).getBlock();
		if (block.getType()!=Material.SLIME_BLOCK) return;
		Vector velocity = player.getVelocity();
		double motY = (e.getTo().getY()-e.getFrom().getY());
		if (motY>-0.11) return;
		velocity.setY(-motY * 1.05);
		player.setVelocity(velocity);
	}
}