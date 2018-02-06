package de.gerrygames.viarewind.legacysupport.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import us.myles.ViaVersion.api.Via;

public class BrewingListener implements Listener {

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent e) {
		if (!(e.getInventory() instanceof BrewerInventory)) return;
		Player player = (Player) e.getPlayer();
		int version = Via.getAPI().getPlayerVersion(player);
		if (version>79) return;
		PlayerInventory playerInventory = player.getInventory();
		ItemStack blazePowder = new ItemStack(Material.BLAZE_POWDER);
		int amount = 0;
		for (int i = 0; i<playerInventory.getSize(); i++) {
			ItemStack item = playerInventory.getItem(i);
			if (item==null || !item.isSimilar(blazePowder)) continue;
			if (amount + item.getAmount() > 64) {
				item.setAmount(amount + item.getAmount() - 64);
				amount = 64;
			} else {
				amount += item.getAmount();
				item = new ItemStack(Material.AIR);
			}
			playerInventory.setItem(i, item);
			if (amount==64) break;
		}
		if (amount==0) return;
		BrewerInventory inventory = (BrewerInventory) e.getInventory();
		blazePowder.setAmount(amount);
		inventory.setFuel(blazePowder);
	}
}
