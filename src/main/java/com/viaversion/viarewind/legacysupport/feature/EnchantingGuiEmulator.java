/*
 * This file is part of ViaRewind-Legacy-Support - https://github.com/ViaVersion/ViaRewind-Legacy-Support
 * Copyright (C) 2018-2024 ViaVersion and contributors
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

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Map;

@SuppressWarnings("unchecked")
public class EnchantingGuiEmulator implements Listener {

    private final boolean newMaterialNames;
    private final Material lapisMaterial;

    public EnchantingGuiEmulator() {
        newMaterialNames = Material.getMaterial("LAPIS_LAZULI") != null;

        if (newMaterialNames) {
            lapisMaterial = Material.LAPIS_LAZULI;
        } else {
            lapisMaterial = Material.getMaterial("INK_SACK");
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (!(e.getInventory() instanceof EnchantingInventory)) return;

        final Player player = (Player) e.getPlayer();
        if (Via.getAPI().getPlayerProtocolVersion(player).newerThanOrEqualTo(ProtocolVersion.v1_8)) return;

        final PlayerInventory inv = player.getInventory();
        final ItemStack lapis = newMaterialNames ? new ItemStack(lapisMaterial) : new ItemStack(lapisMaterial, 1, (short) 4);

        int amount = 0;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);

            if (item == null || !item.isSimilar(lapis)) continue;

            if (amount + item.getAmount() > 64) {
                item.setAmount(amount + item.getAmount() - 64);
                amount = 64;
            } else {
                amount += item.getAmount();
                item = new ItemStack(Material.AIR);
            }

            inv.setItem(i, item);
            if (amount == 64) break;
        }

        if (amount == 0) return;

        final EnchantingInventory replacement = (EnchantingInventory) e.getInventory();
        lapis.setAmount(amount);
        replacement.setSecondary(lapis);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getInventory() instanceof EnchantingInventory)) return;

        final Player player = (Player) e.getPlayer();
        if (Via.getAPI().getPlayerProtocolVersion(player).newerThanOrEqualTo(ProtocolVersion.v1_8)) return;

        final PlayerInventory inv = player.getInventory();
        final EnchantingInventory replacement = (EnchantingInventory) e.getInventory();

        final ItemStack item = replacement.getSecondary();
        if (item == null || item.getType() == Material.AIR) return;

        replacement.setSecondary(new ItemStack(Material.AIR));

        Map<Integer, ItemStack> remaining = inv.addItem(item);
        if (!remaining.isEmpty()) {
            final Location location = player.getLocation();

            for (ItemStack value : remaining.values()) {
                player.getWorld().dropItem(location, value);
            }
        }
    }
}
