package de.gerrygames.viarewind.legacysupport;

import de.gerrygames.viarewind.legacysupport.injector.LilyPadFixer;
import de.gerrygames.viarewind.legacysupport.listener.BrewingListener;
import de.gerrygames.viarewind.legacysupport.listener.EnchantingListener;
import de.gerrygames.viarewind.legacysupport.listener.SoundListener;
import de.gerrygames.viarewind.legacysupport.versioninfo.VersionInformer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;

public class BukkitPlugin extends JavaPlugin {
	private static BukkitPlugin instance;

	@Override
	public void onEnable() {
		instance = this;
		saveDefaultConfig();
		getConfig().options().copyDefaults(true);
		saveConfig();
		final FileConfiguration config = getConfig();
		Bukkit.getScheduler().runTask(this, () -> {
			if (ProtocolRegistry.SERVER_PROTOCOL>5 && config.getBoolean("enchanting-gui-fix"))
				Bukkit.getPluginManager().registerEvents(new EnchantingListener(), this);
			if (ProtocolRegistry.SERVER_PROTOCOL>78 && config.getBoolean("brewing-stand-gui-fix"))
				Bukkit.getPluginManager().registerEvents(new BrewingListener(), this);
			if (ProtocolRegistry.SERVER_PROTOCOL>84 && config.getBoolean("lily-pad-fix"))
				LilyPadFixer.fix();
			if (ProtocolRegistry.SERVER_PROTOCOL>47 && config.getBoolean("sound-fix"))
				Bukkit.getPluginManager().registerEvents(new SoundListener(), this);
			if (config.getBoolean("versioninfo.active")) new VersionInformer();
		});
	}

	public static BukkitPlugin getInstance() {
		return instance;
	}
}
