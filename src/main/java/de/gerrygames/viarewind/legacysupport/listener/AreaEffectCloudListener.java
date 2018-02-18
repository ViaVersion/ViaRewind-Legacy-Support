package de.gerrygames.viarewind.legacysupport.listener;

import de.gerrygames.viarewind.legacysupport.BukkitPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import us.myles.ViaVersion.api.Via;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

public class AreaEffectCloudListener implements Listener {
	private ArrayList<AreaEffectCloud> effectClouds = new ArrayList<>();

	public AreaEffectCloudListener() {
		Bukkit.getScheduler().runTaskTimer(BukkitPlugin.getInstance(), () -> {
			Set<Player> players = Bukkit.getOnlinePlayers()
					                      .stream()
					                      .filter(p -> Via.getAPI().getPlayerVersion(p) <= 54)
					                      .collect(Collectors.toSet());
			effectClouds.removeIf(e -> !e.isValid());
			effectClouds.forEach(cloud -> {
				World world = cloud.getWorld();
				Particle particle = cloud.getParticle();
				Location loc = cloud.getLocation();

				float radius = cloud.getRadius();
				
				float area = (float) Math.PI * radius * radius;

				for(int i = 0; i < area; i++) {
					float f1 = (float)Math.random() * 6.2831855F;
					float f2 = (float)Math.sqrt(Math.random()) * radius;
					float f3 = (float)Math.cos(f1) * f2;
					float f6 = (float)Math.sin(f1) * f2;
					if (particle == Particle.SPELL_MOB) {
						int color = cloud.getColor().asRGB();
						int r = color >> 16 & 255;
						int g = color >> 8 & 255;
						int b = color & 255;
						players.forEach(player -> {
							if (player.getWorld()!=loc.getWorld()) return;
							player.spawnParticle(particle, loc.getX() + f3, loc.getY(), loc.getZ() + f6, 0, r / 255f, g / 255f, b / 255f);
						});
					} else {
						//TODO particles with data
						//world.spawnParticle(particle, loc.getX() + f3, loc.getY(), loc.getZ() + f6, 1, (0.5 - Math.random()) * 0.15, 0.009999999776482582D, (0.5 - Math.random()) * 0.15d, );
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
