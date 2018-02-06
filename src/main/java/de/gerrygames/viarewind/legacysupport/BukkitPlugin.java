package de.gerrygames.viarewind.legacysupport;

import de.gerrygames.viarewind.legacysupport.injector.LilyPadFixer;
import de.gerrygames.viarewind.legacysupport.listener.BrewingListener;
import de.gerrygames.viarewind.legacysupport.listener.EnchantingListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitPlugin extends JavaPlugin {

	@Override
	public void onEnable() {
		saveDefaultConfig();
		FileConfiguration config = getConfig();
		if (config.getBoolean("enchanting-gui-fix")) Bukkit.getPluginManager().registerEvents(new EnchantingListener(), this);
		if (config.getBoolean("brewing-stand-gui-fix")) Bukkit.getPluginManager().registerEvents(new BrewingListener(), this);
		if (config.getBoolean("lily-pad-fix")) LilyPadFixer.fix();
	}
}
