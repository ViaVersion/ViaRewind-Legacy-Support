package com.viaversion.viarewind.legacysupport;

import com.viaversion.viarewind.legacysupport.injector.BoundingBoxFixer;
import com.viaversion.viarewind.legacysupport.versioninfo.VersionInformer;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viarewind.legacysupport.listener.AreaEffectCloudListener;
import com.viaversion.viarewind.legacysupport.listener.BounceListener;
import com.viaversion.viarewind.legacysupport.listener.BrewingListener;
import com.viaversion.viarewind.legacysupport.listener.ElytraListener;
import com.viaversion.viarewind.legacysupport.listener.EnchantingListener;
import com.viaversion.viarewind.legacysupport.listener.SoundListener;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class BukkitPlugin extends JavaPlugin {
    private static BukkitPlugin instance;

    @Override
    public void onLoad() {
        instance = this;
    }

    protected void makeConfig() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    @Override
    public void onEnable() {
        makeConfig();

        final FileConfiguration config = getConfig();

        new BukkitRunnable() {
            @Override
            public void run() {
                int serverProtocol = Via.getAPI().getServerVersion().lowestSupportedVersion();
                if (serverProtocol == -1) return;
                cancel();

                if (serverProtocol >= ProtocolVersion.v1_8.getVersion() && config.getBoolean("enchanting-gui-fix")) {
                    Bukkit.getPluginManager().registerEvents(new EnchantingListener(), BukkitPlugin.this);
                }
                if (serverProtocol >= ProtocolVersion.v1_8.getVersion() && config.getBoolean("slime-fix")) {
                    Bukkit.getPluginManager().registerEvents(new BounceListener(), BukkitPlugin.this);
                }
                if (serverProtocol >= ProtocolVersion.v1_9.getVersion() && config.getBoolean("sound-fix")) {
                    Bukkit.getPluginManager().registerEvents(new SoundListener(), BukkitPlugin.this);
                }
                // Added in 15w31a (1.9)
                if (serverProtocol >= ProtocolVersion.v1_9.getVersion() && config.getBoolean("ladder-fix")) {
                    BoundingBoxFixer.fixLadder(serverProtocol);
                }
                // Added in 15w32c (1.9)
                if (serverProtocol >= ProtocolVersion.v1_9.getVersion() && config.getBoolean("area-effect-cloud-particles")) {
                    Bukkit.getPluginManager().registerEvents(new AreaEffectCloudListener(), BukkitPlugin.this);
                }
                // Added in 15w40b (1.9)
                if (serverProtocol > ProtocolVersion.v1_9.getVersion() && config.getBoolean("elytra-fix")) {
                    Bukkit.getPluginManager().registerEvents(new ElytraListener(), BukkitPlugin.this);
                }
                // Added in 15w41b (1.9)
                if (serverProtocol >= ProtocolVersion.v1_9.getVersion() && config.getBoolean("brewing-stand-gui-fix")) {
                    Bukkit.getPluginManager().registerEvents(new BrewingListener(), BukkitPlugin.this);
                }
                // Added in 15w44b (1.9)
                if (serverProtocol >= ProtocolVersion.v1_9.getVersion() && config.getBoolean("lily-pad-fix")) {
                    BoundingBoxFixer.fixLilyPad();
                }
                if (serverProtocol >= ProtocolVersion.v1_14_4.getVersion() && config.getBoolean("carpet-fix")) {
                    BoundingBoxFixer.fixCarpet(serverProtocol);
                }

                if (config.getBoolean("versioninfo.active")) {
                    new VersionInformer();
                }
            }
        }.runTaskTimer(this, 1L, 1L);
    }

    public static BukkitPlugin getInstance() {
        return instance;
    }
}
