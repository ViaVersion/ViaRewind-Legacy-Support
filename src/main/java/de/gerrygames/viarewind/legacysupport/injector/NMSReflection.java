package de.gerrygames.viarewind.legacysupport.injector;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class NMSReflection {
	private static String version;

	public static String getVersion() {
		return version==null ? version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] : version;
	}

	public static Class getNMSClass(String name) {
		try {
			return Class.forName("net.minecraft.server." + getVersion() + "." + name);
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static Class getCraftBukkitClass(String name) {
		try {
			return Class.forName("org.bukkit.craftbukkit." + getVersion() + "." + name);
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static void sendPacket(Player player, Object packet) {
		try {
			Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
			Object playerConnection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
			playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
