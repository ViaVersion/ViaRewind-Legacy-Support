package de.gerrygames.viarewind.legacysupport;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.version.ServerProtocolVersion;
import de.gerrygames.viarewind.legacysupport.injector.BoundingBoxFixer;
import de.gerrygames.viarewind.legacysupport.listener.AreaEffectCloudListener;
import de.gerrygames.viarewind.legacysupport.listener.BounceListener;
import de.gerrygames.viarewind.legacysupport.listener.BrewingListener;
import de.gerrygames.viarewind.legacysupport.listener.ElytraListener;
import de.gerrygames.viarewind.legacysupport.listener.EnchantingListener;
import de.gerrygames.viarewind.legacysupport.listener.SoundListener;
import de.gerrygames.viarewind.legacysupport.versioninfo.VersionInformer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Level;

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
				ServerProtocolVersion version = Via.getAPI().getServerVersion();
				if (!version.isKnown()) {
					return;
				}

				int serverProtocol = version.lowestSupportedVersion();
				cancel();
				if (serverProtocol > 5 && config.getBoolean("enchanting-gui-fix"))
					Bukkit.getPluginManager().registerEvents(new EnchantingListener(), BukkitPlugin.this);
				if (serverProtocol > 78 && config.getBoolean("brewing-stand-gui-fix"))
					Bukkit.getPluginManager().registerEvents(new BrewingListener(), BukkitPlugin.this);
				if (serverProtocol > 84 && config.getBoolean("lily-pad-fix"))
					BoundingBoxFixer.fixLilyPad();
				if (serverProtocol > 48 && config.getBoolean("ladder-fix"))
					BoundingBoxFixer.fixLadder();
				if (serverProtocol > 47 && config.getBoolean("sound-fix")) {
					try {
						Bukkit.getPluginManager().registerEvents(new SoundListener(), BukkitPlugin.this);
					} catch (ClassNotFoundException | NoSuchMethodException e) {
						getLogger().log(Level.SEVERE, "Could not load sound fix - please report this on our GitHub", e);
					}
				}
				if (serverProtocol > 5 && config.getBoolean("slime-fix"))
					Bukkit.getPluginManager().registerEvents(new BounceListener(), BukkitPlugin.this);
				if (serverProtocol > 76 && config.getBoolean("elytra-fix"))
					Bukkit.getPluginManager().registerEvents(new ElytraListener(), BukkitPlugin.this);
				if (serverProtocol > 54 && config.getBoolean("area-effect-cloud-particles"))
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
