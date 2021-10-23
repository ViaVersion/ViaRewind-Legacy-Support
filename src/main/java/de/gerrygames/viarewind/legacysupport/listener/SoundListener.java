package de.gerrygames.viarewind.legacysupport.listener;

import com.viaversion.viaversion.api.Via;
import de.gerrygames.viarewind.legacysupport.BukkitPlugin;
import de.gerrygames.viarewind.legacysupport.injector.NMSReflection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.SoundGroup;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class SoundListener implements Listener {
	private static boolean isSoundCategory = false;
	static {
		try {
			Class.forName("org.bukkit.SoundCategory");
			isSoundCategory = true;
		} catch (ClassNotFoundException ignored) {}
	}

	private static final String SOUND_CATEGORY_BLOCKS = "BLOCKS";
	private static final String SOUND_CATEGORY_PLAYERS = "PLAYERS";

	private Object soundCategory;
	private Constructor soundPacketConstructor;

	private Method getCraftBlockToNMSBlockStateMethod, blockStateToBlockMethod;

	private Method getStepSoundMethod;

	private Method bukkitSoundGroupMethod;
	private Method bukkitSoundMethod;

	private Method soundEffectMethod;
	private Method volumeMethod;
	private Method pitchMethod;

	public SoundListener() throws ClassNotFoundException, NoSuchMethodException {
		Class<?> craftBlockClass = NMSReflection.obc("block.CraftBlock");

		try {
			getCraftBlockToNMSBlockStateMethod = craftBlockClass.getMethod("getNMS");
		} catch (NoSuchMethodException exception) {
			getCraftBlockToNMSBlockStateMethod = craftBlockClass.getMethod("getData0");
			getCraftBlockToNMSBlockStateMethod.setAccessible(true);
		}
		Class<?> blockDataClass = getCraftBlockToNMSBlockStateMethod.getReturnType();
		blockStateToBlockMethod = blockDataClass.getMethod("getBlock");

		Class<?> nmsBlockClass = blockStateToBlockMethod.getReturnType();

		try {
			getStepSoundMethod = nmsBlockClass.getMethod("getStepSound", blockDataClass);
		} catch (NoSuchMethodException ex) {
			try {
				getStepSoundMethod = nmsBlockClass.getMethod("getStepSound");
			} catch (NoSuchMethodException ex2) {
				getStepSoundMethod = nmsBlockClass.getMethod("w");
			}
		}

		Class<?> soundEffectTypeClass = getStepSoundMethod.getReturnType();
		try {
			Class<?> soundGroupClass = NMSReflection.obc("CraftSoundGroup");
			bukkitSoundGroupMethod = soundGroupClass.getDeclaredMethod("getSoundGroup", soundEffectTypeClass);
		} catch (Exception ex) {
			try {
				soundEffectMethod = soundEffectTypeClass.getMethod("getPlaceSound");
				volumeMethod = soundEffectTypeClass.getMethod("getVolume");
				pitchMethod = soundEffectTypeClass.getMethod("getPitch");
			} catch (NoSuchMethodException ex3) {
				soundEffectMethod = soundEffectTypeClass.getMethod("e");
				volumeMethod = soundEffectTypeClass.getMethod("a");
				pitchMethod = soundEffectTypeClass.getMethod("b");
			}

			try {
				bukkitSoundMethod = NMSReflection.getBukkitSoundMethod();
			} catch (Exception ex2) {
				Class soundCategoryClass = NMSReflection.nms("SoundCategory", "net.minecraft.sounds.SoundCategory");
				this.soundCategory = Enum.valueOf(soundCategoryClass, "BLOCKS");

				Class<?> soundPacketClass = NMSReflection.getGamePacketClass("PacketPlayOutNamedSoundEffect");
				if (soundPacketClass == null) {
					throw new ClassNotFoundException("Failed to find PacketPlayOutNamedSoundEffect class");
				}

				soundPacketConstructor = soundPacketClass.getConstructor(
						soundEffectMethod.getReturnType(), soundCategory.getClass(),
						double.class, double.class, double.class,
						float.class, float.class
				);
			}
		}

		registerListener();
	}

	private void registerListener() {
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
		if (Via.getAPI().getPlayerVersion(player) > 47) return;
		playBlockPlaceSound(player, e.getBlock());
	}

	private void onItemPickUp(Player player) {
		float volume = 0.2f;
		float pitch = (float) ((Math.random() - Math.random()) * 0.7f + 1.0f) * 2.0f;
		Location loc = player.getLocation();
		playSound(loc, Sound.ENTITY_ITEM_PICKUP, SOUND_CATEGORY_PLAYERS, volume, pitch, 16, 47);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onExperienceOrbPickup(PlayerExpChangeEvent e) {
		float volume = 0.1f;
		float pitch = (float) (0.5f * ((Math.random() - Math.random()) * 0.7f + 1.8f));
		playSound(e.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SOUND_CATEGORY_PLAYERS, volume, pitch, 16, 47);
	}

	private void playSound(Location loc, Sound sound, String category, float volume, float pitch, double dist, int version) {
		for (Player player : loc.getWorld().getPlayers()) {
			if (Via.getAPI().getPlayerVersion(player) <= version && player.getLocation().distanceSquared(loc) < dist * dist) {
				playSound(player, loc, sound, category, volume, pitch);
			}
		}
	}

	private void playSound(Player player, Location loc, Sound sound, String category, float volume, float pitch) {
		if (isSoundCategory) {
			player.playSound(loc, sound, SoundCategory.valueOf(category), volume, pitch);
		} else {
			player.playSound(loc, sound, volume, pitch);
		}
	}

	private void playBlockPlaceSound(Player player, Block block) {
		try {
			Object blockData = getCraftBlockToNMSBlockStateMethod.invoke(block);
			Object nmsBlock = blockStateToBlockMethod.invoke(blockData);

			Object soundType;
			if (getStepSoundMethod.getParameterCount() == 0) {
				soundType = getStepSoundMethod.invoke(nmsBlock);
			} else {
				soundType = getStepSoundMethod.invoke(nmsBlock, blockData);
			}

			Object soundEffect = null;
			Sound sound = null;
			float volume,pitch;

			if (bukkitSoundGroupMethod != null) {
				SoundGroup soundGroup = (SoundGroup) bukkitSoundGroupMethod.invoke(null, soundType);
				sound = soundGroup.getPlaceSound();
				volume = soundGroup.getVolume();
				pitch = soundGroup.getPitch();
			} else {
				soundEffect = soundEffectMethod.invoke(soundType);
				volume = (float) volumeMethod.invoke(soundType);
				pitch = (float) pitchMethod.invoke(soundType);

				if (bukkitSoundMethod != null) {
					sound = (Sound) bukkitSoundMethod.invoke(null, soundEffect);
				}
			}

			volume = (volume + 1.0f) / 2.0f;
			pitch *= 0.8;

			Location location = block.getLocation().add(0.5, 0.5, 0.5);
			if (sound != null) {
				playSound(player, location, sound, SOUND_CATEGORY_BLOCKS, volume, pitch);
			} else {
				playPacketSound(player, soundEffect, this.soundCategory, location.getX(),
						location.getY(), location.getZ(), volume, pitch);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void playPacketSound(Player player, Object soundEffect, Object soundCategory, double x, double y, double z, float volume, float pitch) {
		try {
			Object packet = soundPacketConstructor.newInstance(soundEffect, soundCategory, x, y, z, volume, pitch);
			NMSReflection.sendPacket(player, packet);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
