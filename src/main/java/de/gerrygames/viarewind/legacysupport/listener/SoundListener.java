package de.gerrygames.viarewind.legacysupport.listener;

import com.viaversion.viaversion.api.Via;
import de.gerrygames.viarewind.legacysupport.BukkitPlugin;
import de.gerrygames.viarewind.legacysupport.injector.NMSReflection;
import de.gerrygames.viarewind.legacysupport.reflection.MethodSignature;
import de.gerrygames.viarewind.legacysupport.reflection.ReflectionAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import java.lang.reflect.Method;

public class SoundListener implements Listener {
	private static boolean isSoundCategory = false;
	static {
		try {
			Class.forName("org.bukkit.SoundCategory");
			isSoundCategory = true;
		} catch (ClassNotFoundException ignored) {}
	}

	public SoundListener() {
		try {
			Class.forName("org.bukkit.event.entity.EntityPickupItemEvent");

			Bukkit.getPluginManager().registerEvents(new Listener() {

				@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
				public void onItemPickUp(EntityPickupItemEvent e) {
					if (!(e.getEntity() instanceof Player)) return;
					SoundListener.this.onItemPickUp((Player) e.getEntity());
				}

			}, BukkitPlugin.getInstance());
		} catch (Exception ex) {
			Bukkit.getPluginManager().registerEvents(new Listener() {

				@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
				public void onItemPickUp(PlayerPickupItemEvent e) {
					SoundListener.this.onItemPickUp(e.getPlayer());
				}

			}, BukkitPlugin.getInstance());
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		Player player = e.getPlayer();
		if (Via.getAPI().getPlayerVersion(player)>47) return;
		playBlockPlaceSound(player, e.getBlock());
	}

	private void onItemPickUp(Player player) {
		float volume = 0.2f;
		float pitch = (float) ((Math.random() - Math.random()) * 0.7f + 1.0f) * 2.0f;
		Location loc = player.getLocation();
		playSound(loc, Sound.ENTITY_ITEM_PICKUP, "PLAYERS", volume, pitch, 16, 47);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onExperienceOrbPickup(PlayerExpChangeEvent e) {
		float volume = 0.1f;
		float pitch = (float) (0.5f * ((Math.random() - Math.random()) * 0.7f + 1.8f));
		playSound(e.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, "PLAYERS", volume, pitch, 16, 47);
	}

	private static void playSound(Location loc, Sound sound, String category, float volume, float pitch, double dist, int version) {
		Bukkit.getOnlinePlayers().stream()
				.filter(p -> p.getWorld()==loc.getWorld())
				.filter(p -> p.getLocation().distanceSquared(loc) < dist * dist)
				.filter(p -> Via.getAPI().getPlayerVersion(p) <= version)
				.forEach(p -> {
					if (isSoundCategory) {
						p.playSound(loc, sound, SoundCategory.valueOf(category), volume, pitch);
					} else {
						p.playSound(loc, sound, volume, pitch);
					}
				});
	}

	private static void playBlockPlaceSound(Player player, Block block) {
		try {
			World world = block.getWorld();
			Object nmsWorld = world.getClass().getMethod("getHandle").invoke(world);
			Class<?> blockPositionClass = NMSReflection.getBlockPositionClass();
			Object blockPosition = null;
			if (blockPositionClass != null) {
				blockPosition = blockPositionClass.getConstructor(int.class, int.class, int.class).newInstance(block.getX(), block.getY(), block.getZ());
			}
			Method getTypeMethod = ReflectionAPI.pickMethod(
					nmsWorld.getClass(),
					new MethodSignature("getType", blockPositionClass),
					new MethodSignature("a_", blockPositionClass) // 1.18.2
			);
			Object blockData = getTypeMethod.invoke(nmsWorld, blockPosition);
			Method getBlock = ReflectionAPI.pickMethod(
					blockData.getClass(),
					new MethodSignature("getBlock"),
					new MethodSignature("b") // 1.18.2
			);
			getBlock.setAccessible(true);
			Object nmsBlock = getBlock.invoke(blockData);
			Method getStepSound = ReflectionAPI.pickMethod(
					nmsBlock.getClass(),
                    new MethodSignature("m", blockData.getClass()), // 1.18.2
                    new MethodSignature("w"), // 1.18?
					new MethodSignature("getStepSound", blockData.getClass()), // 1.17
					new MethodSignature("getStepSound") // pre 1.17
			);

			Object soundType;
			if (getStepSound.getParameterCount() == 0) {
				soundType = getStepSound.invoke(nmsBlock);
			} else {
				soundType = getStepSound.invoke(nmsBlock, blockData);
			}

			Method soundEffectMethod;
			Method volumeMethod;
			Method pitchMethod;

			try {
				soundEffectMethod = soundType.getClass().getMethod("getPlaceSound");
				volumeMethod = soundType.getClass().getMethod("getVolume");
				pitchMethod = soundType.getClass().getMethod("getPitch");
			} catch (NoSuchMethodException ex) {
				soundEffectMethod = soundType.getClass().getMethod("e");
				volumeMethod = soundType.getClass().getMethod("a");
				pitchMethod = soundType.getClass().getMethod("b");
			}

			Object soundEffect = soundEffectMethod.invoke(soundType);
			float volume = (float) volumeMethod.invoke(soundType);
			float pitch = (float) pitchMethod.invoke(soundType);
			Object soundCategory = Enum.valueOf(NMSReflection.getSoundCategoryClass(), "BLOCKS");

			volume = (volume + 1.0f) / 2.0f;
			pitch *= 0.8;

			playSound(player, soundEffect, soundCategory, block.getX() + 0.5, block.getY() + 0.5, block.getZ() + 0.5, volume, pitch, world.getSeed());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void playSound(Player player, Object soundEffect, Object soundCategory, double x, double y, double z, float volume, float pitch, long seed) {
		try {
			Object packet = NMSReflection.getGamePacketClass("PacketPlayOutNamedSoundEffect").getConstructor(
					soundEffect.getClass(), soundCategory.getClass(),
					double.class, double.class, double.class,
					float.class, float.class
			).newInstance(
					soundEffect, soundCategory,
					x, y, z,
					volume, pitch
			);

			NMSReflection.sendPacket(player, packet);
		} catch (Exception ex) {
			try {
				Object packet = NMSReflection.getGamePacketClass("PacketPlayOutNamedSoundEffect").getConstructor(
						soundEffect.getClass(), soundCategory.getClass(),
						double.class, double.class, double.class,
						float.class, float.class, long.class
				).newInstance(
						soundEffect, soundCategory,
						x, y, z,
						volume, pitch, seed
				);

				NMSReflection.sendPacket(player, packet);
			} catch (Exception ex2) {
				ex.printStackTrace();
			}
		}
	}
}
