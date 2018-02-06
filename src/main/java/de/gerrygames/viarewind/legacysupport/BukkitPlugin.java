package de.gerrygames.viarewind.legacysupport;

import de.gerrygames.viarewind.legacysupport.injector.LilyPadFixer;
import de.gerrygames.viarewind.legacysupport.listener.BrewingListener;
import de.gerrygames.viarewind.legacysupport.listener.EnchantingListener;
import de.gerrygames.viarewind.legacysupport.versioninfo.VersionInformer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitPlugin extends JavaPlugin {
	private static BukkitPlugin instance;

	@Override
	public void onEnable() {
		instance = this;
		saveDefaultConfig();
		getConfig().options().copyDefaults(true);
		saveConfig();
		FileConfiguration config = getConfig();
		if (config.getBoolean("enchanting-gui-fix")) Bukkit.getPluginManager().registerEvents(new EnchantingListener(), this);
		if (config.getBoolean("brewing-stand-gui-fix")) Bukkit.getPluginManager().registerEvents(new BrewingListener(), this);
		if (config.getBoolean("lily-pad-fix")) LilyPadFixer.fix();
		if (config.getBoolean("versioninfo.active")) new VersionInformer();
	}

	public static BukkitPlugin getInstance() {
		return instance;
	}
}
