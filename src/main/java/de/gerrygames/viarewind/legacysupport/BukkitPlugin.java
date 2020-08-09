package de.gerrygames.viarewind.legacysupport;

import de.gerrygames.viarewind.legacysupport.injector.BoundingBoxFixer;
import de.gerrygames.viarewind.legacysupport.listener.*;
import de.gerrygames.viarewind.legacysupport.versioninfo.VersionInformer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
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
		new BukkitRunnable() {
			@Override
			public void run() {
				if (ProtocolRegistry.SERVER_PROTOCOL == -1) return;
				cancel();
				if (ProtocolRegistry.SERVER_PROTOCOL > 5 && config.getBoolean("enchanting-gui-fix"))
					Bukkit.getPluginManager().registerEvents(new EnchantingListener(), BukkitPlugin.this);
				if (ProtocolRegistry.SERVER_PROTOCOL > 78 && config.getBoolean("brewing-stand-gui-fix"))
					Bukkit.getPluginManager().registerEvents(new BrewingListener(), BukkitPlugin.this);
				if (ProtocolRegistry.SERVER_PROTOCOL > 84 && config.getBoolean("lily-pad-fix"))
					BoundingBoxFixer.fixLilyPad();
				if (ProtocolRegistry.SERVER_PROTOCOL > 48 && config.getBoolean("ladder-fix"))
					BoundingBoxFixer.fixLadder();
				if (ProtocolRegistry.SERVER_PROTOCOL > 47 && config.getBoolean("sound-fix"))
					Bukkit.getPluginManager().registerEvents(new SoundListener(), BukkitPlugin.this);
				if (ProtocolRegistry.SERVER_PROTOCOL > 5 && config.getBoolean("slime-fix"))
					Bukkit.getPluginManager().registerEvents(new BounceListener(), BukkitPlugin.this);
				if (ProtocolRegistry.SERVER_PROTOCOL > 76 && config.getBoolean("elytra-fix"))
					Bukkit.getPluginManager().registerEvents(new ElytraListener(), BukkitPlugin.this);
				if (ProtocolRegistry.SERVER_PROTOCOL > 392 && config.getBoolean("riptide-fix"))
					Bukkit.getPluginManager().registerEvents(new RiptideListener(), BukkitPlugin.this);
				if (ProtocolRegistry.SERVER_PROTOCOL > 54 && config.getBoolean("area-effect-cloud-particles"))
					Bukkit.getPluginManager().registerEvents(new AreaEffectCloudListener(), BukkitPlugin.this);
				if (config.getBoolean("versioninfo.active"))
					new VersionInformer();
			}
		}.runTaskTimer(this, 1L, 1L);
	}

	public static BukkitPlugin getInstance() {
		return instance;
	}
}
