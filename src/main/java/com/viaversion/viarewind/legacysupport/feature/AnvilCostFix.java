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

import com.viaversion.viarewind.legacysupport.BukkitPlugin;
import com.viaversion.viarewind.legacysupport.util.NMSUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import java.util.logging.Level;

public class AnvilCostFix implements Listener {
    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();

        if (inventory instanceof AnvilInventory) {
            Bukkit.getScheduler().runTaskLater(BukkitPlugin.getInstance(), () -> {
                for (HumanEntity entity : inventory.getViewers()) {
                    if (entity instanceof Player) {
                        refreshAnvilInventoryPlayer((Player) entity);
                    }
                }
            }, 1L);
        }
    }

    public void refreshAnvilInventoryPlayer(Player player) {
        Object nmsPlayer = NMSUtil.getNMSPlayer(player);

        try {
            Object activeContainer = nmsPlayer.getClass().getField("activeContainer").get(nmsPlayer);

            if (activeContainer.getClass().getSimpleName().equals("ContainerAnvil")) {
                Object cost = activeContainer.getClass().getField("a").get(activeContainer);

                nmsPlayer.getClass().getMethod("setContainerData", activeContainer.getClass().getSuperclass(), int.class, int.class).invoke(nmsPlayer, activeContainer, cost, cost);
            }
        } catch (Exception e) {
            BukkitPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to refresh anvil inventory", e);
        }
    }
}
