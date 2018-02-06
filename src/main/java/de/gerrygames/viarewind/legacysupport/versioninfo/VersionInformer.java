package de.gerrygames.viarewind.legacysupport.versioninfo;

import de.gerrygames.viarewind.legacysupport.BukkitPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import us.myles.ViaVersion.api.Via;

public class VersionInformer implements Listener {
	private String message;
	private int maxVersion;

	public VersionInformer() {
		message = BukkitPlugin.getInstance().getConfig().getString("versioninfo.message");
		message = ChatColor.translateAlternateColorCodes('&', message);
		message = message.replace("%version%", Bukkit.getVersion().split(" ")[2].replace(")", ""));

		maxVersion = BukkitPlugin.getInstance().getConfig().getInt("versioninfo.max-version");
		String interval = BukkitPlugin.getInstance().getConfig().getString("versioninfo.interval");
		if (interval.equalsIgnoreCase("JOIN")) {
			Bukkit.getPluginManager().registerEvents(this, BukkitPlugin.getInstance());
		} else {
			long ticks = Long.parseLong(interval);
			Bukkit.getScheduler().runTaskTimer(BukkitPlugin.getInstance(), () -> {
				Bukkit.getOnlinePlayers().forEach(player -> {
					int version = Via.getAPI().getPlayerVersion(player);
					if (version>maxVersion) return;
					player.sendMessage(message);
				});
			}, ticks, ticks);
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		int version = Via.getAPI().getPlayerVersion(e.getPlayer());
		if (version>maxVersion) return;
		e.getPlayer().sendMessage(message);
	}
}
