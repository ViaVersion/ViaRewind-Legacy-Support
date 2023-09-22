package com.viaversion.viarewind.legacysupport.listener;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viarewind.legacysupport.BukkitPlugin;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.LingeringPotionSplashEvent;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class AreaEffectCloudListener implements Listener {
    private final ArrayList<AreaEffectCloud> effectClouds = new ArrayList<>();

    public AreaEffectCloudListener() {
        Bukkit.getScheduler().runTaskTimer(BukkitPlugin.getInstance(), () -> {
            final Set<Player> affectedPlayers = Bukkit.getOnlinePlayers().stream().filter(p -> Via.getAPI().getPlayerVersion(p) <= ProtocolVersion.v1_8.getVersion()).collect(Collectors.toSet());
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
