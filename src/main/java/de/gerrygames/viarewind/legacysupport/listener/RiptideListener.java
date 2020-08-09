package de.gerrygames.viarewind.legacysupport.listener;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.bukkit.util.Vector;
import us.myles.ViaVersion.api.Via;

public class RiptideListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPlayerRiptide(PlayerRiptideEvent event) {
        Player p = event.getPlayer();
        if (Via.getAPI().getPlayerVersion(p)>392) return;

        int level = event.getItem().getEnchantmentLevel(Enchantment.RIPTIDE);

        float yaw = p.getLocation().getYaw();
        float pitch = p.getLocation().getPitch();
        double x = -Math.sin(yaw * (0.017453292f)) * Math.cos(pitch * (0.017453292f));
        double y = -Math.sin(pitch * (0.017453292f));
        double z = Math.cos(yaw * (0.017453292f)) * Math.cos(pitch * (0.017453292f));
        double length = Math.sqrt(x * x + y * y + z * z);
        float f5 = 3.0F * ((1.0F + (float)level) / 4.0F);
        x = x * (f5 / length);
        y = y * (f5 / length);
        z = z * (f5 / length);

        p.setVelocity(new Vector(x, y, z));

        if (p.isOnGround()) {
            p.setVelocity(new Vector(0.0D, 1.1999999F, 0.0D));
        }

    }
}
