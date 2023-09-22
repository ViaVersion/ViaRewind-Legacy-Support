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
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

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
                int serverProtocol = Via.getAPI().getServerVersion().lowestSupportedVersion();
                if (serverProtocol == -1) return;
                cancel();

                if (serverProtocol > 5 && config.getBoolean("enchanting-gui-fix")) Bukkit.getPluginManager().registerEvents(new EnchantingListener(), BukkitPlugin.this);
                if (serverProtocol > 78 && config.getBoolean("brewing-stand-gui-fix")) Bukkit.getPluginManager().registerEvents(new BrewingListener(), BukkitPlugin.this);
                if (serverProtocol > 84 && config.getBoolean("lily-pad-fix")) BoundingBoxFixer.fixLilyPad();
                if (serverProtocol > 404 && config.getBoolean("carpet-fix")) // 1.14+ only BoundingBoxFixer.fixCarpet();
                if (serverProtocol > 48 && config.getBoolean("ladder-fix")) BoundingBoxFixer.fixLadder();
                if (serverProtocol > 47 && config.getBoolean("sound-fix")) Bukkit.getPluginManager().registerEvents(new SoundListener(), BukkitPlugin.this);
                if (serverProtocol > 5 && config.getBoolean("slime-fix")) Bukkit.getPluginManager().registerEvents(new BounceListener(), BukkitPlugin.this);
                if (serverProtocol > 76 && config.getBoolean("elytra-fix")) Bukkit.getPluginManager().registerEvents(new ElytraListener(), BukkitPlugin.this);
                if (serverProtocol > 54 && config.getBoolean("area-effect-cloud-particles")) Bukkit.getPluginManager().registerEvents(new AreaEffectCloudListener(), BukkitPlugin.this);
                if (config.getBoolean("versioninfo.active")) new VersionInformer();
            }
        }.runTaskTimer(this, 1L, 1L);
    }

    public static BukkitPlugin getInstance() {
        return instance;
    }
}
