package de.gerrygames.viarewind.legacysupport.injector;

import org.bukkit.Bukkit;

public class NMSReflection {
	private static String version;

	public static String getVersion() {
		return version==null ? version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] : version;
	}

	public static Class getNMSClass(String name) {
		try {
			return Class.forName("net.minecraft.server." + NMSReflection.getVersion() + "." + name);
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		return null;
	}
}
