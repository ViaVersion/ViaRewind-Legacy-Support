package de.gerrygames.viarewind.legacysupport.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import us.myles.ViaVersion.api.Via;

public class EnchantingListener implements Listener {

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent e) {
		if (!(e.getInventory() instanceof EnchantingInventory)) return;
		Player player = (Player) e.getPlayer();
		if (Via.getAPI().getPlayerVersion(player)>5) return;
		PlayerInventory playerInventory = player.getInventory();
		ItemStack lapis = new ItemStack(Material.INK_SACK, 1, (short) 4);
		int amount = 0;
		for (int i = 0; i<playerInventory.getSize(); i++) {
			ItemStack item = playerInventory.getItem(i);
			if (item==null || !item.isSimilar(lapis)) continue;
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
		EnchantingInventory inventory = (EnchantingInventory) e.getInventory();
		lapis.setAmount(amount);
		inventory.setSecondary(lapis);
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		if (!(e.getInventory() instanceof EnchantingInventory)) return;
		Player player = (Player) e.getPlayer();
		int version = Via.getAPI().getPlayerVersion(player);
		if (version>5) return;
		PlayerInventory playerInventory = player.getInventory();
		EnchantingInventory inventory = (EnchantingInventory) e.getInventory();
		ItemStack item = inventory.getSecondary();
		if (item==null || item.getType()==Material.AIR) return;
		inventory.setSecondary(new ItemStack(Material.AIR));
		playerInventory.addItem(item);
	}
}
