package de.gerrygames.viarewind.legacysupport.listener;

import de.gerrygames.viarewind.legacysupport.injector.NMSReflection;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import us.myles.ViaVersion.api.Via;

import java.lang.reflect.Method;

public class SoundListener implements Listener {

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		Player player = e.getPlayer();
		if (Via.getAPI().getPlayerVersion(player)>47) return;
		playBlockPlaceSound(player, e.getBlock());
	}

	private static void playBlockPlaceSound(Player player, Block block) {
		try {
			World world = block.getWorld();
			Object nmsWorld = world.getClass().getMethod("getHandle").invoke(world);
			Class blockPositionClass = NMSReflection.getNMSClass("BlockPosition");
			Object blockPosition = blockPositionClass.getConstructor(int.class, int.class, int.class).newInstance(block.getX(), block.getY(), block.getZ());
			Object blockData = nmsWorld.getClass().getSuperclass().getMethod("getType", blockPositionClass).invoke(nmsWorld, blockPosition);
			Method getBlock = blockData.getClass().getMethod("getBlock");
			getBlock.setAccessible(true);
			Object nmsBlock = getBlock.invoke(blockData);
			Object soundType = nmsBlock.getClass().getMethod("getStepSound").invoke(nmsBlock);

			Object soundEffect = soundType.getClass().getMethod("e").invoke(soundType);
			float f1 = (float) soundType.getClass().getMethod("a").invoke(soundType);
			float f2 = (float) soundType.getClass().getMethod("a").invoke(soundType);
			Object soundCategory = Enum.class.getMethod("valueOf", Class.class, String.class).invoke(null, NMSReflection.getNMSClass("SoundCategory"), "BLOCKS");

			f1 = (f1 + 1.0f) / 2.0f;
			f2 *= 0.8;

			Object packet = NMSReflection.getNMSClass("PacketPlayOutNamedSoundEffect").getConstructor(
					soundEffect.getClass(), soundCategory.getClass(),
					double.class, double.class, double.class,
					float.class, float.class)
					.newInstance(
							soundEffect, soundCategory,
							block.getX() + 0.5, block.getY() + 0.5, block.getZ() + 0.5,
							f1, f2
					);

			NMSReflection.sendPacket(player, packet);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}