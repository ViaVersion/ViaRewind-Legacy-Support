package de.gerrygames.viarewind.legacysupport.listener;

import com.viaversion.viaversion.api.Via;
import org.bukkit.Material;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class BrewingListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (!e.hasBlock() || e.getClickedBlock().getType()!=Material.BREWING_STAND) return;
		Player player = (Player) e.getPlayer();
		int version = Via.getAPI().getPlayerVersion(player);
		if (version>79) return;
		ItemStack blazePowder = new ItemStack(Material.BLAZE_POWDER);
		ItemStack playerItem = e.getItem();
		if (playerItem==null) playerItem = new ItemStack(Material.AIR);
		BrewingStand brewingStand = (BrewingStand)e.getClickedBlock().getState();
		BrewerInventory inventory = brewingStand.getInventory();
		ItemStack fuel = inventory.getFuel();
		if (fuel==null) fuel = new ItemStack(Material.AIR);

		if (e.getAction()==Action.RIGHT_CLICK_BLOCK) {
			if (!blazePowder.isSimilar(playerItem)) return;
			if (fuel.getType()!=Material.AIR && !fuel.isSimilar(playerItem)) return;
			if (fuel.getAmount()>=64) return;
			int amount = player.isSneaking() ? Math.min(playerItem.getAmount(), 64-fuel.getAmount()) : 1;
			if (playerItem.getAmount()==amount) {
				playerItem = new ItemStack(Material.AIR);
			} else {
				playerItem.setAmount(playerItem.getAmount()-amount);
			}
			if (fuel.getType()==Material.AIR) {
				fuel = new ItemStack(Material.BLAZE_POWDER, amount);
			} else {
				fuel.setAmount(fuel.getAmount()+amount);
			}
			inventory.setFuel(fuel);
		} else {
			if (!blazePowder.isSimilar(fuel)) return;
			if (!blazePowder.isSimilar(playerItem) && playerItem.getType()!=Material.AIR) return;
			if (playerItem.getAmount()>=64) return;
			int amount = player.isSneaking() ? Math.min(fuel.getAmount(), 64-playerItem.getAmount()) : 1;
			if (fuel.getAmount()==amount) {
				fuel = new ItemStack(Material.AIR);
			} else {
				fuel.setAmount(fuel.getAmount()-amount);
			}
			if (playerItem.getType()==Material.AIR) {
				playerItem = new ItemStack(Material.BLAZE_POWDER, amount);
			} else {
				playerItem.setAmount(playerItem.getAmount()+amount);
			}
			inventory.setFuel(fuel);
		}
		if (e.getHand()==EquipmentSlot.HAND) {
			player.getInventory().setItemInMainHand(playerItem);
		} else {
			player.getInventory().setItemInOffHand(playerItem);
		}
		e.setCancelled(true);
	}
}
